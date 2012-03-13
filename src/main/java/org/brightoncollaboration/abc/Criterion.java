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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;
import org.brightoncollaboration.abc.tools.I18n;
import org.brightoncollaboration.abc.tools.StringI18n;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Criterion implements Comparable<Criterion> {

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Tag {
    	
    	public static final Tag NULL_TAG = new Tag();

        private String id;
        private StringI18n name;

        public Tag() {
            this(null, null);
        }

        public Tag(StringI18n name) {
            this(name.defaultTranslation(), name);
        }

        public Tag(String id, StringI18n name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public StringI18n getName() {
            return name;
        }

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
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
			Tag other = (Tag) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}

    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Footnote {
        private final Integer position;
        private final String content;
        public Footnote() {
            this(null, null);
        }
        public Footnote(Integer position, String content) {
            this.position = position;
            this.content = content;
        }
        /** The position of the footnote within the i18n'ed wording. */
        public Integer getPosition() { return position; }
        /** The content of the footnote. */
        public String getContent() { return content; }
        public static class ComparatorOnPosition implements Comparator<Footnote> {
            public int compare(Footnote o1, Footnote o2) {
                return o1.getPosition().compareTo(o2.getPosition());
            }
        }
    }
    
    public static class Creator {
        
        protected static final Logger log = Logger.getLogger(Creator.class);
        
        protected Map<String, Criterion> criteriaByShortname = new HashMap<String, Criterion>();
        protected Map<Criterion.Tag, Collection<Criterion>> criteriaByTag = new HashMap<Criterion.Tag, Collection<Criterion>>();

        /**
         * 
         * @param criteria
         * @throws AbcConfigurationException Indicates errors initializing the CriterionCreator. This might be caused by multiple criteria
         * having the same shortName or criteria without ID.
         */
        public Creator(Collection<Criterion> criteria) throws AbcConfigurationException {
            for (Criterion criterion : criteria) {
                if (criterion.getShortName().isEmpty())
                    throw new AbcConfigurationException( String.format("Criterion without short name (wording is \"%s\")", criterion.getWording().defaultTranslation()) );
                if (criteriaByShortname.containsKey(criterion.getShortName()))
                    throw new AbcConfigurationException( String.format("Criterion short name %s not unique.", criterion.getShortName()) );
                criteriaByShortname.put(criterion.getShortName(), criterion);
                Collection<Criterion.Tag> tags;
                if (criterion.getTags().isEmpty())
                    tags = Arrays.asList(Tag.NULL_TAG);
                else
                    tags = criterion.getTags();
                for (Criterion.Tag tag: tags) {
                    if (!criteriaByTag.containsKey(tag))
                        criteriaByTag.put(tag, new HashSet<Criterion>());
                    criteriaByTag.get(tag).add(criterion);
                }
            }
        }
        
        /** Create a CriterionCreator which Creates criteria on demand.
          * They are only given by shortname which will serve as a originalName and a usecaseWording, too.
          */
        static final Creator FROM_SCRATCH;
        static {
            try {
                FROM_SCRATCH = new Creator(new LinkedList<Criterion>()) {
                    @Override
                    public Criterion getByShortName(String shortname) {
                        if(criteriaByShortname.containsKey(shortname))
                            return criteriaByShortname.get(shortname);
                        else {
                            Criterion crit = new Criterion(shortname, shortname);
                            criteriaByShortname.put(shortname, crit);
                            return crit;
                        }
                    }
                };
            } catch (AbcConfigurationException exc) {
                exc.printStackTrace();
                throw new RuntimeException(exc);
            }
        }
        
        public static Creator fromScratch() {
            return FROM_SCRATCH;
        }
        
        public Collection<Criterion> getCriteria() {
            return criteriaByShortname.values();
        }

        public Set<String> getShortNames() {
            return criteriaByShortname.keySet();
        }

        /** Retrieve a criterion by its short name (which is a short and unique
         * name for the criterion used to describe the aefis).
         */
        public Criterion getByShortName(String shortName) {
            if(criteriaByShortname.containsKey(shortName)) {
                Criterion crit = criteriaByShortname.get(shortName);
                log.debug(String.format("Found criterion %s", shortName));
                return crit;
            } else {
                log.error(String.format("Cannot find criterion by short name %s", shortName));
                return null;
            }
        }

        /** Retrieve a collection of tags which have a given tag. If the given tag is
         *  null, the result contains all criteria which do not have any tag.
         */
        public Collection<Criterion> getByTag(Criterion.Tag tag) {
            if (criteriaByTag.containsKey(tag))
                return criteriaByTag.get(tag);
    		else
                return Collections.emptySet();
        }

        /** Retrieve all tags occurring in the criteria. This can include null for
         *  all criteria which does not have a tag.
         */
        public Collection<Criterion.Tag> getTags() {
            return criteriaByTag.keySet();
        }
    }

    private final String shortName;
    
    private final StringI18n wording;
    
    @XmlElementWrapper @XmlElement(name="reference")
    private final List<StringI18n> references;
    
    @XmlElement(nillable=false)
    private final StringI18n comment;
    
    @XmlElementWrapper @XmlElementRef
    private final Collection<Tag> tags;
    
    // NOTE Each Footnote.position is monotonly increasing!
    @XmlJavaTypeAdapter(ListI18nFootnoteAdapter.class)
    private final List<I18n<Footnote>> footnotes;
    
    @XmlElement(nillable=false)
    private final MedDRACoding medDRACoding;
    
    private final Integer orderingWeight;
    
    public Criterion(
		String shortName,
        StringI18n wording,
        StringI18n comment,
        List<I18n<Footnote>> footnotes,
        List<StringI18n> references,
        Collection<Tag> tags,
        MedDRACoding medDRACoding,
        Integer orderingWeight
    ) {
        this.shortName = shortName;
        this.wording = wording;
        this.references = references;
        this.comment = comment;
        this.tags = tags;
        this.footnotes = footnotes;
        this.medDRACoding = medDRACoding;
        this.orderingWeight = orderingWeight;
    }
    
    private static List<StringI18n> stringI18nList(String[] strings) {
        List<StringI18n> res = new LinkedList<StringI18n>();
        for (String str : strings)
            res.add(StringI18n.singleton(str));
        return res;
    }
    
    private static List<Tag> tagList(String[] tags) {
        List<Tag> res = new LinkedList<Tag>();
        for (String tag : tags)
            res.add(new Tag(tag, StringI18n.singleton(tag)));
        return res;
    }
    
    private static List<I18n<Footnote>> footnotes(Footnote[] footnotes) {
    	List<I18n<Footnote>> res = new LinkedList<I18n<Footnote>>();
    	for (Footnote footnote : footnotes)
    		res.add(I18n.singleton(footnote));
    	return res;
    }
    
    public Criterion(String shortName,
            String wording,
            String comment,
            Footnote[] footnotes,
            String[] references,
            String[] tags,
            MedDRACoding medDRACoding,
            Integer orderingWeight) {
        this(shortName,
             StringI18n.singleton(wording),
             comment == null ? null : StringI18n.singleton(comment),
             footnotes(footnotes),
             stringI18nList(references),
             tagList(tags),
             medDRACoding,
             orderingWeight);
    }
    
    public Criterion(String shortName, String wording) {
        this(shortName, wording, null, new Footnote[0], new String[0], new String[0], null, null);
    }

    public Criterion() {
        this(null, null);
    }

    public String getShortName() {
        return shortName;
    }

    public StringI18n getWording() {
        return wording;
    }

    public Collection<Tag> getTags() {
        return tags;
    }

    public StringI18n getComment() {
        return comment;
    }

    public List<I18n<Footnote>> getFootnotes() {
        return footnotes;
    }

    public List<StringI18n> getReferences() {
        return references;
    }

    public MedDRACoding getMedDRACoding() {
        return medDRACoding;
    }
    
    public Integer getOrderingWeight() {
    	return orderingWeight;
    }

    public int compareTo(Criterion crit) {
        return shortName.compareTo(crit.shortName);
    }

    @Override
    public String toString() {
        return String.format("Criterion [shortName=%s]", shortName);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((shortName == null) ? 0 : shortName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Criterion))
            return false;
        Criterion other = (Criterion) obj;
        if (shortName == null) {
            if (other.shortName != null)
                return false;
        } else if (!shortName.equals(other.shortName))
            return false;
        return true;
    }
}

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
class FootnoteWithIndex {
	Integer position;
	Integer index;
	String content;
	public FootnoteWithIndex(Integer position, Integer index, String content) {
		this.position = position;
		this.index = index;
		this.content = content;
	}
	public FootnoteWithIndex() {
		this(null, null, null);
	}
}

