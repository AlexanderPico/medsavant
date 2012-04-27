/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;

import org.ut.biolab.medsavant.db.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.NonFatalDatabaseException;
import org.ut.biolab.medsavant.listener.ProjectListener;
import org.ut.biolab.medsavant.listener.ReferenceListener;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.model.RangeFilter;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.model.event.LoginEvent;
import org.ut.biolab.medsavant.model.event.LoginListener;


/**
 *
 * @author mfiume
 */
public class FilterController {
    private static final Logger LOG = Logger.getLogger(FilterController.class.getName());
    private static final ProjectListener projectListener;
    private static final ReferenceListener referenceListener;
    private static final LoginListener logoutListener;

    private static int filterSetID = 0;

    //private static Map<Integer, Map<Integer, Map<String, Filter>>> filterMapHistory = new TreeMap<Integer, Map<Integer, Map<String, Filter>>>();
    private static Map<Integer, Map<String, Filter>> filterMap = new TreeMap<Integer, Map<String, Filter>>();
    private static List<FiltersChangedListener> listeners = new ArrayList<FiltersChangedListener>();
    private static List<FiltersChangedListener> activeListeners = new ArrayList<FiltersChangedListener>();

    private static Filter lastFilter;
    private static FilterAction lastAction;
    
    private static boolean autoCommit = true;

    static {
        projectListener = new ProjectListener() {
            @Override
            public void projectAdded(String projectName) {
            }

            @Override
            public void projectRemoved(String projectName) {
            }

            @Override
            public void projectChanged(String projectName) {
                removeAllFilters();
            }
            @Override
            public void projectTableRemoved(int projid, int refid) {
            }
        };
        ProjectController.getInstance().addProjectListener(projectListener);

        referenceListener = new ReferenceListener() {
            @Override
            public void referenceAdded(String name) {
            }

            @Override
            public void referenceRemoved(String name) {
            }

            @Override
            public void referenceChanged(String name) {
                removeAllFilters();
            }
        };
        ReferenceController.getInstance().addReferenceListener(referenceListener);


        logoutListener = new LoginListener() {
            @Override
            public void loginEvent(LoginEvent evt) {
                if (evt.getType() == LoginEvent.EventType.LOGGED_OUT) {
                    removeAllFilters();
                }
            }

        };
        LoginController.addLoginListener(logoutListener);
    }

    public static void init(){};

    public static enum FilterAction {ADDED, REMOVED, MODIFIED, REPLACED};

    public static void addFilter(Filter filter, int queryId) {
        
        if (filterMap.get(queryId) == null) {
            filterMap.put(queryId, new TreeMap<String, Filter>());
        }
        Filter prev = filterMap.get(queryId).put(filter.getId(), filter);

        if (prev == null) {
            setLastFilter(filter, FilterAction.ADDED);
        } else {
            setLastFilter(filter, FilterAction.MODIFIED);
        }
        fireFiltersChangedEvent();
    }

    public static void removeFilter(String filterId, int queryId) {

        if (filterMap.get(queryId) == null) return; //filter was never actually added

        Filter removed = filterMap.get(queryId).remove(filterId);
        if (filterMap.get(queryId).isEmpty()) {
            filterMap.remove(queryId);
        }

        if (removed == null) return; //something went wrong, but ignore it
        setLastFilter(removed, FilterAction.REMOVED);
        fireFiltersChangedEvent();
    }
    
    public static void removeFilterSet(int queryId) {
        Map<String, Filter> map = filterMap.remove(queryId);
        if (map == null ||map.isEmpty()) return;
        Filter f = new QueryFilter() {
            @Override
            public String getName() {
                return "Filter Set";
            }

            @Override
            public String getId() {
                return null;
            }

            @Override
            public Condition[] getConditions() {
                return null;
            }
        };
        setLastFilter(f, FilterAction.REMOVED);
        fireFiltersChangedEvent();
    }

    public static void removeAllFilters() {
        filterMap.clear();
        //filterMapHistory.clear();
    }

    public static void addFilterListener(FiltersChangedListener l, boolean first) {
        listeners.add(0, l);
    }

    public static void addFilterListener(FiltersChangedListener l) {
        listeners.add(l);
    }

    public static void removeFilterListener(FiltersChangedListener l) {
        listeners.remove(l);
    }

    public static void addActiveFilterListener(FiltersChangedListener l) {
        activeListeners.add(l);
    }

    public synchronized static int getCurrentFilterSetID() {
        return filterSetID;
    }

    //public static Map<String,Filter> getFilterSet(int filterSetID) {
    /*public static Map<Integer, Map<String, Filter>> getFilterSet(int filterSetID) {
        return filterMapHistory.get(filterSetID);
    }*/

    public static void fireFiltersChangedEvent() {
        fireFiltersChangedEvent(false);
    }
    
