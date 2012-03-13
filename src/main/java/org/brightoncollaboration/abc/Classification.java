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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

import org.brightoncollaboration.abc.clause.Clause;
import org.brightoncollaboration.abc.clause.ConstantClause;

/**
 * This is the return type of a classification of an event with respect to a case definition.
 * Following rules apply to the content of precondition1, precondition2, and precondition3 with respect to the result, if it is not an ERROR:
 * <p/>
 * 1/ if result is LEVEL1 then all preconditions are null
 * <p/>
 * 2/ if result is LEVEL2 then only precondition1 is not null and a precondition to LEVEL1
 * (which is neither ConstantClause.TRUE nor ConstantClause.FALSE).
 * <p/>
 * 3/ if result is LEVEL3 then precondition1 and precondition2 are not null and preconditions to LEVEL1 and LEVEL2, respectivly,
 * (one of them is not ConstantClause.FALSE and neither is ConstantClause.TRUE)
 * <p/>
 * 4/ if result is CATEGORY4 then precondition1, precondition2, and precondition3 are not null and preconditions to the these levels.
 * (one of them is not ConstantClause.FALSE. and neither is ConstantClause.TRUE)
 * <p/>
 * 5/ if result is CATEGORY5 then all preconditions are null.
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Classification {

    @XmlEnum(String.class)
    public enum Result {
        ERROR {
			@Override
			public Integer toInt() {
				return 0;
			}
        },
        LEVEL1 {			
        	@Override
			public Integer toInt() {
        		return 1;
        	}
        },
        LEVEL2 {			
        	@Override
			public Integer toInt() {
        		return 2;
        	}
        },
        LEVEL3 {			
        	@Override
			public Integer toInt() {
        		return 3;
        	}
        },
        CATEGORY4{			
        	@Override
			public Integer toInt() {
        		return 4;
        	}
        },
        CATEGORY5{			
        	@Override
			public Integer toInt() {
        		return 5;
        	}
        };
        
        public static Result ofInt(int i) {
            if(i == ERROR.toInt())
                return ERROR;
            if(i == LEVEL1.toInt())
                return LEVEL1;
            if(i == LEVEL2.toInt())
                return LEVEL2;
            if(i == LEVEL3.toInt())
                return LEVEL3;
            if(i == CATEGORY4.toInt())
                return CATEGORY4;
            if(i == CATEGORY5.toInt())
                return CATEGORY5;
            throw new Error(String.format("Number %d is no valid Classification.Result.ofInt", i));
        }
        public abstract Integer toInt();
    }

    @XmlElement
    private final Result result;
    
    private final Clause precondition1;
    
    private final Clause precondition2;
    
    private final Clause precondition3;
    
    public Result getResult() {
    	return result;
    }

    public Clause getPrecondition1() {
        return precondition1;
    }

    public Clause getPrecondition2() {
        return precondition2;
    }

    public Clause getPrecondition3() {
        return precondition3;
    }
    
    @SuppressWarnings("unused")
    @XmlElementRef @XmlElementWrapper
    private Collection<IndexedPrecondition> getIndexedPreconditions() {
        return Arrays.asList(
                new IndexedPrecondition(1, precondition1),
                new IndexedPrecondition(2, precondition2),
                new IndexedPrecondition(3, precondition3));
    }
    
    public static Classification create(Result result, Clause precondition1, Clause precondition2, Clause precondition3) {
        return new Classification(result, precondition1, precondition2, precondition3);
    }

    public static Classification createError() {
        return new Classification(Result.ERROR, null, null, null);
    }

    @Deprecated
    public static Classification createLevel1() {
        return new Classification(Result.LEVEL1, null, null, null);
    }

    @Deprecated
    public static Classification createLevel2(Clause precondition1) {
        assert (precondition1 != ConstantClause.TRUE);    // Would be level 1
        assert (precondition1 != ConstantClause.FALSE); // Would be category 5
        return new Classification(Result.LEVEL2,
                precondition1,
                null,
                null);
    }

    @Deprecated
    public static Classification createLevel3(Clause precondition1, Clause precondition2) {
        assert (precondition1 != ConstantClause.TRUE);
        assert (precondition1 != ConstantClause.FALSE);
        assert (precondition2 != ConstantClause.TRUE);
        assert (precondition2 != ConstantClause.FALSE);
        return new Classification(Result.LEVEL3,
                precondition1,
                precondition2,
                null);
    }

    @Deprecated
    public static Classification createCategory4(Clause precondition1, Clause precondition2, Clause precondition3) {
        assert (precondition1 != ConstantClause.TRUE);
        assert (precondition1 != ConstantClause.FALSE);
        assert (precondition2 != ConstantClause.TRUE);
        assert (precondition2 != ConstantClause.FALSE);
        assert (precondition3 != ConstantClause.TRUE);
        assert (precondition3 != ConstantClause.FALSE);
        return new Classification(Result.CATEGORY4,
                precondition1,
                precondition2,
                precondition3);
    }

    @Deprecated
    public static Classification createCategory5() {
        return new Classification(Result.CATEGORY5, null, null, null);
    }

    Classification() {
    	this(null, null, null, null);
    }
    
    private Classification(Result result, Clause precondition1, Clause precondition2, Clause precondition3) {
        this.result = result;
        this.precondition1 = precondition1;
        this.precondition2 = precondition2;
        this.precondition3 = precondition3;
    }
}

@XmlRootElement
class IndexedPrecondition {
	
	@XmlElement
	public final Integer target;
	
	@XmlElementRef @XmlElementWrapper
	public final List<Criterion> criteria;
	
	@XmlElementRef
	public final Clause clause;
	
	public IndexedPrecondition() {
		this(null, null);
	}

	public IndexedPrecondition(Integer target, Clause clause) {
		this.target = target;
		this.clause = clause;
		this.criteria = new LinkedList<Criterion>(clause.getOccuringCriteria());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clause == null) ? 0 : clause.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		result = prime * result + ((criteria == null) ? 0 : criteria.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IndexedPrecondition other = (IndexedPrecondition) obj;
		if (clause == null) {
			if (other.clause != null)
				return false;
		} else if (!clause.equals(other.clause))
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		if (criteria == null) {
			if (other.criteria != null)
				return false;
		} else if (!criteria.equals(other.criteria))
			return false;
		return true;
	}
}
