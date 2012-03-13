/*
 * This file is part of the Automatic Brighton Classification Tool (ABC-Tool).
 *
 * The ABC-Tool is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * The ABC-Tool is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero Affero General Public
 * License along with The ABC-Tool.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2008, 2012 Benedikt Becker
 */

package org.brightoncollaboration.abc.clause.visitor;

import static org.brightoncollaboration.abc.clause.Clause.negateList;
import static org.brightoncollaboration.abc.clause.Clause.visitClauseList;

import java.util.Arrays;
import java.util.List;

import org.brightoncollaboration.abc.clause.AndClause;
import org.brightoncollaboration.abc.clause.Clause;
import org.brightoncollaboration.abc.clause.ConstantClause;
import org.brightoncollaboration.abc.clause.CriterionClause;
import org.brightoncollaboration.abc.clause.NListClause;
import org.brightoncollaboration.abc.clause.NotClause;
import org.brightoncollaboration.abc.clause.Relation;


/**
 * This class computes rules like simplifications of logical negation,
 * The simplification follows rules like  ¬(x ∧ y) = (¬x ∨ ¬y), ¬(x ∨ y) = (¬x ∧ ¬y), ¬¬x = x.
 */
class NegateOneVisitor implements Visitor<Clause> {
	
    public Clause visitConstant(ConstantClause c) {
    	if (c.getValue())
    		return ConstantClause.FALSE;
    	else
    		return ConstantClause.TRUE;
    }

    public Clause visitCriterion(CriterionClause c) {
        return new CriterionClause(c.getCriterion(), c.getValue().invert());
    }

    public Clause visitNot(NotClause c) {
        return c.getClause();
    }


    public Clause visitAnd(AndClause c) {
        List<Clause> negations = negateList(c.getClauses());
        List<Clause> subClauses = visitClauseList(new NegationNormalForm(), negations);
        return new NListClause(Relation.AT_LEAST, 1, subClauses);
    }

    public Clause visitNList(NListClause c) {

        List<Clause> newClauses = visitClauseList(new NegationNormalForm(), c.getClauses());
        Relation rel = c.getRelation();
        int number = c.getNumber();

        /*
         *             AT_LEAST    AT_LEAST:    AT_MOST:    EXACTLY:
         *                             n           n          n
         *   original: -++++++++   ----+++++   +++++----  ----+----
         *   after:    +--------   ++++-----   -----++++  ++++-++++
         */

        if (rel == Relation.AT_LEAST) {
        	if (number == 1) {
        		List<Clause> negations = negateList(c.getClauses());
        		List<Clause> subClauses = visitClauseList(new NegationNormalForm(), negations);
        		return new AndClause(subClauses);
        	} else
        		return new NListClause(Relation.AT_MOST, number - 1, newClauses);
        }
        else if (rel == Relation.AT_MOST)
            return new NListClause(Relation.AT_LEAST, number + 1, newClauses);
        else {
            assert(rel == Relation.EXACTLY);
            if (number == 0)
                return new NListClause(Relation.AT_LEAST, 1, newClauses);
            else {
                List<Clause> orSubClauses =
                        Arrays.asList(
                                (Clause) new NListClause(Relation.AT_LEAST, number + 1, newClauses),
                                (Clause) new NListClause(Relation.AT_MOST, number - 1, newClauses));
                return new NListClause(Relation.AT_LEAST, 1, orSubClauses);
            }
        } 
    }

}

public class NegationNormalForm implements Visitor<Clause> {

    public Clause visitConstant(ConstantClause c) {
    	return c;
    }

    public Clause visitCriterion(CriterionClause c) {
        return c;
    }

    public Clause visitNot(NotClause c) {
    	return c.getClause()
    		.acceptVisitor(new NegateOneVisitor())
    		.acceptVisitor(this);
    }

    public Clause visitAnd(AndClause c) {
        List<Clause> newClauses = visitClauseList(this, c.getClauses());
        return new AndClause(newClauses);
    }

    public Clause visitNList(NListClause c) {
        List<Clause> newClauses = visitClauseList(this, c.getClauses());
        return new NListClause(c.getRelation(), c.getNumber(), newClauses);
    }

}
