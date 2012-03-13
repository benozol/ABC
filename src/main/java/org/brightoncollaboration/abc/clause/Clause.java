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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.brightoncollaboration.abc.Criterion;
import org.brightoncollaboration.abc.Event;
import org.brightoncollaboration.abc.Value;
import org.brightoncollaboration.abc.clause.visitor.CNFClause;
import org.brightoncollaboration.abc.clause.visitor.CNFConjunction;
import org.brightoncollaboration.abc.clause.visitor.ClauseSimplifier;
import org.brightoncollaboration.abc.clause.visitor.ConjunctiveNormalForm;
import org.brightoncollaboration.abc.clause.visitor.CriterionCollector;
import org.brightoncollaboration.abc.clause.visitor.FillEvent;
import org.brightoncollaboration.abc.clause.visitor.NegationNormalForm;
import org.brightoncollaboration.abc.clause.visitor.ToStringVisitor;
import org.brightoncollaboration.abc.clause.visitor.UnparseClause;
import org.brightoncollaboration.abc.clause.visitor.Visitor;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class Clause {

    public abstract <T> T acceptVisitor(Visitor<T> visitor);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    public boolean isTrue() {
        return equals(ConstantClause.TRUE);
    }

    public boolean isFalse() {
        return equals(ConstantClause.FALSE);
    }

    @Override
    public String toString() {
        return acceptVisitor(new ToStringVisitor());
    }

    public String unparse() {
        return acceptVisitor(new UnparseClause());
    }

    public Collection<Criterion> getOccuringCriteria() {
        return acceptVisitor(new CriterionCollector());
    }

    /** Fills this with information from event, simplifies it and minimizes it.
     *  Yields a preconditioning clause p for this on a event e such that (e ∧
     *  p |= this)
     */
    public Clause precondition(Event e) throws TimeoutException {
        return fillEvent(e).simplify().minimize();
    }

    /** Fills information from an event by substituting criteria clauses by true
     * or false depending on their value defined by the event.
     */
    Clause fillEvent(Event e) {
        return acceptVisitor(new FillEvent(e));
    }

    /** Applies local simplifications.
     */
    Clause simplify() {
        return acceptVisitor(new ClauseSimplifier());
    }

    /** Returns FALSE if this is not satisfiable and TRUE if this is universal
     *  and the unchanged clause otherwise.
     *  It is reasonable to simplify before minimize.
     */
    Clause minimize() throws TimeoutException {
        if (isTautology())   //     (|= clause)
            return ConstantClause.TRUE;
        if (isSatisfiable()) //  ∃p (p |= this)
            return this;
        else               // ¬∃p (p |= this)
            return ConstantClause.FALSE;
    }

    /** Tests whether this is satisfiable.
     */
    public boolean isSatisfiable() throws TimeoutException {

        // The following is necessary because sat4j complains about clauses without variables
        if(isTrue())
            return true;
        if(isFalse())
            return false;

        CNFConjunction cnf = toCNFConjunction();

        ISolver solver = SolverFactory.instance().defaultSolver();
        solver.setExpectedNumberOfClauses(cnf.getClauses().size());

        Map<Criterion, Integer> indices = new HashMap<Criterion, Integer>();
        int index = 1;
        for (Criterion criterion : getOccuringCriteria())
            indices.put(criterion, index++);
        solver.newVar(index);

        try {
            for (CNFClause clause : cnf.getClauses())
                solver.addClause(new VecInt(clause.toInts(indices)));
        } catch (org.sat4j.specs.ContradictionException contradiction) {
            return false;
        }

        return solver.isSatisfiable();
    }

    CNFConjunction toCNFConjunction() {
        return acceptVisitor(new NegationNormalForm())
              .acceptVisitor(new ConjunctiveNormalForm());
    }

    public boolean isTautology() throws TimeoutException {
        return !new NotClause(this).isSatisfiable();
    }
   
    public static <T> List<T> visitClauseList(Visitor<T> visitor, List<? extends Clause> clauses) {
        List<T> newClauses = new LinkedList<T>();
        for (Clause clause : clauses)
            newClauses.add(clause.acceptVisitor(visitor));
        return newClauses;
    }

    public static List<Clause> negateList(List<? extends Clause> clauses) {
        List<Clause> newClauses = new LinkedList<Clause>();
        for (Clause c : clauses)
            newClauses.add(new NotClause(c));
        return newClauses;
    }

    // static shortcuts for the construction of clauses
    public static Clause tru() { return ConstantClause.TRUE; }
    public static Clause fals() { return ConstantClause.FALSE; }
    public static Clause criterion(Criterion criterion, Value value) { return new CriterionClause(criterion, value); }
    public static Clause not(Clause c) { return new NotClause(c); }
    public static Clause and(Clause... cs) { return new AndClause(Arrays.asList(cs)); }
    public static Clause nlist(Relation rel, int n, Clause... cs) { return new NListClause(rel, n, Arrays.asList(cs)); }
}
