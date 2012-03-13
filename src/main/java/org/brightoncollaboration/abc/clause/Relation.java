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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum
public enum Relation {
	
	@XmlEnumValue("atLeast") AT_LEAST {
	    public boolean satisfies(int n, int tops, int rest) {
		      return n <= tops; // pessimistically assert rest as false
		  }
		
		  public boolean possible(int n, int rest) {
		      return n <= rest; // optimistacally assert rest as true
		  }
	},
	@XmlEnumValue("exactly") EXACTLY {
		public boolean satisfies(int n, int tops, int rest) {
			return rest == 0 && n == tops;
		}

		public boolean possible(int n, int rest) {
			return 0 <= n && n <= rest;
		}
	},
	@XmlEnumValue("atMost") AT_MOST {
		public boolean satisfies(int n, int tops, int rest) {
			return tops + rest <= n; // pessimistically assert rest as true
		}

		public boolean possible(int n, int rest) {
			return n >= 0; // optimistacally assert rest as false
		}
	};
	
	/**
	 * Decides if the relation is certainly satisfied.
	 *
	 * @param n Number of original subclauses
	 * @param tops Number of clauses of the nlist which simplified to top
	 * @param rest Number of nontrivial clauses in nlist after simplifying
	 */
	public abstract boolean satisfies(int n, int tops, int rest);
	
	/**
	 * Decides if the relation is possibly satisfied.
   * Same parameters as satisfies.
   * @param n Number of original subclauses
   * @param rest Number of nontrivial subclauses
	 */
	public abstract boolean possible(int n, int rest);
}

