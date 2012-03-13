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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.brightoncollaboration.abc.Criterion.Creator;

public abstract class AbcData {
	
	public abstract Map<String, CaseDefinition> getCaseDefinitions();
	
	public abstract Criterion.Creator getCriterionCreator();
	
	void validate() throws AbcConfigurationException {
        
	    StringBuffer irrelevantMsg = new StringBuffer();
	    Collection<Criterion> irrelevant = irrelevantCriteria();
        if (!irrelevant.isEmpty()) {
            irrelevantMsg.append(String.format("there are irrelevant criteria: %s", irrelevant));
            AbcEngine.log.warn(irrelevantMsg.toString());
        }
        
        StringBuffer missingMsg = new StringBuffer();
        Collection<Criterion> missing = missingCriteria();
        if (!missing.isEmpty()) {            
            missingMsg.append("criteria are missing for the case definitions: ");
            missingMsg.append(missing.toString());
            AbcEngine.log.warn(missingMsg.toString()); 
        }
        
        if (!missing.isEmpty()) {
            StringBuffer errorMsg = new StringBuffer();
            errorMsg.append(missingMsg);            
            if (!irrelevant.isEmpty()) {
                errorMsg.append(" however there are irrelevant criteria, too: ");
                errorMsg.append(irrelevantMsg);
            }
            throw new AbcConfigurationException(errorMsg.toString());
        }
	}

    private Collection<Criterion> occurringCriteria() {
        Collection<Criterion> res = new LinkedList<Criterion>();
        for (CaseDefinition caseDefinition : getCaseDefinitions().values())
            res.addAll(caseDefinition.occurringCriteria());
        return res;
    }

    public Collection<Criterion> missingCriteria() {
        Collection<Criterion> res = new LinkedList<Criterion>();
        Collection<Criterion> occurring = occurringCriteria();
        for (Criterion criterion : occurring)
            if (getCriterionCreator() != Creator.FROM_SCRATCH && !getCriterionCreator().getCriteria().contains(criterion))
                res.add(criterion);
        return res;
    }

    public Collection<Criterion> irrelevantCriteria() {
        Collection<Criterion> res = new LinkedList<Criterion>();
        Collection<Criterion> occurring = occurringCriteria();
        for (Criterion criterion : getCriterionCreator().getCriteria())
            if (!occurring.contains(criterion))
                res.add(criterion);
        return res;
    }
    
    public static AbcData staticData(List<CaseDefinition> caseDefinitions) {
        Map<String, CaseDefinition> caseDefinitionsMap = new HashMap<String, CaseDefinition>();
        for (CaseDefinition cd : caseDefinitions)
            caseDefinitionsMap.put(nameForString(cd.getName().defaultTranslation()), cd);
        return staticData(Criterion.Creator.fromScratch(), caseDefinitionsMap);
    }

    public static AbcData staticData(final Creator criterionCreator, final Map<String, CaseDefinition> caseDefinitions) {
    	return new AbcData() {
    		
    		public Map<String, CaseDefinition> getCaseDefinitions() {
    			return caseDefinitions;
    		}

    		public Creator getCriterionCreator() {
    			return criterionCreator;
    		}
    	};
    	
    }
    
    public static String nameForString(String str) {
        return str.replaceAll("\\s+", "_").toLowerCase();
    }
}