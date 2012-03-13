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
import java.util.Map;
import java.util.Set;

import org.brightoncollaboration.abc.Criterion;

/** Represents a disjunction of CNFLiterals.
 */
public class CNFClause {
    private Set<CNFLiteral> literals;

    public CNFClause(Set<CNFLiteral> literals) {
        this.literals = literals;
    }

    public CNFClause() {
        this.literals = new HashSet<CNFLiteral>();
    }

    public Set<CNFLiteral> getLiterals() {
        return literals;
    }

    public static CNFClause singleton(CNFLiteral literal) {
        return new CNFClause(new HashSet<CNFLiteral>(Arrays.asList(literal)));
    }

    @Override
    public int hashCode() {
        return literals.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass() == CNFClause.class)
            return literals.equals(((CNFClause) obj).literals);
        else
            return false;
    }

    public int[] toInts(Map<Criterion, Integer> indices) {
        int[] result = new int[literals.size()];
        int index = 0;
        for (CNFLiteral literal : literals)
            result[index++] = literal.toInt(indices);
        return result;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        boolean first = true;
        for (CNFLiteral lit : literals) {
            if (first)
                first = false;
            else
                sb.append(", ");
            sb.append(lit.toString());
        }
        sb.append("}");
        return sb.toString();
    }
}

