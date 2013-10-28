/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.filter;

import java.io.*;
import java.util.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.ut.biolab.medsavant.client.api.FilterStateAdapter;


/**
 *
 * @author Andrew
 */
public class FilterState implements FilterStateAdapter {

    public static final String ROOT_ELEMENT = "filters";
    public static final String SET_ELEMENT = "set";
    public static final String FILTER_ELEMENT = "filter";
    public static final String TABLE_ELEMENT = "table";
    public static final String VALUE_ELEMENT = "value";

    private final Filter.Type type;
    private final String name;
    private final String filterID;
    private final Map<String, List<String>> values = new HashMap<String, List<String>>();

    /**
     * Construct a new filter state object suitable for storage.
     *
     * @param type one of the filter-type constants
     * @param name human-friendly name for the filter
     * @param id unique identifier for the filter (often an SQL column name)
     */
    public FilterState(Filter.Type type, String name, String id) {
        this.type = type;
        this.name = name;
        this.filterID = id;
    }

    public String getName() {
        return name;
    }

    public String getFilterID() {
        return filterID;
    }

    public Filter.Type getType() {
        return type;
    }

    /**
     * Get an element which is assumed to be unique.
     */
    @Override
    public String getOneValue(String key) {
        List<String> keyValues = values.get(key);
        if (keyValues != null && keyValues.size() > 0) {
            return keyValues.get(0);
        }
        return null;
    }

    @Override
    public void putOneValue(String key, Object val) {
        values.put(key, Arrays.asList(val.toString()));
    }

    @Override
    public List<String> getValues(String key) {
        return values.get(key);
    }

    @Override
    public void putValues(String key, List<String> vals) {
        values.put(key, vals);
    }

    /**
     * Assumes that there is exactly one table element in the XML.
     */
    public String getTable() {
        return getOneValue("table");
    }

    @Override
    public String generateXML() {
        StringBuilder sb = new StringBuilder("\t\t<filter name=\"" + name + "\" id=\"" + filterID + "\" type=\"" + type + "\" >\n");
        for (String key: values.keySet()) {
            List<String> keyVals = values.get(key);
            for (String val: keyVals) {
                sb.append("\t\t\t<").append(key).append(">").append(val).append("</").append(key).append(">\n");
            }
        }
        sb.append("\t\t</filter>");
        return sb.toString();
    }

    public static List<List<FilterState>> loadFiltersFromFiles(Collection<File> files) throws XMLStreamException, FileNotFoundException {

        List<List<FilterState>> states = new ArrayList<List<FilterState>>();

        for (File f: files) {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(f));
            boolean done = false;
            List<FilterState> curSet = null;
            FilterState curFilter = null;
            String elem;
            do {
                switch (reader.next()) {
                    case XMLStreamConstants.START_ELEMENT:
                        elem = reader.getLocalName();
                        if (elem.equals(SET_ELEMENT)) {
                            curSet = new ArrayList<FilterState>();
                        } else if (elem.equals(FILTER_ELEMENT)) {
                            if (curSet == null) {
                                throw new XMLStreamException("XML error: <filter> found outside of <set> element.");
                            }
                            String name = reader.getAttributeValue(null, "name");
                            String id = reader.getAttributeValue(null, "id");
                            Filter.Type type = Filter.Type.valueOf(reader.getAttributeValue(null, "type"));
                            curFilter = new FilterState(type, name, id);
                        } else if (!elem.equals(ROOT_ELEMENT)) {
                            if (curFilter == null) {
                                throw new XMLStreamException("XML error: <" + elem + "> found outside of <filter> element.");
                            }
                            List<String> existingValues = curFilter.values.get(elem);
                            if (existingValues == null) {
                                existingValues = new ArrayList<String>();
                                curFilter.values.put(elem, existingValues);
                            }
                            existingValues.add(reader.getElementText());
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        elem = reader.getLocalName();
                        if (elem.equals(SET_ELEMENT)) {
                            states.add(curSet);
                            curSet = null;
                        } else if (elem.equals(FILTER_ELEMENT)) {
                            curSet.add(curFilter);
                            curFilter = null;
                        }
                        break;
                    case XMLStreamConstants.END_DOCUMENT:
                        reader.close();
                        done = true;
                        break;
                }
            } while (!done);
        }
        return states;
    }
}