    private synchronized static void fireFiltersChangedEvent(boolean force) {
        
        if (!autoCommit && !force) return;

        filterSetID++;
        //filterMapHistory.put(filterSetID,filterMap);
        
        Thread t = new Thread(){
            @Override
            public void run(){
                try {
                    ResultController.getInstance().getNumFilteredVariants();
                } catch (NonFatalDatabaseException ex) {
                    Logger.getLogger(FilterController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        t.setPriority(Thread.MAX_PRIORITY);//Make sure this thread executes immediately. Is this good enough?
        t.start();
        
        //cancel any running workers from last filter
        for (FiltersChangedListener l : activeListeners) {
            try {
                l.filtersChanged();
            } catch (Exception e) {
                LOG.log(Level.SEVERE, null, e);
            }
        }
        activeListeners.clear();

        //start new filter change
        for (FiltersChangedListener l : listeners) {
            try {
                l.filtersChanged();
            } catch (Exception e) {
                LOG.log(Level.SEVERE, null, e);
            }
        }
        
        //current view should be refreshed if it relies on filters
        //ViewController.getInstance().refreshView();
    }

    public static Filter getFilter(String title, int queryId) {
        //return filterMap.get(title);
        return filterMap.get(queryId).get(title);
    }

   /* static List<PostProcessFilter> getPostProcessFilters() {
        List<PostProcessFilter> ppfs = new ArrayList<PostProcessFilter>();
        for (Filter f : filterMap.values()) {
            if (f instanceof PostProcessFilter) {
                ppfs.add((PostProcessFilter) f);
            }
        }
        return ppfs;
    }*/

    public static List<QueryFilter> getQueryFilters(int queryId) {
        List<QueryFilter> qfs = new ArrayList<QueryFilter>();
        RangeFilter rf = new RangeFilter() {
            @Override
            public String getName() {
                return "Range Filters";
            }
            @Override
            public String getId() {
                return "range_filters";
            }
        };
        boolean hasRangeFilter = false;
        for (Filter f : filterMap.get(queryId).values()) {
            if (f instanceof RangeFilter) {
                rf.merge(((RangeFilter)f).getRangeSet());
                hasRangeFilter = true;
            } else if (f instanceof QueryFilter) {
                qfs.add((QueryFilter) f);
            }
        }
        if (hasRangeFilter) {
            qfs.add((QueryFilter)rf);
        }
        return qfs;
    }

    public static List<List<QueryFilter>> getQueryFilters() {
        List<List<QueryFilter>> qfs = new ArrayList<List<QueryFilter>>();
        for(Object key : filterMap.keySet().toArray()) {
            qfs.add(getQueryFilters((Integer)key));
        }
        return qfs;
    }

    public static Condition[] getQueryFilterConditions(int queryId) {
        List<QueryFilter> filters = prioritizeFilters(getQueryFilters(queryId));
        Condition[] conditions = new Condition[filters.size()];
        for(int i = 0; i < filters.size(); i++) {
            conditions[i] = ComboCondition.or(filters.get(i).getConditions());
        }
        return conditions;
    }
    
    private static List<QueryFilter> prioritizeFilters(List<QueryFilter> filters){
        
        List<QueryFilter> result = new ArrayList<QueryFilter>();
        addFiltersToList(filters, result, DefaultVariantTableSchema.COLUMNNAME_OF_CHROM);
        addFiltersToList(filters, result, DefaultVariantTableSchema.COLUMNNAME_OF_POSITION);
        for(QueryFilter f : filters) result.add(f); //remaining
        
        return result;
    }
    
    //add anything from filters with filterId to list
    private static void addFiltersToList(List<QueryFilter> filters, List<QueryFilter> list, String filterId){
        for(int i = filters.size()-1; i >= 0; i--){
            if (filters.get(i).getId().equals(filterId)){
                list.add(filters.remove(i));
            }
        }
    }
    
    public static Condition[][] getQueryFilterConditions() {
        Object[] keys = filterMap.keySet().toArray();
        Condition[][] conditions = new Condition[keys.length][];
        for(int i = 0; i < keys.length; i++) {
            conditions[i] = getQueryFilterConditions((Integer)keys[i]);
        }
        return conditions;
    }

    private static void setLastFilter(Filter filter, FilterAction action) {
        lastFilter = filter;
        lastAction = action;
    }

    public static Filter getLastFilter() {
        return lastFilter;
    }

    public static FilterAction getLastAction() {
        return lastAction;
    }

    public static String getLastActionString() {
        switch(lastAction) {
            case ADDED:
                return "Added";
            case REMOVED:
                return "Removed";
            case MODIFIED:
                return "Modified";
            case REPLACED:
                return "Replaced";
            default:
                return "";
        }
    }

    public static boolean hasFiltersApplied() {
        for(Integer key : filterMap.keySet()) {
            Map<String, Filter> current = filterMap.get(key);
            if (current != null && !current.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFilterActive(int queryId, String filterId) {
        return filterMap.containsKey(queryId) && filterMap.get(queryId).containsKey(filterId);
    }
    
    public static void setAutoCommit(boolean auto){
        autoCommit = auto;
    }
    
    public static void commit(final String filterName, FilterAction action){ 
        
        Filter f = new QueryFilter() {
            @Override
            public String getName() {
                return filterName;
            }

            @Override
            public String getId() {
                return null;
            }

            @Override
            public Condition[] getConditions() {
                return null;
            }
        };
        setLastFilter(f, action);
        fireFiltersChangedEvent(true);
    }
    
}