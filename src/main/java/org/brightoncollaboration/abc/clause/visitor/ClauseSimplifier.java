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

import java.util.LinkedList;
import java.util.List;

import org.brightoncollaboration.abc.Value;
import org.brightoncollaboration.abc.clause.AndClause;
import org.brightoncollaboration.abc.clause.Clause;
import org.brightoncollaboration.abc.clause.ConstantClause;
import org.brightoncollaboration.abc.clause.CriterionClause;
import org.brightoncollaboration.abc.clause.NListClause;
import org.brightoncollaboration.abc.clause.NotClause;
import org.brightoncollaboration.abc.clause.Relation;

/**
 * A ClauseSimplifier simplifies a given Clause statically.
 * This means the result is semantically identical to the original clause but
 * simpler in that all statically known clauses are solved (such including ┬ and ┴).
 */
public class ClauseSimplifier implements Visitor<Clause> {

    public Clause visitConstant(ConstantClause constantClause) {
    	return constantClause;
    }

    public Clause visitCriterion(CriterionClause criterionClause) {
        return criterionClause;
    }

    public Clause visitNot(NotClause notClause) {

        Clause subClause = notClause.getClause().acceptVisitor(this);

        if (subClause == ConstantClause.TRUE)
            return ConstantClause.FALSE;

        if (subClause == ConstantClause.FALSE)
            return ConstantClause.TRUE;

        if (subClause instanceof CriterionClause) {
            CriterionClause criterionClause = (CriterionClause) subClause;
            if (criterionClause.getValue() != Value.UNDEFINED)
                return new CriterionClause(criterionClause.getCriterion(), criterionClause.getValue().invert());
        }

        return new NotClause(subClause);
    }

    public Clause visitAnd(AndClause andClause) {
        List<Clause> newClauses = new LinkedList<Clause>();

        for (Clause c : andClause.getClauses()) {
            Clause d = c.acceptVisitor(this);

            if (d == ConstantClause.FALSE)
                return ConstantClause.FALSE;

            else if (d instanceof AndClause)
                newClauses.addAll(((AndClause) d).getClauses());
            else if (d != ConstantClause.TRUE)
                newClauses.add(d);
        }

        int size = newClauses.size();
        if (size == 0)
            return ConstantClause.TRUE;
        else if (size == 1)
            return newClauses.get(0);
        else
            return new AndClause(newClauses);
    }

    public Clause visitNList(NListClause nListClause) {

        int topCount = 0;    // Number of subclauses reducing to TRUE
        int bottomCount = 0; // Number of subclauses reducing to FALSE
        List<Clause> newClauses = new LinkedList<Clause>();  // Simplified, nontrivial subclauses
        for (Clause c : nListClause.getClauses()) {
            Clause d = c.acceptVisitor(this);
            if (d.equals(ConstantClause.TRUE))
                topCount ++;
            else if(d.equals(ConstantClause.FALSE))
                bottomCount++;
            else
                newClauses.add(d);
        }

        Relation rel = nListClause.getRelation();

        if (rel.satisfies(nListClause.getNumber(), topCount, newClauses.size()))
            return ConstantClause.TRUE;
        else {
            int newNumber = nListClause.getNumber() - topCount;
            if (rel.possible(newNumber, newClauses.size())) {
                /* TODO Think again, it’s late
                 *   n ≤ c₁, …, c_n ⇒ c₁ ∧ … ∧ c_n 
                 *   n = c₁, …, c_n ⇒ c₁ ∧ … ∧ c_n
                 */
                if (newNumber == newClauses.size() && (rel == Relation.AT_LEAST || rel == Relation.EXACTLY)) {
                    return new AndClause(newClauses).acceptVisitor(this);
                } else
                    return new NListClause(rel, newNumber, newClauses);
            } else
                return ConstantClause.FALSE;
        }
    }
}
