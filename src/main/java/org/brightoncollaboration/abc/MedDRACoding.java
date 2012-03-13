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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MedDRACoding {
	
	@XmlRootElement
	public static class MedDRATerm {

	    @XmlElement
	    public String id;

	    public MedDRATerm() {
	        this(null);
	    }

	    public MedDRATerm(String id) {
	        this.id = id;
	    }
	}


    Collection<MedDRATerm> highLevelTerms;
    Collection<MedDRATerm> preferredLevelTerms;
    Collection<MedDRATerm> lowestLevelTerms;

    public MedDRACoding() {
        this(null, null, null);
    }

    public MedDRACoding(Collection<MedDRATerm> highLevelTerms, Collection<MedDRATerm> preferredLevelTerms, Collection<MedDRATerm> lowestLevelTerms) {
        this.highLevelTerms = highLevelTerms;
        this.preferredLevelTerms = preferredLevelTerms;
        this.lowestLevelTerms = lowestLevelTerms;
    }

    @XmlElementWrapper
    @XmlElementRef
    public Collection<MedDRATerm> getHighLevelTerms() {
        return highLevelTerms;
    }

    @XmlElementWrapper
    @XmlElementRef
    public Collection<MedDRATerm> getPreferredLevelTerms() {
        return preferredLevelTerms;
    }
    
    @XmlElementWrapper
    @XmlElementRef
    public Collection<MedDRATerm> getLowestLevelTerms() {
        return lowestLevelTerms;
    }
}
