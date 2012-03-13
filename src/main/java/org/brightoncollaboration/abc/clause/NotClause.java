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

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.brightoncollaboration.abc.clause.visitor.Visitor;

@XmlRootElement
public class NotClause extends Clause {

    @XmlElementRef
    private final Clause clause;

    public NotClause() {
    	this(null);
    }
    
    public NotClause(Clause c) {
        this.clause = c;
    }

    public Clause getClause() {
        return clause;
    }

    @Override
    public <T> T acceptVisitor(Visitor<T> visitor) {
        return visitor.visitNot(this);
    }

    @Override
    public int hashCode() {
        final int prime = 47;
        int result = 5;
        result = prime * result + ((clause == null) ? 0 : clause.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof NotClause))
            return false;
        NotClause other = (NotClause) obj;
        if (clause == null) {
            if (other.clause != null)
                return false;
        } else if (!clause.equals(other.clause))
            return false;
        return true;
    }
}