@XmlRootElement
class FootnoteTranslation {
    @XmlElement
    String lang;
    @XmlElementRef
    Criterion.Footnote content;
    FootnoteTranslation() {
        this(null, null);
    }
    FootnoteTranslation(String lang, Criterion.Footnote content) {
        this.lang = lang;
        this.content = content;
    } 
}

@XmlRootElement
class FootnoteTranslations {
    @XmlElementRef
    List<FootnoteTranslation> translations;
    FootnoteTranslations() {
        this(null);
    }
    FootnoteTranslations(List<FootnoteTranslation> translations) {
        this.translations = translations;
    }
}

class Footnotes {
	@XmlElementRef
	List<FootnoteTranslations> footnotes;
	Footnotes(List<FootnoteTranslations> footnotes) {
		this.footnotes = footnotes;
	}
	Footnotes() {
		this(null);
	}
}

class ListI18nFootnoteAdapter extends XmlAdapter<Footnotes, List<I18n<Criterion.Footnote>>> {

    @Override
    public Footnotes marshal(List<I18n<Criterion.Footnote>> v) throws Exception {
        if (v == null)
            return null;
        List<FootnoteTranslations> footnotes = new LinkedList<FootnoteTranslations>();
        for (I18n<Criterion.Footnote> i18nFootnote : v) {
        	List<FootnoteTranslation> translations = new LinkedList<FootnoteTranslation>();
        	for (String lang : i18nFootnote.getLanguages())
        		translations.add(new FootnoteTranslation(lang, i18nFootnote.translation(lang)));
        	footnotes.add(new FootnoteTranslations(translations));
        }
        return new Footnotes(footnotes);
    }

    @Override
    public List<I18n<Criterion.Footnote>> unmarshal(Footnotes v) throws Exception {
        throw new RuntimeException("I18nFootnoteListAdapter.unmarshal not implemented");
    }
}
