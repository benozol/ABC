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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;


public class ValueTest {

    @Test(expected=RuntimeException.class)
    public final void testToBoolean() {
        assertThat(Value.POSITIVE.toBoolean(), is(true));
        assertThat(Value.NEGATIVE.toBoolean(), is(false));
        Value.UNDEFINED.toBoolean();
    }

    @Test(expected=RuntimeException.class)
    public final void testInvert() {
        assertThat(Value.POSITIVE.invert(), is(Value.NEGATIVE));
        assertThat(Value.NEGATIVE.invert(), is(Value.POSITIVE));
        Value.UNDEFINED.invert();
    }

    @Test
    public final void testFromBoolean() {
        assertThat(Value.fromBoolean(true), is(Value.POSITIVE));
        assertThat(Value.fromBoolean(false), is(Value.NEGATIVE));
    }

    @Test
    public final void testToFromInteger() {
        assertThat(Value.fromInteger(Value.POSITIVE.toInteger()), is(Value.POSITIVE));
        assertThat(Value.fromInteger(Value.NEGATIVE.toInteger()), is(Value.NEGATIVE));
        assertThat(Value.fromInteger(Value.UNDEFINED.toInteger()), is(Value.UNDEFINED));
    }

    @Test
    public final void testLiterals() {
        Map<String, Value> ls = Value.literals("yes", "no", "undefined");
        assertThat(ls.get("yes"), is(Value.POSITIVE));
        assertThat(ls.get("no"), is(Value.NEGATIVE));
        assertThat(ls.get("undefined"), is(Value.UNDEFINED));
    }

}
