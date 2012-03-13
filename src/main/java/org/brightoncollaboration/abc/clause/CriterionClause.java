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

package org.brightoncollaboration.abc.clause;

import javax.xml.bind.annotation.XmlRootElement;

import org.brightoncollaboration.abc.Criterion;
import org.brightoncollaboration.abc.Value;
import org.brightoncollaboration.abc.clause.visitor.Visitor;

@XmlRootElement
public class CriterionClause extends Clause {

    private final Criterion criterion;
    private final Value value;

    public CriterionClause() {
    	this(null, null);
    }
    
    public CriterionClause(Criterion criterion, Value value) {
        this.criterion = criterion;
        this.value = value;
    }

    public Criterion getCriterion() {
        return criterion;
    }
    
    public String getCriterionID() {
    	return criterion.getShortName();
    }

    public Value getValue() {
        return value;
    }
    
    @Override
    public <T> T acceptVisitor(Visitor<T> visitor) {
        return visitor.visitCriterion(this);
    }

    @Override
    public int hashCode() {
        final int prime = 41;
        int result = 3;
        result = prime * result + ((criterion == null) ? 0 : criterion.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof CriterionClause))
            return false;
        CriterionClause other = (CriterionClause) obj;
        if (criterion == null) {
            if (other.criterion != null)
                return false;
        } else if (!criterion.equals(other.criterion))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

}
