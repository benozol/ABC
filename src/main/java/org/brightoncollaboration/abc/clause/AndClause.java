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

import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.brightoncollaboration.abc.clause.visitor.Visitor;

@XmlRootElement
public class AndClause extends Clause {

    @XmlElementRef @XmlElementWrapper
    private final List<Clause> clauses;

    public AndClause() {
    	this(null);
    }
    
    public AndClause(List<Clause> clauses) {
        this.clauses = clauses;
    }

    public List<Clause> getClauses() {
        return clauses;
    }

    @Override
    public <T> T acceptVisitor(Visitor<T> visitor) {
        return visitor.visitAnd(this);
    }

    @Override
    public int hashCode() {
        final int prime = 33;
        int result = 1;
        result = prime * result + ((clauses == null) ? 0 : clauses.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof AndClause))
            return false;
        AndClause other = (AndClause) obj;
        if (clauses == null) {
            if (other.clauses != null)
                return false;
        } else if (!clauses.equals(other.clauses))
            return false;
        return true;
    }
    
}
