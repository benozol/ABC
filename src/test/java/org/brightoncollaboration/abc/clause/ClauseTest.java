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
import static org.brightoncollaboration.abc.clause.Relation.AT_MOST;
import static org.brightoncollaboration.abc.clause.Relation.EXACTLY;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.brightoncollaboration.abc.Criterion;
import org.brightoncollaboration.abc.Event;
import org.brightoncollaboration.abc.clause.visitor.CNFClause;
import org.brightoncollaboration.abc.clause.visitor.CNFConjunction;
import org.brightoncollaboration.abc.clause.visitor.CNFLiteral;
import org.brightoncollaboration.abc.clause.visitor.NegationNormalForm;
import org.brightoncollaboration.abc.clause.visitor.Visitor;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.sat4j.specs.TimeoutException;


@SuppressWarnings("deprecation")
class ValidNegationNormalForm implements Visitor<Boolean> {
    public Boolean visitConstant(ConstantClause c) { return true; }
    public Boolean visitCriterion(CriterionClause c) { return true; }
    public Boolean visitNot(NotClause c) { return false; }
    public Boolean visitAnd(AndClause c) { return all(c.getClauses()); }
    public Boolean visitNList(NListClause c) { return all(c.getClauses()); }
    private Boolean all(Collection<Clause> clauses) {
        for(Clause c: clauses)
            if(!c.acceptVisitor(this))
                return false;
        return true;
    }
}

public class ClauseTest {

    public static final String XSL_CLAUSE_TO_HTML = "/xsl/clause-to-xhtml.xsl";

    Criterion.Creator cc = Criterion.Creator.fromScratch();
    Criterion a = cc.getByShortName("a");
    Criterion b = cc.getByShortName("b");
    Criterion c = cc.getByShortName("c");

    @Test
    public void testToString() {

        assertThat(ConstantClause.TRUE.toString(), equalToIgnoringWhiteSpace("⊤"));
        assertThat(ConstantClause.FALSE.toString(), equalToIgnoringWhiteSpace("⊥"));

        assertThat(new CriterionClause(a, POSITIVE).toString(), equalToIgnoringWhiteSpace("a"));
        assertThat(new CriterionClause(a, NEGATIVE).toString(), equalToIgnoringWhiteSpace("!a"));
        assertThat(new CriterionClause(a, UNDEFINED).toString(), equalToIgnoringWhiteSpace("?a"));

        assertThat(new NotClause(new CriterionClause(a, POSITIVE)).toString(), equalToIgnoringWhiteSpace("¬a")); 

        assertThat(new AndClause(new LinkedList<Clause>()).toString(),
                equalToIgnoringWhiteSpace("( ∧ )")); 
        assertThat(new AndClause(Arrays.asList((Clause) new CriterionClause(a, POSITIVE))).toString(),
                equalToIgnoringWhiteSpace("(a)")); 
        assertThat(new AndClause(Arrays.asList((Clause)
                       new CriterionClause(a, POSITIVE),
                       new CriterionClause(b, POSITIVE))).toString(),
                equalToIgnoringWhiteSpace("(a ∧ b)")); 

        List<Clause> cs = Arrays.asList((Clause)
            new CriterionClause(a, POSITIVE),
            new CriterionClause(b, NEGATIVE),
            new CriterionClause(c, UNDEFINED));
        assertThat(new NListClause(Relation.EXACTLY, 2, cs).toString(),
                equalToIgnoringWhiteSpace("(2 = a, !b, ?c)"));
        assertThat(new NListClause(Relation.AT_LEAST, 2, cs).toString(),
                equalToIgnoringWhiteSpace("(2 ≤ a, !b, ?c)"));
        assertThat(new NListClause(Relation.AT_MOST, 2, cs).toString(),
                equalToIgnoringWhiteSpace("(2 ≥ a, !b, ?c)"));
    }

    private String clauseToHtml(Clause clause) throws Exception {
        JAXBContext cxt = JAXBContext.newInstance(
                Criterion.class, ConstantClause.class, CriterionClause.class,
                NotClause.class, AndClause.class, NListClause.class);
        StringWriter writer = new StringWriter();
        JAXBSource source = new JAXBSource(cxt, clause);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer(new StreamSource(getClass().getResourceAsStream(XSL_CLAUSE_TO_HTML)));
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.setOutputProperty(OutputKeys.INDENT, "no");
        t.transform(source, new StreamResult(writer));
        return writer.toString();
    }

