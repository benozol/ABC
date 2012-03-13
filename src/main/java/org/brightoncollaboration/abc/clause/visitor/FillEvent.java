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

import static org.brightoncollaboration.abc.clause.Clause.visitClauseList;

import java.util.List;

import org.brightoncollaboration.abc.Event;
import org.brightoncollaboration.abc.Value;
import org.brightoncollaboration.abc.clause.AndClause;
import org.brightoncollaboration.abc.clause.Clause;
import org.brightoncollaboration.abc.clause.ConstantClause;
import org.brightoncollaboration.abc.clause.CriterionClause;
import org.brightoncollaboration.abc.clause.NListClause;
import org.brightoncollaboration.abc.clause.NotClause;

/**
 * Fills criteria in a clause with available information form a event.
 */
public class FillEvent implements Visitor<Clause> {

    protected Event event;

    public FillEvent(Event p) {
        this.event = p;
    }

    public Clause visitConstant(ConstantClause constantClause) {
    	return constantClause;
    }

    public Clause visitCriterion(CriterionClause criterionClause) {
        Value value = event.valueOfCriterion(criterionClause.getCriterion());
        if( criterionClause.getValue() == value )
            return ConstantClause.TRUE;
        else if( value == Value.UNDEFINED )
            return criterionClause;
        else 
            return ConstantClause.FALSE;
    }

    public Clause visitNot(NotClause notClause) {
        return new NotClause(notClause.getClause().acceptVisitor(this));
    }

    public Clause visitAnd(AndClause andClause) {
        List<Clause> newClauses = visitClauseList(this, andClause.getClauses());
        return new AndClause(newClauses);
    }

    public Clause visitNList(NListClause nListClause) {
        List<Clause> newClauses = visitClauseList(this, nListClause.getClauses());
        return new NListClause(nListClause.getRelation(), nListClause.getNumber(), newClauses);
    }
}
