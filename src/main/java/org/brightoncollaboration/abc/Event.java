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

package org.brightoncollaboration.abc;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Event {

    private Map<Criterion, Value> symptoms;

    public Event() {
        this(new HashMap<Criterion, Value>());
    }

    public Event(Map<Criterion, Value> symptoms) {
        this.symptoms = symptoms;
    }

    public Map<Criterion, Value> getSymptoms() {
        return symptoms;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((symptoms == null) ? 0 : symptoms.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Event))
            return false;
        Event other = (Event) obj;
        if (symptoms == null) {
            if (other.symptoms != null)
                return false;
        } else if (!symptoms.equals(other.symptoms))
            return false;
        return true;
    }

    public Event addSymptom(Criterion criterion, Value value) {
        symptoms.put(criterion, value);
        return this;
    }

    public Value valueOfCriterion(Criterion crit) {
        if (symptoms.containsKey(crit))
            return symptoms.get(crit);
        else
            return Value.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Event:");
        for( Criterion c: symptoms.keySet() )
            sb.append(String.format(" %s=%s", c.getShortName(), symptoms.get(c).toString()));
        return sb.toString();
    }

    /** Returns a new event adding the given symptom.
     */
    public Event extension(Criterion criterion, Value value) {
        Map<Criterion, Value> newSymptoms = new HashMap<Criterion, Value>(symptoms);
        newSymptoms.put(criterion, value);
        return new Event(newSymptoms);
    }

    /** Returns the collection of all possible extensions by the given criteria.
     */
    public Collection<Event> extensions(Collection<Criterion> criteria) {
        return extensions(new LinkedList<Criterion>(criteria), 0);
    }

    private Collection<Event> extensions(List<Criterion> criteria, int ix) {
        if (ix < criteria.size()) {
            Collection<Event> res = new HashSet<Event>();
            Criterion criterion = criteria.get(ix);
            res.addAll(extension(criterion, Value.POSITIVE).extensions(criteria, ix+1));
            res.addAll(extension(criterion, Value.NEGATIVE).extensions(criteria, ix+1));
            res.addAll(extension(criterion, Value.UNDEFINED).extensions(criteria, ix+1));
            return res;
        } else
            return Collections.singleton(this);
    }
}