    @Test public void negationNormalForm() throws org.sat4j.specs.TimeoutException {
    	List<Clause> clauses = Arrays.asList(
    		tru(), fals(),
    		criterion(a, POSITIVE), criterion(b, NEGATIVE), criterion(c, POSITIVE),
    		not(tru()), not(criterion(a, POSITIVE)), not(not(criterion(a, NEGATIVE))),
    		and(criterion(a, POSITIVE), not(criterion(b,NEGATIVE))),
    		nlist(AT_LEAST, 2, criterion(a, POSITIVE), tru(), not(and(criterion(c, POSITIVE)))),
    		nlist(EXACTLY, 2, criterion(a, POSITIVE), tru(), not(and(criterion(c, POSITIVE)))),
    		nlist(AT_MOST, 2, criterion(a, POSITIVE), tru(), not(and(criterion(c, POSITIVE))))
    	);
    	
        for(Clause clause: clauses) {
            Clause nnf = clause.acceptVisitor(new NegationNormalForm());
            assertThat(nnf.acceptVisitor(new ValidNegationNormalForm()), is(true));
            assertThat(nnf, isEquivalent(clause));
        }
        
        for(Clause clause: clauses) {
        	Clause negated = not(clause);
            Clause nnf = negated.acceptVisitor(new NegationNormalForm());
            assertThat(nnf.acceptVisitor(new ValidNegationNormalForm()), is(true));
        	assertThat(nnf, isEquivalent(negated));
        }
    }

    @SuppressWarnings("unchecked")
	@Test public void occuringCriteria() {
        assertThat(tru().getOccuringCriteria().size(), is(0));
        assertThat(fals().getOccuringCriteria().size(), is(0));
        assertThat(criterion(a,POSITIVE).getOccuringCriteria(),
                equalsAsSet(is(a)));
        assertThat(criterion(a,NEGATIVE).getOccuringCriteria(),
                equalsAsSet(is(a)));
        assertThat(criterion(a,UNDEFINED).getOccuringCriteria(),
                equalsAsSet(is(a)));
        assertThat(and(criterion(a,POSITIVE), criterion(b,NEGATIVE)).getOccuringCriteria(),
                equalsAsSet(is(a), is(b)));
        assertThat(nlist(AT_LEAST, 1, criterion(a,POSITIVE), criterion(b,NEGATIVE)).getOccuringCriteria(),
                equalsAsSet(is(a), is(b)));
        assertThat(nlist(EXACTLY, 1, criterion(a,POSITIVE), criterion(b,NEGATIVE)).getOccuringCriteria(),
                equalsAsSet(is(a), is(b)));
        assertThat(nlist(AT_MOST, 1, criterion(a,POSITIVE), criterion(b,NEGATIVE)).getOccuringCriteria(),
                equalsAsSet(is(a), is(b)));
    }

    @Test public void fillEvent() {

        assertThat(ConstantClause.TRUE.fillEvent(new Event()), is((Clause)ConstantClause.TRUE));
        assertThat(ConstantClause.FALSE.fillEvent(new Event()), is((Clause)ConstantClause.FALSE));

        Clause posX = new CriterionClause(a, POSITIVE);
        assertThat(posX.fillEvent(new Event().addSymptom(a, POSITIVE)),
            is((Clause) ConstantClause.TRUE));
        assertThat(posX.fillEvent(new Event()),
            is(posX));
        assertThat(posX.fillEvent(new Event().addSymptom(a, NEGATIVE)),
            is((Clause) ConstantClause.FALSE));

        Clause negX = new CriterionClause(a, NEGATIVE);
        assertThat(negX.fillEvent(new Event().addSymptom(a, POSITIVE)),
            is((Clause) ConstantClause.FALSE));
        assertThat(negX.fillEvent(new Event()),
            is(negX));
        assertThat(negX.fillEvent(new Event().addSymptom(a, NEGATIVE)),
            is((Clause) ConstantClause.TRUE));

        Clause unkX = new CriterionClause(a, UNDEFINED);
        assertThat(unkX.fillEvent(new Event().addSymptom(a, POSITIVE)),
            is((Clause) ConstantClause.FALSE));
        assertThat(unkX.fillEvent(new Event()),
            is((Clause) ConstantClause.TRUE));
        assertThat(unkX.fillEvent(new Event().addSymptom(a, NEGATIVE)),
            is((Clause) ConstantClause.FALSE));

        Clause posY = new CriterionClause(b, POSITIVE);
        Clause posZ = new CriterionClause(c, POSITIVE);
        Event p = new Event().addSymptom(a, POSITIVE).addSymptom(b, NEGATIVE);
        assertThat(new NotClause(posX).fillEvent(p),
            is((Clause) new NotClause(ConstantClause.TRUE)));
//        assertThat(new OrClause(Arrays.asList(posX, posY, posZ)).fillEvent(p),
//            is((Clause) new OrClause(Arrays.asList(ConstantClause.TRUE, ConstantClause.FALSE, posZ))));
        assertThat(new AndClause(Arrays.asList(posX, posY, posZ)).fillEvent(p),
            is((Clause) new AndClause(Arrays.asList(ConstantClause.TRUE, ConstantClause.FALSE, posZ))));
        assertThat(new NListClause(Relation.EXACTLY, 5, Arrays.asList(posX, posY, posZ)).fillEvent(p),
            is((Clause) new NListClause(Relation.EXACTLY, 5, Arrays.asList(ConstantClause.TRUE, ConstantClause.FALSE, posZ))));
    }

