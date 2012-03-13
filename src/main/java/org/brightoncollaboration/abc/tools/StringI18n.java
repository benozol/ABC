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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
@XmlJavaTypeAdapter(StringI18nAdapter.class)
public class StringI18n extends I18n<String> {

    public StringI18n() {
        this(null);
    }

    public StringI18n(Map<String, String> translations) {
        this.translations = translations;
    }

    public static StringI18n singleton(String content) {
        return singleton(DEFAULT_LANGUAGE, content);
    }

    public static StringI18n singleton(String lang, String content) {
        return new StringI18n(Collections.singletonMap(lang, content));
    }
}

class StringI18nAdapter extends XmlAdapter<TranslationList, StringI18n> {

    @Override
    public TranslationList marshal(StringI18n str) {
        if (str == null)
            return null;
        else {
            List<Translation> translations = new LinkedList<Translation>();
            for (String lang : str.getLanguages())
                translations.add(new Translation(lang, str.translation(lang)));
            return new TranslationList(translations);
        }
    }

    @Override
    public StringI18n unmarshal(TranslationList list) {
        if (list == null)
            return null;
        else {
            Map<String, String> res = new HashMap<String, String>();
            for (Translation translation : list.translations)
                res.put(translation.lang, translation.content);
            return new StringI18n(res);
        }
    }
}

@XmlRootElement
class TranslationList {

    @XmlElementRef
    List<Translation> translations;

    TranslationList() {
        this(null);
    }

    TranslationList(List<Translation> ts) {
        translations = ts;
    };
}

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
class Translation {

    String lang;
    String content;

    Translation() {
        this(null, null);
    }

    Translation(String lang, String content) {
        this.lang = lang;
        this.content = content;
    }
}
