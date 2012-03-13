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
import java.util.Map;

import org.apache.log4j.Logger;
import org.brightoncollaboration.abc.Criterion.Tag;

/**
 * This class provides high level and validation methods to work with the ABC
 * engine.
 */
public class AbcEngine {

    static final Logger log = Logger.getLogger(AbcEngine.class);

    private AbcData data;
    
    public AbcEngine() {
        this.data = null;
    }
    
    public AbcEngine(AbcData data) throws AbcConfigurationException {
    	data.validate();
        this.data = data;
    }

    public Criterion.Creator getCriterionCreator() {
        return data.getCriterionCreator();
    }

    public Map<String, CaseDefinition> getCaseDefinitions() {
        return data.getCaseDefinitions();
    }

    public CaseDefinition getCaseDefinition(String name) throws AbcConfigurationException {
    	if (data.getCaseDefinitions().containsKey(name))
    		return data.getCaseDefinitions().get(name);
    	else
    		return null;
    }

    // API methods

    public Collection<Criterion> getCriteria() {
        return data.getCriterionCreator().getCriteria();
    }

    public Collection<Criterion> getCriteriaOccurringInCaseDefinition(CaseDefinition caseDefinition) {
        return caseDefinition.occurringCriteria();
    }

    public static Map<Tag, Collection<Criterion>> sortListByTags(Collection<Criterion> criteria) {
        Map<Tag, Collection<Criterion>> res = new HashMap<Tag, Collection<Criterion>>();
        for (Criterion criterion: criteria) {
            for (Tag tag: criterion.getTags()) {
                if (!res.containsKey(tag))
                    res.put(tag, new LinkedList<Criterion>());
                res.get(tag).add(criterion);
            }
        }
        return res;
    }

    public Classification confirmDiagnosis(String reporterName, CaseDefinition caseDefinition, CommentedValue.Event event) throws AbcConfigurationException {
        Classification classification = caseDefinition.assignLevel(event.toEvent());
        log.debug(String.format("Confirm diagnosis: case definition %s: %s", caseDefinition.getName()
                .defaultTranslation(), classification.getResult().toInt()));
        return classification;
    }
    
}
