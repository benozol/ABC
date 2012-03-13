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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Represents a set of clauses, ie a conjunction of disjunctions of literals.
 */
public class CNFConjunction {
    private Set<CNFClause> clauses;
    
    public CNFConjunction() {
    	this.clauses = new HashSet<CNFClause>();
    }

    public CNFConjunction(Set<CNFClause> clauses) {
        this.clauses = clauses;
    }

    public static CNFConjunction singleton(CNFClause clause) {
        return new CNFConjunction(new HashSet<CNFClause>(Arrays.asList(clause)));
    }

    public Set<CNFClause> getClauses() {
        return clauses;
    }

    public void setClauses(Set<CNFClause> clauses) {
        this.clauses = clauses;
    }

    @Override
    public int hashCode() {
        return clauses.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass() == CNFConjunction.class)
            return clauses.equals(((CNFConjunction) obj).clauses);
        else
            return false;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        boolean first = true;
        for (CNFClause clause : clauses) {
            if (first)
                first = false;
            else
                sb.append(", ");
            sb.append(clause.toString());
        }
        sb.append("}");
        return sb.toString();
    }
}
