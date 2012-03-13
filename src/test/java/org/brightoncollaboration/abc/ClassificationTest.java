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

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.brightoncollaboration.abc.Classification.Result;
import org.brightoncollaboration.abc.clause.Clause;
import org.junit.Test;

public class ClassificationTest {
	
	Criterion.Creator cc = Criterion.Creator.fromScratch();
	Criterion a = cc.getByShortName("a");
	Criterion b = cc.getByShortName("b");
	Criterion c = cc.getByShortName("c");

	@Test
	public final void testGetPreconditions() {
		Clause c1 = Clause.criterion(a, Value.POSITIVE);
		Clause c2 = Clause.criterion(b, Value.POSITIVE);
		Clause c3 = Clause.criterion(c, Value.POSITIVE);
		
		final Classification error = Classification.createError();
        assertThat(error.getPrecondition1(), nullValue());
        assertThat(error.getPrecondition2(), nullValue());
        assertThat(error.getPrecondition3(), nullValue());
        
        final Classification proper = Classification.create(Result.CATEGORY4, c1, c2, c3);
        assertThat(proper.getPrecondition1(), is(c1));
        assertThat(proper.getPrecondition2(), is(c2));
        assertThat(proper.getPrecondition3(), is(c3));
	}

	@SuppressWarnings("deprecation")
    @Test
	public final void testGetLevel() {
		Clause c = Clause.criterion(a, Value.POSITIVE);
		assertThat(Classification.createError().getResult().toInt(), is(0));
		assertThat(Classification.createLevel1().getResult().toInt(), is(1));
		assertThat(Classification.createLevel2(c).getResult().toInt(), is(2));
		assertThat(Classification.createLevel3(c, c).getResult().toInt(), is(3));
		assertThat(Classification.createCategory4(c, c, c).getResult().toInt(), is(4));
		assertThat(Classification.createCategory5().getResult().toInt(), is(5));
	}

}
