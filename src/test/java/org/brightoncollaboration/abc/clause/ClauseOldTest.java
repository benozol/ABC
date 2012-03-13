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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.brightoncollaboration.abc.Criterion;
import org.brightoncollaboration.abc.Event;
import org.brightoncollaboration.abc.Value;


@org.junit.Ignore
public class ClauseOldTest {

    Criterion.Creator cc = Criterion.Creator.fromScratch();

    Criterion a;
    Criterion b;
    Criterion c;
    Criterion d;
    Criterion e;
    Criterion f;

    Clause c1;
    Clause c2;
    Clause c3;
    Clause c4;
    Clause c5;
    Clause c6;
    Clause c7;

    Clause and;
    Clause notand;
    Clause wrong;
    Clause nlna1;
    Clause x2;
    Clause nlna2b;
    Clause nlna2;
    Clause nlna3;

    List<Clause> clauses;

    @org.junit.Before public void setup() {
        a = cc.getByShortName("a");
        b = cc.getByShortName("b");
        c = cc.getByShortName("c");
        d = cc.getByShortName("d");
        e = cc.getByShortName("e");
        f = cc.getByShortName("f");

        c1 = new CriterionClause(a, Value.POSITIVE);
        c2 = new NotClause(new CriterionClause(b, Value.NEGATIVE));
        c3 = new AndClause(Arrays.asList(c1, c2));
        c4 = new NListClause(Relation.AT_LEAST, 1, Arrays.asList(new CriterionClause(e, Value.NEGATIVE), c3));
        c5 = new NListClause(Relation.AT_LEAST, 2, Arrays.asList(c1, c2, c4));
        c6 = new NListClause(Relation.EXACTLY, 2, Arrays.asList(c1, c2, c4));
        c7 = new NListClause(Relation.AT_MOST, 2, Arrays.asList(c1, c2, c4));

        and = new AndClause(Arrays.asList(new CriterionClause(a, Value.POSITIVE), ConstantClause.FALSE));
        notand = new NotClause(and);
        wrong = new NotClause(ConstantClause.FALSE);
        nlna1 = new NListClause(Relation.EXACTLY, 1, Arrays.asList(notand, new NotClause(ConstantClause.TRUE)));
        x2 = new NotClause(ConstantClause.FALSE);
        nlna2b = new NListClause(Relation.EXACTLY, 0, Arrays.asList(x2));
        nlna2 = new NListClause(Relation.EXACTLY, 1, Arrays.asList(notand, x2));
        nlna3 = new NListClause(Relation.EXACTLY, 1, Arrays.asList(notand, new CriterionClause(a, Value.POSITIVE)));

        clauses = Arrays.asList(c1, c2, c3, c4, c5, c6, c7,
                and, notand, wrong, nlna1, x2, nlna2b, nlna2, nlna3);
    }