    @Test public void satisfiable() throws TimeoutException {

        assertThat(tru().isSatisfiable(), is(true));
        assertThat(fals().isSatisfiable(), is(false));

        assertThat(criterion(a, POSITIVE).isSatisfiable(), is(true));
        assertThat(criterion(a, NEGATIVE).isSatisfiable(), is(true));

        assertThat(not(criterion(a, POSITIVE)).isSatisfiable(), is(true));
        assertThat(not(tru()).isSatisfiable(), is(false));
        assertThat(not(fals()).isSatisfiable(), is(true));

        assertThat(nlist(Relation.AT_LEAST, 1).isSatisfiable(), is(false));
    }

    @Test public void universal() throws TimeoutException {

        assertThat(tru().isTautology(), is(true));
        assertThat(fals().isTautology(), is(false));

        assertThat(nlist(Relation.AT_LEAST, 1, criterion(a, POSITIVE), not(criterion(a, POSITIVE)))
                   .isTautology(),
                is(true));
        assertThat(and(criterion(a, POSITIVE), not(criterion(a, POSITIVE))).isTautology(),
                is(false));
    }
    
    @Test public void precondition() throws TimeoutException {
        Event ev = new Event().addSymptom(a, POSITIVE);
        assertThat(and(criterion(a, POSITIVE), criterion(b, NEGATIVE))
                   .precondition(ev),
                is(criterion(b, NEGATIVE)));
        assertThat(not(criterion(a, POSITIVE))
                   .precondition(new Event().addSymptom(a, NEGATIVE)),
                is(tru()));
        assertThat(not(criterion(a, NEGATIVE))
                   .precondition(new Event().addSymptom(a, NEGATIVE)),
                is(fals()));
        assertThat(not(criterion(b, NEGATIVE))
                   .precondition(new Event().addSymptom(a, NEGATIVE)),
                is(criterion(b, POSITIVE)));

        // (1 ≤ a, b) ∧ ¬a
        Clause c = and(nlist(Relation.AT_LEAST, 1, criterion(a, POSITIVE), criterion(b, POSITIVE)),
                       not(criterion(a, POSITIVE)));
        Event ev2 = new Event().addSymptom(b, NEGATIVE);
        assertThat(c.precondition(ev2), is(fals()));
    }

