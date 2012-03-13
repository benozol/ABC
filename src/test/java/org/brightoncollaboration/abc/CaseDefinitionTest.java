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

import static org.brightoncollaboration.abc.clause.Clause.and;
import static org.brightoncollaboration.abc.clause.Clause.nlist;
import static org.brightoncollaboration.abc.clause.Clause.not;
import static org.brightoncollaboration.abc.clause.Relation.EXACTLY;
import static org.brightoncollaboration.abc.clause.Relation.AT_LEAST;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.text.ParseException;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

import org.brightoncollaboration.abc.clause.Clause;
import org.brightoncollaboration.abc.clause.CriterionClause;
import org.brightoncollaboration.abc.tools.StringI18n;
import org.junit.Before;
import org.junit.Test;
public class CaseDefinitionTest {

    private Criterion.Creator cc;
    private Clause x;
    private Clause y;
    private Clause z;
    private StringI18n name;
    private CaseDefinition cd;

    @Before public void setUp() {
        cc = Criterion.Creator.fromScratch();
        x = new CriterionClause(cc.getByShortName("X"), Value.POSITIVE);
        y = new CriterionClause(cc.getByShortName("Y"), Value.POSITIVE);
        z = new CriterionClause(cc.getByShortName("Z"), Value.POSITIVE);
        Map<String, String> translations = new HashMap<String, String>();
        translations.put("en", "abc");
        translations.put("de", "def");
        name = new StringI18n(translations);
        cd = new CaseDefinition(name, x, y, z);
    }

    @Test public void getter() {
        assertThat(cd.getName().translation("en"), is("abc"));
        assertThat(cd.getName().translation("de"), is("def"));
        assertThat(cd.getLevel1Clause(), is(x));
        assertThat(cd.getLevel2Clause(), is(y));
        assertThat(cd.getLevel3Clause(), is(z));
    }

    @Test public void criteria() {
        Collection<Criterion> crits = cd.occurringCriteria();
        assertThat(crits.size(), is(3));
        assertThat(crits, hasItems(cc.getByShortName("X"), cc.getByShortName("Y"), cc.getByShortName("Z")));
    }

    @Test public void parseUnparse() {
    	Clause c1 = nlist(EXACTLY, 2, and(not(x), nlist(AT_LEAST, 1, y, z)));
        CaseDefinition cd1 = new CaseDefinition(name, c1, y, z);
        try {
            CaseDefinition cd2 = AbcParser.parseCaseDefinition(cd1.unparse(), cc);
            assertThat(cd1, is(cd2));
        } catch (ParseException exc) {
            fail(exc.toString());
        }
    }

    @Test public void assignLevel() {
        
        Event p1 = new Event().addSymptom(cc.getByShortName("X"), Value.POSITIVE);
        Classification r1 = cd.assignLevel(p1);
        assertThat(r1.getResult().toInt(), is(1));

        Event p2 = new Event().addSymptom(cc.getByShortName("Y"), Value.POSITIVE);
        Classification r2 = cd.assignLevel(p2);
        assertThat(r2.getResult().toInt(), is(2));
        assertThat(r2.getPrecondition1(), is(x));

        Event p3 = new Event().addSymptom(cc.getByShortName("Z"), Value.POSITIVE);
        Classification r3 = cd.assignLevel(p3);
        assertThat(r3.getResult().toInt(), is(3));
        assertThat(r3.getPrecondition1(), is(x));
        assertThat(r3.getPrecondition2(), is(y));

        Event p4 = new Event().addSymptom(cc.getByShortName("X"), Value.POSITIVE);
        assertThat(new CaseDefinition(StringI18n.singleton(""), Clause.and(x, y), y, z).assignLevel(p4).getResult().toInt(),
                is(4));

        Classification r4 = cd.assignLevel(new Event());
        assertThat(r4.getResult().toInt(), is(4));
        assertThat(r4.getPrecondition1(), is(x));
        assertThat(r4.getPrecondition2(), is(y));
        assertThat(r4.getPrecondition3(), is(z));

        Event p5 = new Event()
          .addSymptom(cc.getByShortName("X"), Value.NEGATIVE)
          .addSymptom(cc.getByShortName("Y"), Value.NEGATIVE)
          .addSymptom(cc.getByShortName("Z"), Value.NEGATIVE);
        Classification r5 = cd.assignLevel(p5);
        assertThat(r5.getResult().toInt(), is(5));
    }
}