    @org.junit.Test public void testSimplify() throws org.sat4j.specs.TimeoutException {

        assertTrue(
                new NotClause(new CriterionClause(a, Value.NEGATIVE)).simplify().minimize()
                .equals(c1));

        Clause bsimple = new CriterionClause(b, Value.POSITIVE);

        assertEquals(c2.simplify().minimize(), bsimple);

        assertEquals(
                new AndClause(Arrays.asList(c1, c2, ConstantClause.TRUE)).simplify().minimize(),
                new AndClause(Arrays.asList(c1, bsimple)));

        Clause c3t = new AndClause(Arrays.asList(c1, ConstantClause.TRUE, c2));
        Clause c3b = new AndClause(Arrays.asList(c1, ConstantClause.FALSE, c2));

        assertEquals(c3t.simplify().minimize(),
                new AndClause(Arrays.asList(c1, bsimple)));
        assertEquals(c3b.simplify().minimize(),
                ConstantClause.FALSE);
        
        Clause c4t = new NListClause(Relation.AT_LEAST, 1, Arrays.asList(new CriterionClause(e, Value.NEGATIVE), c3, ConstantClause.TRUE));
        Clause c4b = new NListClause(Relation.AT_LEAST, 1, Arrays.asList(ConstantClause.FALSE, new NotClause(new CriterionClause(e, Value.NEGATIVE)), c3));

        assertEquals(c4t.simplify().minimize(), ConstantClause.TRUE);
        assertEquals(c4b.simplify().minimize(), new NListClause(Relation.AT_LEAST, 1, Arrays.asList(new CriterionClause(e, Value.POSITIVE), c3.simplify().minimize())));

        { // check n-list
            Clause ac = new CriterionClause(a, Value.POSITIVE);
            Clause bc = new CriterionClause(b, Value.POSITIVE);
            Clause cc = new CriterionClause(c, Value.POSITIVE);
            Clause dc = new CriterionClause(d, Value.POSITIVE);

            List<Clause> atod = Arrays.asList(ac, bc, cc, dc);

            assertEquals(
                    new NListClause(Relation.AT_LEAST, 2, atod),
                    new NListClause(Relation.AT_LEAST, 3, Arrays.asList(ac, ConstantClause.TRUE, bc, cc, dc)).simplify().minimize());
            assertEquals(
                    new NListClause(Relation.AT_LEAST, 3, atod),
                    new NListClause(Relation.AT_LEAST, 3, Arrays.asList(ac, ConstantClause.FALSE, bc, cc, dc)).simplify().minimize());

            assertEquals(
                    new NListClause(Relation.EXACTLY, 2, atod),
                    new NListClause(Relation.EXACTLY, 3, Arrays.asList(ac, ConstantClause.TRUE, bc, cc, dc)).simplify().minimize());
            assertEquals(
                    new NListClause(Relation.EXACTLY, 3, atod),
                    new NListClause(Relation.EXACTLY, 3, Arrays.asList(ac, ConstantClause.FALSE, bc, cc, dc)).simplify().minimize());
            assertEquals(
                    ConstantClause.FALSE,
                    new NListClause(Relation.EXACTLY, 1, Arrays.asList((Clause)ConstantClause.FALSE)).simplify().minimize());
            assertEquals(
                    ConstantClause.TRUE,
                    new NListClause(Relation.EXACTLY, 1, Arrays.asList((Clause)ConstantClause.TRUE)).simplify().minimize());
            assertEquals(
                    ConstantClause.FALSE,
                    new NListClause(Relation.EXACTLY, 1, Arrays.asList((Clause)ConstantClause.TRUE, ConstantClause.TRUE)).simplify().minimize());
            assertEquals(
                    ConstantClause.FALSE,
                    new NListClause(Relation.EXACTLY, 1, Arrays.asList((Clause)ConstantClause.FALSE, ConstantClause.FALSE)).simplify().minimize());
            assertEquals(
                    ConstantClause.TRUE,
                    new NListClause(Relation.EXACTLY, 1, Arrays.asList((Clause)ConstantClause.TRUE, ConstantClause.FALSE)).simplify().minimize());
            assertEquals(
                    ConstantClause.FALSE,
                    new NListClause(Relation.EXACTLY, 0, Arrays.asList((Clause)ConstantClause.TRUE)).simplify().minimize());
            assertEquals(
                    ConstantClause.FALSE,
                    new NListClause(Relation.EXACTLY, -2, Arrays.asList((Clause)ConstantClause.TRUE)).simplify().minimize());

            assertEquals(
                    new NListClause(Relation.AT_MOST, 2, atod),
                    new NListClause(Relation.AT_MOST, 3, Arrays.asList(ac, ConstantClause.TRUE, bc, cc, dc)).simplify().minimize());
            assertEquals(
                    new NListClause(Relation.AT_MOST, 3, atod),
                    new NListClause(Relation.AT_MOST, 3, Arrays.asList(ac, ConstantClause.FALSE, bc, cc, dc)).simplify().minimize());
        }
             
        { // check propagation of top/bottom through clauses

            assertEquals(ConstantClause.FALSE, and.simplify().minimize());
            assertEquals(ConstantClause.TRUE, notand.simplify().minimize());
            assertEquals(ConstantClause.TRUE, wrong.simplify().minimize());
            assertEquals(ConstantClause.TRUE, nlna1.simplify().minimize());
            assertEquals(ConstantClause.TRUE, x2.simplify().minimize());
            assertEquals(ConstantClause.FALSE, nlna2b.simplify().minimize());
            assertEquals(ConstantClause.FALSE, nlna2.simplify().minimize());
            assertEquals(new CriterionClause(a, Value.NEGATIVE), nlna3.simplify().minimize());
        }
    }

