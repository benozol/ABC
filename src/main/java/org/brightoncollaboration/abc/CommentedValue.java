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

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A {@link org.brightoncollaboration.abc.Value} combined with a comment. This
 * is not used in the ABC inference core but in
 * {@link org.brightoncollaboration.abc.AbcEngine} and
 * {@link org.brightoncollaboration.abc.AbcDatabase}.
 */
@XmlRootElement
public class CommentedValue {

    public final Value value;

    public final String comment;
    
    public CommentedValue() {
    	this(null, null);
    }

    public CommentedValue(Value value, String comment) {
        this.value = value;
        this.comment = comment;
    }

    public CommentedValue(Value value) {
        this(value, null);
    }

    /**
     * Similar to {@link org.brightoncollaboration.abc.Event} but with commented
     * values.
     */
    public static class Event {

        private String uid;
        private Map<Criterion, CommentedValue> symptoms;

        public Event() {
            this(null, new HashMap<Criterion, CommentedValue>());
        }

        public Event(String uid) {
            this(uid, new HashMap<Criterion, CommentedValue>());
        }

        public Event(Map<Criterion, CommentedValue> symptoms) {
            this(null, symptoms);
        }

        public Event(String uid, Map<Criterion, CommentedValue> symptoms) {
            this.uid = uid;
            this.symptoms = symptoms;
        }

        public String getUid() {
            return uid;
        }

        public Map<Criterion, CommentedValue> getSymptoms() {
            return symptoms;
        }

        public void addSymptom(Criterion criterion, CommentedValue value) {
            symptoms.put(criterion, value);
        }

        public org.brightoncollaboration.abc.Event toEvent() {
            Map<Criterion, Value> res = new HashMap<Criterion, Value>();
            for (Criterion criterion : symptoms.keySet())
                res.put(criterion, symptoms.get(criterion).value);
            return new org.brightoncollaboration.abc.Event(res);
        }

        public static Event fromEvent(String uid, org.brightoncollaboration.abc.Event event) {
            Map<Criterion, CommentedValue> symptoms = new HashMap<Criterion, CommentedValue>();
            for (Criterion criterion : event.getSymptoms().keySet())
                symptoms.put(criterion, new CommentedValue(event.getSymptoms().get(criterion), null));
            return new Event(uid, symptoms);
        }
    }
}
