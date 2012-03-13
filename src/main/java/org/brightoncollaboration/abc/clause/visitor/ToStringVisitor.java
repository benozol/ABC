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

import java.util.ArrayList;
import java.util.List;

import org.brightoncollaboration.abc.Value;
import org.brightoncollaboration.abc.clause.AndClause;
import org.brightoncollaboration.abc.clause.Clause;
import org.brightoncollaboration.abc.clause.ConstantClause;
import org.brightoncollaboration.abc.clause.CriterionClause;
import org.brightoncollaboration.abc.clause.NListClause;
import org.brightoncollaboration.abc.clause.NotClause;
import org.brightoncollaboration.abc.clause.Relation;

/** Produces a very compact string representation of a clause.
 */
public class ToStringVisitor implements Visitor<String> {

    public static final String TRUE_STRING = "\u22a4";
    public static final String FALSE_STRING = "\u22a5";
    public static final String NOT_STRING = "\u00AC";
    public static final String CRITERION_UNKNOWN_STRING = "?";
    public static final String NEG_STRING = "!";
    public static final String OR_STRING = "\u2228";
    public static final String AND_STRING = "\u2227";
    public static final String EQ_STRING = "=";
    public static final String LEQ_STRING = "\u2264";
    public static final String GEQ_STRING = "\u2265";

    public String visitConstant(ConstantClause c) {
    	if (c.getValue())
    		return TRUE_STRING;
    	else
    		return FALSE_STRING;
    }

    public String visitCriterion(CriterionClause criterionClause) {
        String name = criterionClause.getCriterion().getShortName();
        if (name == null)
            name = criterionClause.getCriterion().getWording().defaultTranslation();

        if (criterionClause.getValue() == Value.POSITIVE)
            return name;
        else if (criterionClause.getValue() == Value.NEGATIVE)
            return String.format("%s%s", NEG_STRING, name);
        else
            return String.format("%s%s", CRITERION_UNKNOWN_STRING, name);
    }

    public String visitNot(NotClause c) {
        return String.format("%s%s", NOT_STRING, c.getClause().acceptVisitor(this));
    }

    public String detok(List<Clause> list, String det) {
        ArrayList<Clause> a = new ArrayList<Clause>(list);
        if (a.size() == 0)
            return det;
        if (a.size() == 1)
            return a.get(0).acceptVisitor(this);

        String ret = "";
        for (int i = 0; i < a.size() - 1; i++)
            ret += a.get(i).acceptVisitor(this) + det;
        ret += a.get(a.size() - 1).acceptVisitor(this);
        return ret;
    }

    public String visitAnd(AndClause andClause) {
        return String.format("(%s)", detok(andClause.getClauses(), " " + AND_STRING + " "));
    }

    public String visitNList(NListClause nlist) {
        String rel;
        if(nlist.getRelation() == Relation.AT_LEAST)
            rel = LEQ_STRING;
        else if(nlist.getRelation() == Relation.AT_MOST)
            rel = GEQ_STRING;
        else {
            assert nlist.getRelation() == Relation.EXACTLY;
            rel = EQ_STRING;
        }
        return String.format("(%d %s %s)",
                nlist.getNumber(),
                rel,
                detok(nlist.getClauses(), ", "));
    }
}