    @org.junit.Test public void testPrecondition() throws org.sat4j.specs.TimeoutException {

        assertEquals(c1,
                c1.precondition(new Event()));
        assertEquals(ConstantClause.TRUE,
                c1.precondition(new Event().addSymptom(a, Value.POSITIVE)));
        assertEquals(ConstantClause.FALSE,
                c1.precondition(new Event().addSymptom(a, Value.NEGATIVE)));


        Clause d1 = new AndClause(Arrays.asList((Clause)new CriterionClause(a, Value.POSITIVE), new CriterionClause(b, Value.NEGATIVE)));
        assertEquals(new CriterionClause(b, Value.NEGATIVE),
                d1.precondition(new Event().addSymptom(a, Value.POSITIVE)));
        assertEquals(ConstantClause.FALSE,
                d1.precondition(new Event().addSymptom(a, Value.NEGATIVE)));
        assertEquals(ConstantClause.FALSE,
                d1.precondition(new Event().addSymptom(b, Value.POSITIVE)));
        assertEquals(d1,
                d1.precondition(new Event().addSymptom(c, Value.POSITIVE)));

        Clause d2 = new NListClause(Relation.AT_LEAST, 1, Arrays.asList((Clause)new CriterionClause(a, Value.POSITIVE), new CriterionClause(b, Value.NEGATIVE)));
        assertEquals(d2,
                d2.precondition(new Event()));
        assertEquals(ConstantClause.TRUE,
                d2.precondition(new Event().addSymptom(a, Value.POSITIVE).addSymptom(b, Value.NEGATIVE).addSymptom(c, Value.POSITIVE).addSymptom(f, Value.POSITIVE)));
        assertEquals(ConstantClause.TRUE,
                d2.precondition(new Event().addSymptom(a, Value.POSITIVE)));
        assertEquals(new CriterionClause(b, Value.NEGATIVE),
                d2.precondition(new Event().addSymptom(a, Value.NEGATIVE)));

        Clause d3 = new NotClause(d2);
        assertEquals(d3,
                d3.precondition(new Event()));
        assertEquals(ConstantClause.FALSE,
                d3.precondition(new Event().addSymptom(a, Value.POSITIVE).addSymptom(b, Value.NEGATIVE).addSymptom(c, Value.POSITIVE).addSymptom(f, Value.POSITIVE)));
        assertEquals(ConstantClause.FALSE,
                d3.precondition(new Event().addSymptom(a, Value.POSITIVE)));
        assertEquals(new CriterionClause(b, Value.POSITIVE),
                d3.precondition(new Event().addSymptom(a, Value.NEGATIVE)));
    }

    /**
     * x, y:    events.
     * φ, ψ:    clauses.
     * x ⊨ φ:   event x is a case of φ.
     * φ,x ⊨ ψ: an extension of x is a case of ψ iff. it is a case of φ
     * φ is given.
     * test: ∀x:  φ,x ⊨ ψ (ψ,y ⊨) ⇒ (y ⊨ φ)
     */
    private void testValidity(Clause phi) throws org.sat4j.specs.TimeoutException {
        Collection<Criterion> criteria = phi.getOccuringCriteria();

        for(Event x : new Event().extensions(criteria)) {
            Clause psi = phi.precondition(x);
            for(Event y : x.extensions(psi.getOccuringCriteria())) {
                if (psi.precondition(y).isTrue())
                    assertThat(phi.precondition(y), is(Clause.tru()));
            }
        }
    }

    /**
     *  Tests validity precondition for all known clauses.
     */
    @org.junit.Test public void testValidity() throws org.sat4j.specs.TimeoutException {
        for(Clause phi: clauses)
            testValidity(phi);
    }

    /* @org.junit.Ignore @org.junit.Test public void testIntussusceptionValidity() throws org.sat4j.specs.TimeoutException {

        List<Clause> majorIntussusception = builder.list( // there are three major criteria for intussusception
                // Evidence of intestinal obstruction
                builder.and(builder.criterion("vomitBile"),
                        builder.or(builder.criterion("absBowlSound"), // zusammen
                           builder.criterion("dilBowlLoopXR"))), // zusammen
                // Features of intestinal invagination
                builder.nlist(1,
                        builder.criterion("intussUS"),
                        builder.criterion("intussCT")),
                // Evidence of intestinal vascular compromise or venous congestion
                builder.or(builder.criterion("bloodRectPass"),
                        builder.criterion("bloodRectExam")));

        System.err.println("nlist(2, majorIntussusception)");
        testValidity(builder.nlist(2, majorIntussusception));

        System.err.println("nlist(=, 1, majorIntussusception)");
        testValidity(builder.nlist(Relation.EXACTLY, 1, majorIntussusception));

        List<Clause> minorIntussusception = builder.list(
                builder.criterion("ageLess1yr"),
                builder.criterion("abdoPain"),
                builder.criterion("vomit"),
                builder.criterion("lethargy"),
                builder.criterion("Pallor"),
                builder.criterion("shockHypovol"),
                builder.criterion("abnGasPattern"));

        System.err.println("nlist(3, minorIntussusception)");
        testValidity(builder.nlist(3, minorIntussusception));

        System.err.println("nlist(=, 1, minorIntussusception)");
        testValidity(builder.nlist(Relation.EXACTLY, 1, majorIntussusception));

        Clause intussusceptionDefault =
                builder.not(builder.criterion("bowlObstSurgNotIntus"));

        System.err.println("intussusceptionDefault");
        testValidity(intussusceptionDefault);

        // Clause intussusceptionLevel2 =
                builder.and(intussusceptionDefault,
                        builder.or(// Clinical criteria
                                builder.nlist(2, majorIntussusception),
                                builder.and(builder.nlist(Relation.EXACTLY, 1, majorIntussusception),
                                        builder.nlist(3, minorIntussusception))));

    }
    */
}


