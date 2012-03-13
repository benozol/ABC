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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class EventTest  {

    Criterion.Creator cc = Criterion.Creator.fromScratch();
    Criterion a = cc.getByShortName("a");
    Criterion b = cc.getByShortName("b");
    Criterion c = cc.getByShortName("c");

	@Test
    public void addSymptom() {
        Event ev1 = new Event();
        assertThat(ev1.getSymptoms().size(), is(0));
        ev1.addSymptom(a, Value.POSITIVE);
        ev1.addSymptom(b, Value.NEGATIVE);
        assertThat(ev1.getSymptoms().size(), is(2));
        assertThat(ev1, event(symptom(a, Value.POSITIVE), symptom(b, Value.NEGATIVE)));

        Event ev2 = new Event();
        ev2.addSymptom(a, Value.POSITIVE);
        ev2.addSymptom(b, Value.NEGATIVE);
        ev2.addSymptom(c, Value.UNDEFINED);
        assertThat(ev2.getSymptoms().size(), is(3));
        assertThat(ev2, event(symptom(a, Value.POSITIVE), symptom(b, Value.NEGATIVE), symptom(c, Value.UNDEFINED)));
    }

    @Test
    public void valueOfCriterion() {
        Event ev1 = new Event().addSymptom(a, Value.NEGATIVE);
        assertThat(ev1.valueOfCriterion(a), is(Value.NEGATIVE));
        assertThat(ev1.valueOfCriterion(b), is(Value.UNDEFINED));
    }

    @Test
    public void equals() {
        Event ev1 = new Event().addSymptom(a, Value.POSITIVE);
        Event ev2 = new Event().addSymptom(a, Value.POSITIVE);
        assertThat(ev1, is(ev2));
        ev2.addSymptom(b, Value.NEGATIVE);
        assertThat(ev1, not(is(ev2)));
    }

    @Test
    public void extensions() {
        Collection<Criterion> criteria = new HashSet<Criterion>();
        criteria.add(cc.getByShortName("a"));
        criteria.add(cc.getByShortName("b"));
        Collection<Event> extensions = new Event().extensions(criteria);

        Event ev = new Event().addSymptom(a, Value.POSITIVE);
        assertThat(ev.getSymptoms().entrySet().iterator().next(),
            symptom(a, Value.POSITIVE));
        assertThat(ev, event(symptom(a, Value.POSITIVE)));

        assertThat(extensions.size(), is(3*3));
        assertThat(extensions, hasItems(
            event(symptom(b, Value.POSITIVE),  symptom(a, Value.POSITIVE)),
            event(symptom(b, Value.POSITIVE),  symptom(a, Value.NEGATIVE)),
            event(symptom(b, Value.POSITIVE),  symptom(a, Value.UNDEFINED)),
            event(symptom(b, Value.NEGATIVE),  symptom(a, Value.POSITIVE)),
            event(symptom(b, Value.NEGATIVE),  symptom(a, Value.NEGATIVE)),
            event(symptom(b, Value.NEGATIVE),  symptom(a, Value.UNDEFINED)),
            event(symptom(b, Value.UNDEFINED), symptom(a, Value.POSITIVE)),
            event(symptom(b, Value.UNDEFINED), symptom(a, Value.NEGATIVE)),
            event(symptom(b, Value.UNDEFINED), symptom(a, Value.UNDEFINED))));
    }

    private Matcher<Event> event(final Matcher<Map.Entry<Criterion, Value>>... symptomMatchers) {
        return new TypeSafeMatcher<Event>() {
            public void describeTo(Description desc) {
                desc.appendText(String.format("event with symptoms "));
                for (Matcher<Map.Entry<Criterion,Value>> s : symptomMatchers)
                    s.describeTo(desc);
            }
            @Override public boolean matchesSafely(Event event) {
                return event.getSymptoms().size() == symptomMatchers.length
                    && hasItems(symptomMatchers).matches(event.getSymptoms().entrySet());
            }
        };
    }

    private Matcher<Map.Entry<Criterion, Value>> symptom(final Criterion criterion, final Value value) {
        return new TypeSafeMatcher<Map.Entry<Criterion, Value>>() {
            public void describeTo(Description desc) {
                desc.appendText(String.format("%s%s", value.toString(), criterion.getShortName()));
            }
            @Override public boolean matchesSafely(Map.Entry<Criterion, Value> symptom) {
                return criterion.equals(symptom.getKey())
                    && value.equals(symptom.getValue());
            }
        };
    }

}
