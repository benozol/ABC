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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.brightoncollaboration.abc.Classification.Result;
import org.brightoncollaboration.abc.clause.Clause;
import org.brightoncollaboration.abc.clause.visitor.UnparseClause;
import org.brightoncollaboration.abc.tools.StringI18n;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class CaseDefinition {

    private final StringI18n name;

    private final Clause level1Clause;
    private final Clause level2Clause;
    private final Clause level3Clause;
    
    public CaseDefinition() {
    	this(null, null, null, null);
    }

    public CaseDefinition(StringI18n name, Clause definiteClause, Clause probableClause, Clause possibleClause) {
        this.name = name;
        this.level1Clause = definiteClause;
        this.level2Clause = probableClause;
        this.level3Clause = possibleClause;
    }

    @XmlElement
    public StringI18n getName() {
        return name;
    }

    public Clause getLevel1Clause() {
        return level1Clause;
    }

    public Clause getLevel2Clause() {
        return level2Clause;
    }

    public Clause getLevel3Clause() {
        return level3Clause;
    }
    
    
//    @XmlElementRef @XmlElementWrapper
//    public Collection<Clause> getLevelClauses() {
//    	Collection<Clause> res = new LinkedList<Clause>();
//    	res.add(getLevel1Clause());
//    	res.add(getLevel2Clause());
//    	res.add(getLevel3Clause());
//    	return res;
//    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass() == getClass()) {
            CaseDefinition cd = (CaseDefinition) obj;
            return name.equals(cd.name) &&
                level1Clause.equals(cd.level1Clause) &&
                level2Clause.equals(cd.level2Clause) &&
                level3Clause.equals(cd.level3Clause);
        }
        return false;
    }
    
    public Classification assignLevel(Event event) {
        try {
            Classification.Result result;
            Clause precondition1 = level1Clause.precondition(event);
            Clause precondition2 = level2Clause.precondition(event);
            Clause precondition3 = level3Clause.precondition(event);
            if (precondition1.isTrue())
                result = Result.LEVEL1;
            else if (precondition2.isTrue())
                result = Result.LEVEL2;
            else if (precondition3.isTrue())
                result = Result.LEVEL3;
            else {
                boolean allFalse = precondition1.isFalse() && precondition2.isFalse() && precondition3.isFalse();
                if (!allFalse)
                    result = Result.CATEGORY4;
                else
                    result = Result.CATEGORY5;
            }
            return Classification.create(result, precondition1, precondition2, precondition3);
        } catch (OutOfMemoryError err) {
            return Classification.createError();
        } catch (org.sat4j.specs.TimeoutException err) {
            return Classification.createError();
        }
    }

    public Collection<Criterion> occurringCriteria() {
        Set<Criterion> res  = new HashSet<Criterion>();
        res.addAll(level1Clause.getOccuringCriteria());
        res.addAll(level2Clause.getOccuringCriteria());
        res.addAll(level3Clause.getOccuringCriteria());
        return res;
    }

    public String unparse() {
        StringBuffer sb = new StringBuffer();
        sb.append("CASEDEFINITION");
        for (String lang : name.getLanguages())
        	sb.append(String.format("\n  %s: \"%s\"", lang, name.translation(lang)));
        sb.append("\n");
        sb.append("LEVEL1 (\n    " + level1Clause.acceptVisitor(new UnparseClause()) + "\n)\n");
        sb.append("LEVEL2 (\n    " + level2Clause.acceptVisitor(new UnparseClause()) + "\n)\n");
        sb.append("LEVEL3 (\n    " + level3Clause.acceptVisitor(new UnparseClause()) + "\n)\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return "CaseDefinition \"" + name.translation(StringI18n.ENGLISH_LANGUAGE) + "\"";
    }
}