    @Test public void toCNFConjunction() {

        assertThat(tru().toCNFConjunction(),
                is(new CNFConjunction()));
        assertThat(fals().toCNFConjunction(),
                is(CNFConjunction.singleton(new CNFClause())));

        assertThat(criterion(a, POSITIVE).toCNFConjunction(),
                is(CNFConjunction.singleton(CNFClause.singleton(new CNFLiteral(a, true)))));
        assertThat(criterion(a, NEGATIVE).toCNFConjunction(),
                is(CNFConjunction.singleton(CNFClause.singleton(new CNFLiteral(a, false)))));

        assertThat(not(criterion(a, POSITIVE)).toCNFConjunction(),
                is(criterion(a, NEGATIVE).toCNFConjunction()));
        assertThat(not(and(criterion(a, POSITIVE), criterion(b, NEGATIVE))).toCNFConjunction(),
                is(nlist(AT_LEAST, 1, criterion(a, NEGATIVE), criterion(b, POSITIVE)).toCNFConjunction()));

        assertThat(and(criterion(a, POSITIVE), criterion(b, NEGATIVE)).toCNFConjunction(),
                is(new CNFConjunction(
                        new HashSet<CNFClause>(Arrays.asList(
                                CNFClause.singleton(new CNFLiteral(a, true)),
                                CNFClause.singleton(new CNFLiteral(b, false)))))));

        assertThat(nlist(AT_LEAST, 1, criterion(a, POSITIVE), criterion(b, NEGATIVE)).toCNFConjunction(),
                is(CNFConjunction.singleton(
                        new CNFClause(new HashSet<CNFLiteral>(Arrays.asList(
                                new CNFLiteral(a, true),
                                new CNFLiteral(b, false)))))));
                    

        Clause aCrit = criterion(a, POSITIVE);
        Clause bCrit = criterion(b, POSITIVE);
        Clause cCrit = criterion(c, POSITIVE);
        Clause c1 = nlist(AT_LEAST, 2, aCrit, bCrit, cCrit);
        assertThat(c1.toCNFConjunction(),
                is(nlist(AT_LEAST, 1, and(aCrit, bCrit), and(aCrit, cCrit), and(bCrit, cCrit)).toCNFConjunction()));

        assertThat(nlist(EXACTLY, 2, aCrit, bCrit, cCrit).toCNFConjunction(),
                is(nlist(AT_LEAST, 1,
                        and(aCrit, bCrit, criterion(c, NEGATIVE)),
                        and(aCrit, criterion(b, NEGATIVE), cCrit),
                        and(criterion(a, NEGATIVE), bCrit, cCrit)).toCNFConjunction()));

        assertThat(nlist(AT_MOST, 2, aCrit, bCrit, cCrit).toCNFConjunction(),
                is(nlist(AT_LEAST, 1, not(aCrit), not(bCrit), not(cCrit)).toCNFConjunction()));
    }

    @Test public void minimize() throws TimeoutException {

        assertThat(tru().minimize(),
                is(tru()));
        assertThat(fals().minimize(),
                is(fals()));

        assertThat(not(tru()).minimize(), is(fals()));
        assertThat(not(fals()).minimize(), is(tru()));
        assertThat(not(criterion(a, POSITIVE)).minimize(), isEquivalent(criterion(a, NEGATIVE)));
        assertThat(not(criterion(a, NEGATIVE)).minimize(), isEquivalent(criterion(a, POSITIVE)));

        assertThat(and().minimize(), is(tru()));
        assertThat(and(tru(), tru()).minimize(), is(tru()));
        assertThat(and(tru(), fals()).minimize(), is(fals()));
        assertThat(and(tru(), criterion(a, POSITIVE), tru()).minimize(),
                isEquivalent(criterion(a, POSITIVE)));

        assertThat(nlist(AT_LEAST, 1).minimize(),
                is(fals()));
        assertThat(nlist(AT_LEAST, 2, criterion(a, POSITIVE), tru(), criterion(b, NEGATIVE)).minimize(),
                isEquivalent(nlist(AT_LEAST, 1, criterion(a, POSITIVE), criterion(b, NEGATIVE))));
    }

    private <T> Matcher<Collection<T>> equalsAsSet(final Matcher<T>... matchers) {
        return new TypeSafeMatcher<Collection<T>>() {
            public void describeTo(Description desc) {
                desc.appendText("equals as set ");
                for (Matcher<T> m : matchers) {
                    desc.appendText(", ");
                    m.describeTo(desc);
                }
            }
            @Override public boolean matchesSafely(Collection<T> xs) {
                return xs.size() == matchers.length
                    && hasItems(matchers).matches(xs);
            }
        };
    }

    private Matcher<Clause> isEquivalent(final Clause clause) {
        return new TypeSafeMatcher<Clause>() {
            public void describeTo(Description desc) {
                desc.appendText(String.format("is equivalent to clause %s", clause.toString()));
            }
            @Override public boolean matchesSafely(Clause other) {
                try {
                    Clause eq = nlist(Relation.EXACTLY, 1, and(clause, other), and(not(clause), not(other)));
                    return eq.isTautology();
                } catch(TimeoutException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
