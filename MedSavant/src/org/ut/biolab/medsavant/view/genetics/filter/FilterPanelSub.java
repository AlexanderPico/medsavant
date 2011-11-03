/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;

import org.ut.biolab.medsavant.api.MedSavantFilterPlugin;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.format.CustomField.Category;
import org.ut.biolab.medsavant.db.format.AnnotationFormat;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.util.DBUtil.FieldType;
import org.ut.biolab.medsavant.plugin.MedSavantPlugin;
import org.ut.biolab.medsavant.plugin.PluginController;
import org.ut.biolab.medsavant.plugin.PluginDescriptor;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public final class FilterPanelSub extends JPanel{
    
    private int id;
    private JPanel contentPanel;
    private List<FilterPanelSubItem> subItems = new ArrayList<FilterPanelSubItem>();   
    private FilterPanel parent;
    private boolean isRemoved = false;
    
    private static Color BAR_COLOUR = Color.black;
    private static Color BUTTON_OVER_COLOUR = Color.gray;
    
    public FilterPanelSub(final FilterPanel parent, int id){
        
        this.id = id;
        this.parent = parent;
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));        
        setBorder(ViewUtil.getTinyLineBorder());
        
        //title bar
        JPanel titlePanel = ViewUtil.getPrimaryBannerPanel();//new JPanel();
        
        //titlePanel.setBackground(BAR_COLOUR);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
        titlePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                contentPanel.setVisible(!contentPanel.isVisible());
                contentPanel.invalidate();
            }
        });
        titlePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel titleLabel = new JLabel("Filter Set " + id);
        titleLabel.setForeground(Color.white);
        titlePanel.add(Box.createRigidArea(new Dimension(10,20)));
        titlePanel.add(titleLabel);  
        
        titlePanel.add(Box.createHorizontalGlue());     
        
        final JLabel removeLabel = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.REMOVE));
        removeLabel.setBackground(Color.RED);
        removeLabel.setToolTipText("Remove sub query and all contained filters");
        removeLabel.addMouseListener(new MouseListener() {
            
            public void mouseClicked(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}

            public void mouseReleased(MouseEvent e) {
                removeThis();
            }

            public void mouseEntered(MouseEvent e) {
                removeLabel.setBackground(BUTTON_OVER_COLOUR);
            }

            public void mouseExited(MouseEvent e) {
                removeLabel.setBackground(BAR_COLOUR);
            }
        });
        titlePanel.add(removeLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(10,20)));
        
        this.add(titlePanel);

        
        //content pane
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createLineBorder(contentPanel.getBackground(), 6));
        //contentPanel.add(Box.createRigidArea(new Dimension(100,100)));
        this.add(contentPanel);
        
        
        refreshSubItems();

    }
    
    private JPanel createAndLabel(){
        JPanel p = new JPanel();
        p.setMaximumSize(new Dimension(10000,40));
        p.setLayout(new BorderLayout());
        
        JLabel label = new JLabel("AND");
        label.setHorizontalAlignment(JLabel.CENTER);
        //label.setBorder(BorderFactory.createLineBorder(label.getBackground(), 8));
        //label.setFont(ViewUtil.getMediumTitleFont());
        
        p.add(label, BorderLayout.CENTER);
        return p;
    }
    
    public void refreshSubItems(){
        contentPanel.removeAll();
        
        //check for removed items
        for(int i = subItems.size()-1; i >= 0; i--){
            if(subItems.get(i).isRemoved()){
                subItems.remove(i);
            }
        }
        
        //refresh panel
        for(int i = 0; i < subItems.size(); i++){
            this.contentPanel.add(subItems.get(i));
            contentPanel.add(Box.createRigidArea(new Dimension(5,5)));
        }
        
        JPanel addFilterPanel = new JPanel();
        ViewUtil.applyHorizontalBoxLayout(addFilterPanel);
        final JLabel addLabel = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ADD));
        addLabel.setToolTipText("Add new filter");
        addLabel.addMouseListener(new MouseListener() {
            
            public void mouseClicked(MouseEvent e) {}
            public void mousePressed(MouseEvent e) {}

            public void mouseReleased(MouseEvent e) {

                Map<Category, List<FilterPlaceholder>> map = getRemainingFilters();
                
                final JPopupMenu p = new JPopupMenu();                  
                p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black), BorderFactory.createLineBorder(Color.white, 5)));
                p.setBackground(Color.white);
                
                Category[] cats = new Category[map.size()];
                cats = map.keySet().toArray(cats);
                Arrays.sort(cats, new CategoryComparator());
          
                final Map<JPanel, List<Component>> menuMap = new HashMap<JPanel, List<Component>>();
                
                for(Category c : cats){
                    
                    final JPanel header = new JPanel();
                    header.setBackground(Color.white);
                    header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
                    JLabel label = new JLabel(" " + CustomField.categoryToString(c));
                    header.add(label);
                    header.add(Box.createHorizontalGlue()); 
                    header.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    label.setFont(ViewUtil.getMediumTitleFont());
                    header.setPreferredSize(new Dimension(200,20));
                    header.addMouseListener(new MouseAdapter() {
                        public void mouseReleased(MouseEvent e){
                            for(Object key : menuMap.keySet()){
                                for(Component comp : menuMap.get(key)){
                                    comp.setVisible(false);
                                }
                            }
                            for(Component comp : menuMap.get(header)){
                                comp.setVisible(true);
                            }
                            p.validate();
                            p.pack();
                            p.repaint();
                        }
                        public void mouseEntered(MouseEvent e) {
                            header.setBackground(new Color(0.9f, 0.9f, 0.9f));
                        }
                        public void mouseExited(MouseEvent e) {
                            header.setBackground(Color.white);
                        }
                    });
                    menuMap.put(header, new ArrayList<Component>());
                    p.add(header);                    
                    
                    FilterPlaceholder[] filters = new FilterPlaceholder[map.get(c).size()];
                    filters = map.get(c).toArray(filters);
                    Arrays.sort(filters, new FilterComparator());
                    
                    for(final FilterPlaceholder filter : filters){
                        
                        final JPanel item = new JPanel();
                        item.setPreferredSize(new Dimension(200,20));
                        item.setBackground(Color.white);
                        item.setLayout(new BoxLayout(item, BoxLayout.X_AXIS));
                        JLabel itemLabel = new JLabel("     " + filter.getFilterName());
                        item.add(itemLabel);
                        item.add(Box.createHorizontalGlue());                    
                        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        item.addMouseListener(new MouseAdapter() {                        
                            @Override
                            public void mouseReleased(MouseEvent e) {
                                subItems.add(new FilterPanelSubItem(filter.getFilterView(), FilterPanelSub.this, filter.getFilterID()));
                                refreshSubItems();
                                p.setVisible(false);
                            }
                            public void mouseEntered(MouseEvent e) {
                                item.setBackground(new Color(0.9f, 0.9f, 0.9f));
                            }
                            public void mouseExited(MouseEvent e) {
                                item.setBackground(Color.white);
                            }
                        });
                        menuMap.get(header).add(item);
                        item.setVisible(false);
                        p.add(item);
                    }
                    
                    if(filters.length == 0){
                        JPanel item = new JPanel();
                        item.setPreferredSize(new Dimension(150,20));
                        item.setBackground(Color.white);
                        item.setLayout(new BoxLayout(item, BoxLayout.X_AXIS));
                        JLabel empty = new JLabel("     (No filters)");
                        empty.setFont(ViewUtil.getSmallTitleFont());                       
                        item.setVisible(false);     
                        item.add(empty);
                        item.add(Box.createHorizontalGlue());
                        p.add(item);
                        menuMap.get(header).add(item);
                    }  
                }
                
                JPanel ppp = new JPanel();
                ppp.setBackground(Color.white);
                ppp.setPreferredSize(new Dimension(1,1));
                p.add(ppp);
                
                p.show(addLabel, 0, 20);
                
            }

            public void mouseEntered(MouseEvent e) {
                addLabel.setBackground(BUTTON_OVER_COLOUR);
            }

            public void mouseExited(MouseEvent e) {
                addLabel.setBackground(BAR_COLOUR);
            }
        });
        
        JPanel tmp1 = ViewUtil.getSecondaryBannerPanel();//ViewUtil.getClearPanel();
        //ViewUtil.applyHorizontalBoxLayout(tmp1);
        tmp1.setBorder(BorderFactory.createCompoundBorder(
               ViewUtil.getTinyLineBorder(),ViewUtil.getMediumBorder()));
        tmp1.add(addLabel);
        tmp1.add(Box.createRigidArea(new Dimension(5,20)));
        tmp1.add(new JLabel("Add filter"));        
        tmp1.add(Box.createHorizontalGlue()); 
        contentPanel.add(tmp1);
        
        this.updateUI();
    }
    
    private void removeThis(){
        for(int i = subItems.size()-1; i >= 0; i--){
            subItems.get(i).removeThis();
        }
        isRemoved = true;
        parent.refreshSubPanels();
    }
    
    public boolean isRemoved(){
        return isRemoved;
    }
    
    public void addNewSubItem(FilterView view, String filterId){
        subItems.add(new FilterPanelSubItem(view, this, filterId));
        refreshSubItems();
    }
    
    public boolean hasSubItem(String filterId){
        for(FilterPanelSubItem f : subItems){
            if(f.getFilterId().equals(filterId)){
                return true;
            }
        }
        return false;
    }
    
    public int getId(){
        return id;
    }
    
    private Map<Category, List<FilterPlaceholder>> getRemainingFilters(){
                
        Map<Category, List<FilterPlaceholder>> map = new EnumMap<Category, List<FilterPlaceholder>>(Category.class);
                
        map.put(Category.PATIENT, new ArrayList<FilterPlaceholder>());
        map.put(Category.GENOME_COORDS, new ArrayList<FilterPlaceholder>());
        //map.put(Category.GENOTYPE, new ArrayList<FilterPlaceholder>());
        map.put(Category.PHENOTYPE, new ArrayList<FilterPlaceholder>());
        map.put(Category.PLUGIN, new ArrayList<FilterPlaceholder>()); 

        //cohort filter
        if(!hasSubItem(CohortFilterView.FILTER_ID)){
            map.get(Category.PATIENT).add(new FilterPlaceholder() {
                public FilterView getFilterView() { return CohortFilterView.getCohortFilterView(id);}
                public String getFilterID() { return CohortFilterView.FILTER_ID;}
                public String getFilterName() { return CohortFilterView.FILTER_NAME;}
            });  
        }

        //gene list filter
        if(!hasSubItem(GeneListFilterView.FILTER_ID)){
            map.get(Category.GENOME_COORDS).add(new FilterPlaceholder() {
                public FilterView getFilterView() { return GeneListFilterView.getFilterView(id);}
                public String getFilterID() { return GeneListFilterView.FILTER_ID;}
                public String getFilterName() { return GeneListFilterView.FILTER_NAME;}
            });
        }
        
        //plugin filters
        PluginController pc = PluginController.getInstance();
        for (PluginDescriptor desc: pc.getDescriptors()) {
            final MedSavantPlugin p = pc.getPlugin(desc.getID());
            if (p instanceof MedSavantFilterPlugin && !hasSubItem(p.getDescriptor().getID())) {
                map.get(Category.PLUGIN).add(new FilterPlaceholder() {
                    public FilterView getFilterView() { return PluginFilterView.getFilterView((MedSavantFilterPlugin)p, id);}
                    public String getFilterID() { return p.getDescriptor().getID();}
                    public String getFilterName() { return p.getTitle();}
                });
            }
        }

        //add from variant table
        AnnotationFormat[] afs = ProjectController.getInstance().getCurrentAnnotationFormats();
        for(AnnotationFormat af : afs){
            for(final CustomField field : af.getCustomFields()){
                if(field.isFilterable() && !hasSubItem(field.getColumnName()) && isFilterable(field.getFieldType())){
                    map.get(field.getCategory()).add(new FilterPlaceholder() {

                        public FilterView getFilterView() {
                            try {
                                switch(field.getFieldType()){
                                    case INT:
                                        return NumericFilterView.createVariantFilterView(ProjectController.getInstance().getCurrentTableName(), field.getColumnName(), id, field.getAlias(), false);
                                    case FLOAT:
                                    case DECIMAL:
                                        return NumericFilterView.createVariantFilterView(ProjectController.getInstance().getCurrentTableName(), field.getColumnName(), id, field.getAlias(), true);
                                    case BOOLEAN:
                                        return BooleanFilterView.createVariantFilterView(field.getColumnName(), id, field.getAlias());
                                    case VARCHAR:                                 
                                    default:
                                        return StringListFilterView.createVariantFilterView(ProjectController.getInstance().getCurrentTableName(), field.getColumnName(), id, field.getAlias());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                return null;
                            }
                        }

                        public String getFilterID() {
                            return field.getColumnName();
                        }

                        public String getFilterName() {
                            return field.getAlias();
                        }
                    });
                }
            }
        }
        
        //add from patient table
        for(final CustomField field : ProjectController.getInstance().getCurrentPatientFormat()){
            if(field.isFilterable() && !hasSubItem(field.getColumnName()) && isFilterable(field.getFieldType())){
                map.get(field.getCategory()).add(new FilterPlaceholder() {

                    public FilterView getFilterView() {
                        try {
                            switch(field.getFieldType()){
                                case INT:
                                    return NumericFilterView.createPatientFilterView(ProjectController.getInstance().getCurrentPatientTableName(), field.getColumnName(), id, field.getAlias(), false);
                                case FLOAT:
                                case DECIMAL:
                                    return NumericFilterView.createPatientFilterView(ProjectController.getInstance().getCurrentPatientTableName(), field.getColumnName(), id, field.getAlias(), true);
                                case BOOLEAN:
                                    return BooleanFilterView.createPatientFilterView(field.getColumnName(), id, field.getAlias());
                                case VARCHAR:                                 
                                default:
                                    return StringListFilterView.createPatientFilterView(ProjectController.getInstance().getCurrentPatientTableName(), field.getColumnName(), id, field.getAlias());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }

                    public String getFilterID() {
                        return field.getColumnName();
                    }

                    public String getFilterName() {
                        return field.getAlias();
                    }
                });
            }
        }
        
        return map;
    }
    
    private boolean isFilterable(FieldType type){
        switch(type){
            case INT:
            case FLOAT:
            case DECIMAL:
            case BOOLEAN:
            case VARCHAR:  
                return true;
            default:
                return false;
        }
    }
    
    /* 
     * Use this to prevent creating all filters when generating list
     */
    abstract class FilterPlaceholder {
        public abstract FilterView getFilterView();      
        public abstract String getFilterID();
        public abstract String getFilterName();
    } 
    
    public class CategoryComparator implements Comparator<Category> {
        public int compare(Category o1, Category o2) {
            return o1.toString().compareTo(o2.toString());
        }
    }
    
    class FilterComparator implements Comparator<FilterPlaceholder> {
        public int compare(FilterPlaceholder o1, FilterPlaceholder o2) {
            return o1.getFilterName().compareTo(o2.getFilterName());
        }
    }
}