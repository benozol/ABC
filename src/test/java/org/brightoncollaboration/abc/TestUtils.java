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

import org.junit.*;

import org.brightoncollaboration.abc.clause.CriterionClause;

@Ignore("Provides code for testing only") @Deprecated public class TestUtils {
    private static Criterion.Creator cc = Criterion.Creator.fromScratch();
    public static Criterion crit(String name) {
        return cc.getByShortName(name);
    }
    public static final CriterionClause yesA = new CriterionClause( crit("a"), Value.POSITIVE );
    public static final CriterionClause noA = new CriterionClause( crit("a"), Value.NEGATIVE );
    public static final CriterionClause unknownA = new CriterionClause( crit("a"), Value.UNDEFINED );
    public static final CriterionClause yesB = new CriterionClause( crit("b"), Value.POSITIVE );
    public static final CriterionClause noB = new CriterionClause( crit("b"), Value.NEGATIVE );
    public static final CriterionClause unknownB = new CriterionClause( crit("b"), Value.UNDEFINED );
    public static final CriterionClause yesC = new CriterionClause( crit("c"), Value.POSITIVE );
    public static final CriterionClause noC = new CriterionClause( crit("c"), Value.NEGATIVE );
    public static final CriterionClause unknownC = new CriterionClause( crit("c"), Value.UNDEFINED );
}

