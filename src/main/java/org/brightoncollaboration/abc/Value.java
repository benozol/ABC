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

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum Value {
	@XmlEnumValue("positive") POSITIVE {

	    public boolean toBoolean() {
	        return true;
	    }

	    public Value invert() {
	        return Value.NEGATIVE;
	    }

	    public int toInteger() {
	        return 1;
	    }		
	},
	@XmlEnumValue("negative") NEGATIVE {

	    public boolean toBoolean() {
	        return false;
	    }

	    public Value invert() {
	        return Value.POSITIVE;
	    }

	    public int toInteger() {
	        return 2;
	    }		
	},
	@XmlEnumValue("undefined") UNDEFINED {

	    public boolean toBoolean() {
	        throw new RuntimeException("proto.Value.Unknown.toBoolean");
	    }

	    public Value invert() {
	        throw new RuntimeException("proto.Value.Unknown.invert");
	    }

	    public int toInteger() {
	        return 3;
	    }		
	};
	
    abstract public boolean toBoolean(); // undefined for Value.UNDEFINED
    abstract public int toInteger();

    abstract public Value invert();
    
    public static Value fromBoolean(boolean value) {
        if (value)
            return POSITIVE;
        else
            return NEGATIVE;
    }

    public static Value fromInteger(int i) {
        if(i == POSITIVE.toInteger())
            return POSITIVE;
        if(i == NEGATIVE.toInteger())
            return NEGATIVE;
        if(i == UNDEFINED.toInteger())
            return UNDEFINED;
        throw new Error(String.format("Not a valid value: %d", i));
    }

    public static Map<String, Value> literals(String yesLit, String noLit, String unknownLit) {
        Map<String, Value> res = new HashMap<String, Value>();
        res.put(yesLit, POSITIVE);
        res.put(noLit, NEGATIVE);
        res.put(unknownLit, UNDEFINED);
        return res;
    }
}

