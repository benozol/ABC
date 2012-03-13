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

package org.brightoncollaboration.abc.tools;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class I18n<T> {

    public static final String ENGLISH_LANGUAGE = "en";
    public static final String DEFAULT_LANGUAGE = ENGLISH_LANGUAGE;
    public static final String FRENCH_LANGUAGE = "fr";

    protected Map<String, T> translations;

    public I18n() {
        this(null);
    }

    public I18n(Map<String, T> translations) {
        this.translations = translations;
    }
    
    public static <T> I18n<T> singleton(T content) {
        return singleton(DEFAULT_LANGUAGE, content);
    }
    public static <T> I18n<T> singleton(String lang, T content) {
        return new I18n<T>(Collections.singletonMap(lang, content));        
    }

    public Collection<String> getLanguages() {
        return translations.keySet();
    }

    public T translation(String lang) {
        T res = translations.get(lang);
        if (res == null)
            return defaultTranslation();
        else
            return res;
    }

    public T defaultTranslation() {
        return translations.get(DEFAULT_LANGUAGE);
    }

    @Override
    public String toString() {
        return "StringI18n [translations=" + translations + "]";
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((translations == null) ? 0 : translations.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof I18n))
			return false;
		@SuppressWarnings("unchecked")
		I18n<T> other = (I18n<T>) obj;
		if (translations == null) {
			if (other.translations != null)
				return false;
		} else if (!translations.equals(other.translations))
			return false;
		return true;
	}
}
