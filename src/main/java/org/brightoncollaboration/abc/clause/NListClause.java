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
public class NListClause extends Clause {

    private final Relation relation;
    private final int number;
    @XmlElementRef @XmlElementWrapper
    private final List<Clause> clauses;
    
    public NListClause() {
    	this(null, null, null);
    }

    public NListClause(Relation relation, Integer number, List<Clause> clauses) {
        this.relation = relation;
        this.number = number;
        this.clauses = clauses;
        // TODO Check number <= clauses.size()
    }

    public int getNumber() {
        return number;
    }

    public Relation getRelation() {
        return relation;
    }

    public List<Clause> getClauses() {
        return clauses;
    }

    @Override
    public <T> T acceptVisitor(Visitor<T> visitor) {
        return visitor.visitNList(this);
    }

    @Override
    public int hashCode() {
        final int prime = 43;
        int result = 4;
        result = prime * result + ((clauses == null) ? 0 : clauses.hashCode());
        result = prime * result + number;
        result = prime * result + ((relation == null) ? 0 : relation.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof NListClause))
            return false;
        NListClause other = (NListClause) obj;
        if (clauses == null) {
            if (other.clauses != null)
                return false;
        } else if (!clauses.equals(other.clauses))
            return false;
        if (number != other.number)
            return false;
        if (relation == null) {
            if (other.relation != null)
                return false;
        } else if (!relation.equals(other.relation))
            return false;
        return true;
    }
}
