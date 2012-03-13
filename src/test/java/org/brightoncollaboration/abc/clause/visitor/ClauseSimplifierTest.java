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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.brightoncollaboration.abc.Criterion;
import org.brightoncollaboration.abc.Value;
import org.brightoncollaboration.abc.clause.AndClause;
import org.brightoncollaboration.abc.clause.Clause;
import org.brightoncollaboration.abc.clause.ConstantClause;
import org.brightoncollaboration.abc.clause.CriterionClause;
import org.brightoncollaboration.abc.clause.NListClause;
import org.brightoncollaboration.abc.clause.NotClause;
import org.brightoncollaboration.abc.clause.Relation;
import org.junit.Test;

public class ClauseSimplifierTest {

    Criterion.Creator cc = Criterion.Creator.fromScratch();
    Criterion a = cc.getByShortName("a");
    Criterion b = cc.getByShortName("b");
    Criterion c = cc.getByShortName("c");

    @Test
    public void visitConstant() {
        ClauseSimplifier r = new ClauseSimplifier();
        assertThat(ConstantClause.TRUE.acceptVisitor(r),
                is((Clause) ConstantClause.TRUE));
        assertThat(ConstantClause.FALSE.acceptVisitor(r),
                is((Clause) ConstantClause.FALSE));
    }

    @Test
    public void visitCriterion() {
        ClauseSimplifier r = new ClauseSimplifier();

        Clause c1 = new CriterionClause(a, Value.POSITIVE);
        Clause c2 = new CriterionClause(b, Value.NEGATIVE);
        Clause c3 = new CriterionClause(c, Value.UNDEFINED);
        assertThat(c1.acceptVisitor(r), is(c1));
        assertThat(c2.acceptVisitor(r), is(c2));
        assertThat(c3.acceptVisitor(r), is(c3));
    }

    @Test
    public void visitNot() {
        ClauseSimplifier r = new ClauseSimplifier();
        assertThat(new NotClause(ConstantClause.TRUE).acceptVisitor(r),
                is((Clause) ConstantClause.FALSE));
        assertThat(new NotClause(ConstantClause.FALSE).acceptVisitor(r),
                is((Clause) ConstantClause.TRUE));
        assertThat(new NotClause(new CriterionClause(a, Value.POSITIVE)).acceptVisitor(r),
                is((Clause) new CriterionClause(a, Value.NEGATIVE)));
        assertThat(new NotClause(new CriterionClause(a, Value.NEGATIVE)).acceptVisitor(r),
                is((Clause) new CriterionClause(a, Value.POSITIVE)));
        Clause c = new NotClause(new CriterionClause(a, Value.UNDEFINED));
        assertThat(c.acceptVisitor(r), is(c));
    }

    @Test
    public void visitAnd() {
        ClauseSimplifier r = new ClauseSimplifier();

        List<Clause> cs1 = Arrays.asList();
        assertThat(new AndClause(cs1).acceptVisitor(r),
                is((Clause) ConstantClause.TRUE));

        List<Clause> cs2 = Arrays.asList((Clause) ConstantClause.TRUE);
        assertThat(new AndClause(cs2).acceptVisitor(r),
                is((Clause) ConstantClause.TRUE));

        List<Clause> cs3 = Arrays.asList((Clause) new CriterionClause(a,Value.POSITIVE), ConstantClause.TRUE);
        assertThat(new AndClause(cs3).acceptVisitor(r),
                is((Clause) new CriterionClause(a, Value.POSITIVE)));

        List<Clause> cs4 = Arrays.asList((Clause) new CriterionClause(a,Value.POSITIVE), ConstantClause.FALSE);
        assertThat(new AndClause(cs4).acceptVisitor(r),
                is((Clause) ConstantClause.FALSE));

        List<Clause> cs5 = Arrays.asList((Clause)
            new CriterionClause(a, Value.POSITIVE),
            new AndClause(
                Arrays.asList((Clause)
                    new CriterionClause(b,Value.POSITIVE))));
        List<Clause> cs6 = Arrays.asList((Clause)
            new CriterionClause(a, Value.POSITIVE),
            new CriterionClause(b,Value.POSITIVE));
        assertThat(new AndClause(cs5).acceptVisitor(r),
                is((Clause) new AndClause(cs6)));

    }

    @Test
    public void visitNList() {
        ClauseSimplifier r = new ClauseSimplifier();

        List<Clause> cs1 = Arrays.asList((Clause) new CriterionClause(a, Value.POSITIVE), ConstantClause.TRUE);
        assertThat(new NListClause(Relation.AT_LEAST, 1, cs1).acceptVisitor(r),
                is((Clause) ConstantClause.TRUE));

        List<Clause> cs2 = Arrays.asList((Clause)
            new CriterionClause(a, Value.POSITIVE),
            new CriterionClause(b, Value.POSITIVE),
            new CriterionClause(c, Value.NEGATIVE),
            ConstantClause.TRUE);
        List<Clause> cs3 = Arrays.asList((Clause)
            new CriterionClause(a, Value.POSITIVE),
            new CriterionClause(b, Value.POSITIVE),
            new CriterionClause(c, Value.NEGATIVE));
        assertThat(new NListClause(Relation.AT_LEAST, 3, cs2).acceptVisitor(r),
                is((Clause) new NListClause(Relation.AT_LEAST, 2, cs3)));

        List<Clause> cs4 = Arrays.asList((Clause)
            ConstantClause.TRUE,
            ConstantClause.TRUE,
            ConstantClause.FALSE);
        assertThat(new NListClause(Relation.EXACTLY, 1, cs4).acceptVisitor(r),
                is((Clause) ConstantClause.FALSE));
        assertThat(new NListClause(Relation.EXACTLY, 2, cs4).acceptVisitor(r),
                is((Clause) ConstantClause.TRUE));
        assertThat(new NListClause(Relation.EXACTLY, 3, cs4).acceptVisitor(r),
                is((Clause) ConstantClause.FALSE));

        assertThat(new NListClause(Relation.AT_LEAST, 0, cs2).acceptVisitor(r),
                is((Clause) ConstantClause.TRUE));
        
        List<Clause> cs5 = Arrays.asList((Clause)
                new CriterionClause(a, Value.POSITIVE),
                new CriterionClause(b, Value.POSITIVE),
                new CriterionClause(c, Value.NEGATIVE));
        assertThat("Conjunctions are detected in: n <= c_1, ..., c_n",
                new NListClause(Relation.AT_LEAST, 3, cs5).acceptVisitor(r),
                is((Clause) new AndClause(cs5)));
        assertThat("Conjunctions are detected in: n == c_1, ..., c_n",
                new NListClause(Relation.EXACTLY, 3, cs5).acceptVisitor(r),
                is((Clause) new AndClause(cs5)));
        assertThat("Universals are detected in: n >= c_1, ..., c_n",
                new NListClause(Relation.AT_MOST, 3, cs5).acceptVisitor(r),
                is((Clause) ConstantClause.TRUE));
    }
}

