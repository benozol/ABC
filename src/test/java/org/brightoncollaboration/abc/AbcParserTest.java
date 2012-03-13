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

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.brightoncollaboration.abc.clause.AndClause;
import org.brightoncollaboration.abc.clause.Clause;
import org.brightoncollaboration.abc.clause.ConstantClause;
import org.brightoncollaboration.abc.clause.CriterionClause;
import org.brightoncollaboration.abc.clause.NListClause;
import org.brightoncollaboration.abc.clause.NotClause;
import org.brightoncollaboration.abc.clause.Relation;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;

public class AbcParserTest {

    private Criterion.Creator cc;

    private List<Clause> clauses;
    private Clause x;
    private Clause y;
    private Clause z;
    
    @Before
    public void setUp() throws Exception {
        cc = Criterion.Creator.fromScratch();
        x = new CriterionClause(cc.getByShortName("X"), Value.POSITIVE);
        y = new CriterionClause(cc.getByShortName("Y"), Value.POSITIVE);
        z = new CriterionClause(cc.getByShortName("Z"), Value.POSITIVE);
        clauses = Arrays.asList(x, y, z);
    }

    private Matcher<String> parsesTo(final Clause clause) {
        return new TypeSafeMatcher<String>() {
            public boolean matchesSafely(String str) {
                try {
                    Clause res = AbcParser.parseClause(str, cc);
                    return is(clause).matches(res);
                } catch(ParseException exc) {
                    return false;
                }
            }
            public void describeTo(Description description) {
                description
                  .appendText(" parses to ")
                  .appendText(clause.unparse());
            }
        };
    }
    
    private Matcher<String> parseException(final Matcher<String> excMatcher) {
        return new TypeSafeMatcher<String>() {
            public boolean matchesSafely(String str) {
                try {
                    AbcParser.parseClause(str, cc);
                    return false;
                } catch (ParseException exc) {
                    return excMatcher.matches(exc.getMessage());
                }
            }
            public void describeTo(Description description) {
                description.appendText(" parse throws exception ");
                excMatcher.describeTo(description);
            }
        };
    }


    @Test public void constant() {
        assertThat("TRUE", parsesTo(ConstantClause.TRUE));
        assertThat("NOTAPPLICABLE", parsesTo(ConstantClause.FALSE));
    }

    @Test public void symptom() {
        assertThat("x", parseException(is("Only constants, criteria and parenthesize clauses allowed here")));
        assertThat("X", parsesTo(x));
        assertThat("!X", parsesTo(new CriterionClause(cc.getByShortName("X"), Value.NEGATIVE)));
        assertThat("?X", parsesTo(new CriterionClause(cc.getByShortName("X"), Value.UNDEFINED)));
    }


    @Test public void prefixClause() {
        assertThat("NOT", parseException(any(String.class)));
        assertThat("NOT X", parsesTo(new NotClause(new CriterionClause(cc.getByShortName("X"), Value.POSITIVE))));
        assertThat("AND X,Y,Z", parsesTo(new AndClause(clauses)));
        assertThat("EXACTLY 1 FROM X,Y,Z", parsesTo(new NListClause(Relation.EXACTLY, 1, clauses)));
        assertThat("ATLEAST 3 FROM X,Y,Z", parsesTo(new NListClause(Relation.AT_LEAST, 3, clauses)));
        assertThat("ATMOST 2 FROM X,Y,Z", parsesTo(new NListClause(Relation.AT_MOST, 2, clauses)));
    }

    @Test public void infixClause() {
        assertThat("X AND Y AND Z", parsesTo(new AndClause(clauses)));
        //assertThat("X XOR Y XOR Z", parsesTo(new NListClause(Relation.EXACTLY, 1, clauses)));
    }

    @Test public void caseDefinition() {
        String str1 =
            "CASEDEFINITION \"abc\"\n" +
            "LEVEL1 ( X )\n" +
            "LEVEL2 ( Y )\n" +
            "LEVEL3 ( Z )";
        try {
            CaseDefinition cd = AbcParser.parseCaseDefinition(str1, cc);
            assertThat(cd.getName().defaultTranslation(), is("abc"));
            assertThat(cd.getLevel1Clause(), is(x));
            assertThat(cd.getLevel2Clause(), is(y));
            assertThat(cd.getLevel3Clause(), is(z));
        } catch (ParseException exc) {
            fail(exc.toString());
        }
        String str2 =
        	"CASEDEFINITION en: \"abc\" fr: \"gu\"\n" +
            "LEVEL1 ( X )\n" +
            "LEVEL2 ( Y )\n" +
            "LEVEL3 ( Z )";
        try {
            CaseDefinition cd = AbcParser.parseCaseDefinition(str2, cc);
            assertThat(cd.getName().translation("en"), is("abc"));
            assertThat(cd.getName().translation("fr"), is("gu"));
            assertThat(cd.getLevel1Clause(), is(x));
            assertThat(cd.getLevel2Clause(), is(y));
            assertThat(cd.getLevel3Clause(), is(z));
        } catch (ParseException exc) {
            fail(exc.toString());
        }
    }

}
