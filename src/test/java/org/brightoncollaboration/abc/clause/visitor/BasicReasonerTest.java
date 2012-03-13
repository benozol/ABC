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

import static org.brightoncollaboration.abc.Value.NEGATIVE;
import static org.brightoncollaboration.abc.Value.POSITIVE;
import static org.brightoncollaboration.abc.Value.UNDEFINED;
import static org.brightoncollaboration.abc.clause.Clause.and;
import static org.brightoncollaboration.abc.clause.Clause.criterion;
import static org.brightoncollaboration.abc.clause.Clause.fals;
import static org.brightoncollaboration.abc.clause.Clause.nlist;
import static org.brightoncollaboration.abc.clause.Clause.not;
import static org.brightoncollaboration.abc.clause.Clause.tru;
import static org.brightoncollaboration.abc.clause.Relation.AT_LEAST;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.brightoncollaboration.abc.Criterion;
import org.brightoncollaboration.abc.Event;
import org.junit.Test;

public class BasicReasonerTest {

    Criterion.Creator cc = Criterion.Creator.fromScratch();
    Criterion a = cc.getByShortName("a");
    Criterion b = cc.getByShortName("b");
    Criterion c = cc.getByShortName("c");

    @Test
    public void visitConstant() {
        FillEvent r = new FillEvent(new Event());
        assertThat(tru().acceptVisitor(r), is(tru()));
        assertThat(fals().acceptVisitor(r), is(fals()));
    }

    @Test
    public void visitCriterion() {
        Event e = new Event()
            .addSymptom(a, POSITIVE)
            .addSymptom(b, NEGATIVE);
        FillEvent r = new FillEvent(e);

        assertThat(criterion(a, POSITIVE).acceptVisitor(r),
                is(tru()));
        assertThat(criterion(b, NEGATIVE).acceptVisitor(r),
                is(tru()));
        assertThat(criterion(c, UNDEFINED).acceptVisitor(r),
                is(tru()));

        assertThat(criterion(a, NEGATIVE).acceptVisitor(r),
                is(fals()));
        assertThat(criterion(b, POSITIVE).acceptVisitor(r),
                is(fals()));
        assertThat(criterion(c, POSITIVE).acceptVisitor(r),
                is(criterion(c, POSITIVE)));
        assertThat(criterion(c, NEGATIVE).acceptVisitor(r),
                is(criterion(c, NEGATIVE)));

        assertThat(criterion(a, UNDEFINED).acceptVisitor(r),
                is(fals()));
        assertThat(criterion(b, UNDEFINED).acceptVisitor(r),
                is(fals()));
    }

    @Test
    public void visitNot() {
        FillEvent r = new FillEvent(new Event().addSymptom(a, POSITIVE).addSymptom(b, NEGATIVE));
        assertThat(not(fals()).acceptVisitor(r),
                is(not(fals())));
        assertThat(not(criterion(a, POSITIVE)).acceptVisitor(r),
                is(not(tru())));
    }

    @Test
    public void visitAnd() {
        FillEvent r = new FillEvent(new Event().addSymptom(a, POSITIVE).addSymptom(b, NEGATIVE));

        assertThat(and(tru(), fals()).acceptVisitor(r),
                is(and(tru(), fals())));
        assertThat(and(criterion(a, POSITIVE), criterion(b, POSITIVE)).acceptVisitor(r),
                is(and(tru(), fals())));
    }

    @Test
    public void visitNList() {
        FillEvent r = new FillEvent(new Event().addSymptom(a, POSITIVE).addSymptom(b, NEGATIVE));

        assertThat(nlist(AT_LEAST, 2, tru(), fals()).acceptVisitor(r),
                is(nlist(AT_LEAST, 2, tru(), fals())));
        assertThat(nlist(AT_LEAST, 2, criterion(a, POSITIVE), criterion(b, POSITIVE))
                    .acceptVisitor(r),
                is(nlist(AT_LEAST, 2, tru(), fals())));
    }
}
