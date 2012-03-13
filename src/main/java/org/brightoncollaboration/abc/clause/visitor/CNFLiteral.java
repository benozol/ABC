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

import java.util.Map;

import org.brightoncollaboration.abc.Criterion;

public class CNFLiteral {

    private final Criterion criterion;
    private final boolean positive;

    public CNFLiteral(Criterion criterion, boolean positive) {
        this.criterion = criterion;
        this.positive = positive;
    }

    public boolean getPositive() {
        return positive;
    }

    public Criterion getCriterion() {
        return criterion;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass() == CNFLiteral.class) {
            CNFLiteral literal = (CNFLiteral) obj;
            return positive == literal.positive && criterion.equals(literal.criterion);
        } else
            return false;
    }

    @Override
    public int hashCode() {
        return 2 * criterion.hashCode() + (positive ? 0 : 1);
    }

    @Override
    public String toString() {
        if (positive)
            return criterion.getShortName();
        else
            return "-" + criterion.getShortName();
    }

    public int toInt(Map<Criterion, Integer> indices) {
        if (positive)
            return indices.get(criterion);
        else
            return -indices.get(criterion);
    }
}
