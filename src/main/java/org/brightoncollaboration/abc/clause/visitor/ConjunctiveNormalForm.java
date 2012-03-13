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

import static org.brightoncollaboration.abc.clause.Clause.negateList;
import static org.brightoncollaboration.abc.clause.Clause.visitClauseList;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.brightoncollaboration.abc.clause.AndClause;
import org.brightoncollaboration.abc.clause.Clause;
import org.brightoncollaboration.abc.clause.ConstantClause;
import org.brightoncollaboration.abc.clause.CriterionClause;
import org.brightoncollaboration.abc.clause.NListClause;
import org.brightoncollaboration.abc.clause.NotClause;


/**
 * Visitor of org.brightoncollaboration.abc.clause.Clause for conversion to
 * Conjunctive Normal Form (CNF).
 *
 * The CNF of a formula is a conjunction of disjunctions of literals, i.e.
 * <code>
 *   (l11 or ... or l1n) and ... and (lm1 or ... or lmn)
 * </code>
 *
 * Most SAT solvers require a CNF of a formula.
 *
 */
public class ConjunctiveNormalForm implements Visitor<CNFConjunction> {
    
    public CNFConjunction visitConstant(ConstantClause c) {
    	if (c.getValue())
            return new CNFConjunction();
    	else
    		return CNFConjunction.singleton(new CNFClause());
    }

    public CNFConjunction visitCriterion(CriterionClause c) {
        boolean value = c.getValue().toBoolean();
        return CNFConjunction.singleton(
                CNFClause.singleton(
                    new CNFLiteral(c.getCriterion(), value)));
    }

    public CNFConjunction visitNot(NotClause c) {
        throw new Error("Clause must be in NNF before conversion to CNF");
    }



    public CNFConjunction visitAnd(AndClause c) {
        Set<CNFClause> clauses = new HashSet<CNFClause>();

        for (Clause subClause : c.getClauses())
            clauses.addAll(subClause.acceptVisitor(this).getClauses());

        return new CNFConjunction(clauses);
    }

    /** The following non-trivial conversions are documented in proto-proofs.pdf, chapter "Conjunctive Normal Form".
     *   n ≥ cs  ⇒  n ≤ cs  ⇒  1 ≤ cs = disjunction)  ⇐  n = cs
     */
    public CNFConjunction visitNList(NListClause c) {
    	switch (c.getRelation()) {
	    	case AT_LEAST:
	    		return atLeast(c.getNumber(), c.getClauses());
	    	case EXACTLY: 
	    		return exactly(c.getNumber(), c.getClauses());
	    	case AT_MOST: 
	    		return atMost(c.getNumber(), c.getClauses());
	    	default:
	    		return null; // Should be recognized as impossible by the compiler
    	}
    }
    
    /**
     * Computes the CNF of the disjunction of two conjunctions (of clauses).
     *
     * @param conj1 f_1 ∧ ... ∧ f_n
     * @param conj2 g_1 ∧ ... ∧ g_m
     * @return a set of clauses equivalent to (f_1 ∧ ... ∧ f_n) v (g_1 ∧ ... ∧ g_n)
     */
    private static CNFConjunction disjunction(CNFConjunction conj1, CNFConjunction conj2) {
    	
    	Set<CNFClause> result = new HashSet<CNFClause>();
    	
    	for (CNFClause subClause : conj1.getClauses())
    		result.addAll(disjunction(subClause, conj2).getClauses());
    	
    	return new CNFConjunction(result);
    }

    /**
     * Computes the CNF of the disjunction of a clause and a conjunction (of clauses).
     *
     * @param conj1 f
     * @param conj2 g_1 ^ ... ^ g_n
     * @return cnf(f v (g_1 ^ ... ^ g_n)) == (f v g_1) ^ .... ^ (f v g_1)
     */
    private static CNFConjunction disjunction(CNFClause clause, CNFConjunction conjunction) {

        Set<CNFClause> result = new HashSet<CNFClause>();

        for (CNFClause subClause : conjunction.getClauses()) {

            // compute  the clause (f v g_i) (i.e. a set of literals)
            Set<CNFLiteral> literals = new HashSet<CNFLiteral>();
            literals.addAll(clause.getLiterals());
            literals.addAll(subClause.getLiterals());

            result.add(new CNFClause(literals));
        }

        return new CNFConjunction(result);
    }
    
    private CNFConjunction disjunction(List<Clause> clauses) {
        CNFConjunction sofar = CNFConjunction.singleton(new CNFClause());
        for (Clause subClause : clauses)
            sofar = disjunction(sofar, subClause.acceptVisitor(this));
        return sofar;
    	
    }
    
    private CNFConjunction atLeast(int number, List<Clause> clauses) {
        List<List<Clause>> subClauses = powerSetN(number, clauses);
        List<Clause> andClauses = new LinkedList<Clause>();
        for (List<Clause> subClause : subClauses)
            andClauses.add(new AndClause(subClause));
        return disjunction(andClauses);
    }
    
    private CNFConjunction atMost(int number, List<Clause> clauses) {
        int num = clauses.size() - number;
        List<Clause> subClauses = visitClauseList(new NegationNormalForm(), negateList(clauses));
        return atLeast(num, subClauses);
    }
    
    private CNFConjunction exactly(int number, List<Clause> clauses) {
    	List<Clause> subClauses = new LinkedList<Clause>();
    	for (List<Clause> psi : powerSetN(number, clauses)) {
    		
    		List<Clause> phiMinusPsi = new LinkedList<Clause>(clauses);
    		phiMinusPsi.removeAll(psi);
    		
    		List<Clause> conjunction = new LinkedList<Clause>();
    		conjunction.addAll(psi);
    		conjunction.addAll(visitClauseList(new NegationNormalForm(), negateList(phiMinusPsi)));
    		
    		subClauses.add(new AndClause(conjunction));
    	}
    	return disjunction(subClauses);
    }

    /**
     * Compute the subsets of a a list which have a given size.
     *
     * @param number the size of the subsets
     * @param list   the list which acts as the superset
     */
    static <T> List<List<T>> powerSetN(int number, List<T> list) {
        return powerSetN(number, list.size(), list.listIterator());
    }

    private static <T> List<List<T>> powerSetN(int number, int size, ListIterator<T> iterator) {

        List<List<T>> result = new LinkedList<List<T>>();

        if (number == 1) {

            while (iterator.hasNext()) {
                List<T> singleton = new LinkedList<T>();
                singleton.add(iterator.next());
                result.add(singleton);
            }

            for (int i = 0; i < size; i++)
                iterator.previous();

        } else if (number > 1 && number <= size) {
            assert (iterator.hasNext());

            T first = iterator.next();

            List<List<T>> missingOnes = powerSetN(number - 1, size - 1, iterator);
            for (List<T> list : missingOnes)
                list.add(0, first);
            result.addAll(missingOnes);

            result.addAll(powerSetN(number, size - 1, iterator));

            iterator.previous();
        }

        return result;
    }
}
