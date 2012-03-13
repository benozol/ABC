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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.brightoncollaboration.abc.Criterion;
import org.brightoncollaboration.abc.clause.AndClause;
import org.brightoncollaboration.abc.clause.Clause;
import org.brightoncollaboration.abc.clause.ConstantClause;
import org.brightoncollaboration.abc.clause.CriterionClause;
import org.brightoncollaboration.abc.clause.NListClause;
import org.brightoncollaboration.abc.clause.NotClause;

public class CriterionCollector implements Visitor<Set<Criterion>> {

    public Set<Criterion> visitConstant(ConstantClause c) {
        return new HashSet<Criterion>();
    }

    public Set<Criterion> visitCriterion(CriterionClause criterionClause) {
        return Collections.singleton(criterionClause.getCriterion());
    }

    public Set<Criterion> visitNot(NotClause c) {
        return c.getClause().acceptVisitor(this);
    }

    public Set<Criterion> visitAnd(AndClause andClause) {
        Set<Criterion> criteria = new HashSet<Criterion>();
        for (Clause c : andClause.getClauses())
            criteria.addAll(c.acceptVisitor(this));
        return criteria;
    }

    public Set<Criterion> visitNList(NListClause nlistClause) {
        Set<Criterion> criteria = new HashSet<Criterion>();
        for (Clause c : nlistClause.getClauses())
            criteria.addAll(c.acceptVisitor(this));
        return criteria;
    }
}
