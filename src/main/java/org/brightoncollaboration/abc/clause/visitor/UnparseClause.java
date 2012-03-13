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

import java.util.List;

import org.brightoncollaboration.abc.Value;
import org.brightoncollaboration.abc.clause.AndClause;
import org.brightoncollaboration.abc.clause.Clause;
import org.brightoncollaboration.abc.clause.ConstantClause;
import org.brightoncollaboration.abc.clause.CriterionClause;
import org.brightoncollaboration.abc.clause.NListClause;
import org.brightoncollaboration.abc.clause.NotClause;

/** Produces a string in the language which can be parsed with AbcParser.parseClause.
 */
class UnparseTerm implements Visitor<String> {
    
    public String visitConstant(ConstantClause c) {
    	if (c.getValue())
    		return "TRUE";
    	else
    		return "NOTAPPLICABLE";
    }

    public String visitCriterion(CriterionClause c) {
        String prefix = "";
        if( c.getValue() == Value.NEGATIVE )
            prefix = "!";
        else if( c.getValue() == Value.UNDEFINED )
            prefix = "?";
        return prefix + c.getCriterion().getShortName();
    }

    private String parenthesized(Clause c) {
        return "(" + c.acceptVisitor(new UnparseClause()) + ")";
    }

    public String visitNot(NotClause c) {
        return parenthesized(c);
    }

    public String visitAnd(AndClause c) {
        return parenthesized(c);
    }

    public String visitNList(NListClause c) {
        return parenthesized(c);
    }
}

public class UnparseClause implements Visitor<String> {
    
    public String visitConstant(ConstantClause c) {
    	return c.acceptVisitor(new UnparseTerm());
    }

    public String visitCriterion(CriterionClause c) {
        return c.acceptVisitor(new UnparseTerm());
    }

    public String visitNot(NotClause c) {
        return "NOT " + c.getClause().acceptVisitor(new UnparseTerm());
    }

    public String visitAnd(AndClause c) {
        return formatClauseList( c.getClauses(), " AND ", "(AND)" );
    }

    public String visitNList(NListClause c) {
        StringBuffer sb = new StringBuffer();
        switch (c.getRelation()) {
	        case AT_LEAST: 
	            sb.append( "ATLEAST " );
	            break;
	        case EXACTLY:
	        	sb.append( "EXACTLY " );
	            break;
	        case AT_MOST:
	        	sb.append( "ATMOST " );
	            break;
        }
        sb.append( c.getNumber() );
        sb.append( " FROM " );
        sb.append( formatClauseList(c.getClauses(), ", ", "()") );
        return sb.toString();
    }

    public String formatClauseList(List<Clause> clauses, String sep, String empty) {
        StringBuffer sb = new StringBuffer();
        if( clauses.size() == 0 )
            return empty;
        sb.append( clauses.get(0).acceptVisitor(new UnparseTerm()) );
        for( int i=1; i<clauses.size(); i++ )
            sb.append( sep + clauses.get(i).acceptVisitor(new UnparseTerm()) );
        return sb.toString();
        
    }
}

