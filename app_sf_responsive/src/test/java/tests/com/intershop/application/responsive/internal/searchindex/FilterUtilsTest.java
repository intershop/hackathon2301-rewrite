package tests.com.intershop.application.responsive.internal.searchindex;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.intershop.application.responsive.internal.searchindex.FilterUtils;
import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.beehive.xcs.capi.catalog.CatalogCategory;
import com.intershop.component.search.capi.Attribute;
import com.intershop.component.search.capi.CategoryConfiguration;
import com.intershop.component.search.capi.FilterAttribute;
import com.intershop.component.search.capi.FilterAttributeBase;
import com.intershop.component.search.capi.FilterAttributeEntry;
import com.intershop.component.search.capi.FilterAttributeEntryBase;
import com.intershop.component.search.capi.FilterOptions;
import com.intershop.component.search.capi.FilterPredecessor;
import com.intershop.component.search.capi.SearchIndexConfiguration;
import com.intershop.component.search.capi.SearchIndexQuery;
import com.intershop.component.search.capi.SearchIndexQuery.AttributeCondition;
import com.intershop.component.search.capi.SearchIndexQueryUtil;

public class FilterUtilsTest
{
    
    
    @Mock
    private SearchIndexConfiguration indexConfig;
    @Mock
    CatalogCategory category;
    @Mock
    Domain categoryDomain;

    
    private Attribute a1;
    private Attribute a2;
    private Attribute a3;
    private Attribute a4;
    
    private List<Attribute> attributeList;
    
    private SearchIndexQuery query;

    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
        
        attributeList = new ArrayList<>();
        a1 = new Attribute("a1"); 
        a2 = new Attribute("a2"); 
        a3 = new Attribute("a3");
        a4 = new Attribute("a4");
        a1.setFilterOptions(new FilterOptions());
        a2.setFilterOptions(new FilterOptions());
        a3.setFilterOptions(new FilterOptions());
        attributeList.add(a1);
        attributeList.add(a2);
        attributeList.add(a3);
        attributeList.add(a4); //without FilterOptions
        Mockito.when(indexConfig.createAttributesIterator()).thenReturn(attributeList.iterator());
        Mockito.when(indexConfig.getAttributeByName(a1.getName())).thenReturn(a1); 
        Mockito.when(indexConfig.getAttributeByName(a2.getName())).thenReturn(a2); 
        Mockito.when(indexConfig.getAttributeByName(a3.getName())).thenReturn(a3); 
        Mockito.when(indexConfig.getAttributeByName(a4.getName())).thenReturn(a4); 
        
        Mockito.when(category.getName()).thenReturn("CatName");
        Mockito.when(category.getDomain()).thenReturn(categoryDomain); 
        Mockito.when(categoryDomain.getDomainName()).thenReturn("domainName");
        
        query = SearchIndexQueryUtil.createQuery("test");
        
    }
    
    @Test
    public void testRemovedDescendantFilterConditionsQuery()
    {
        query.addCondition(a2.getName(), "a2Val");
        query.addCondition(a4.getName(), "a4Val");
        SearchIndexQuery testQuery = FilterUtils.INSTANCE.getRemovedDescendantFilterConditionsQuery(query, indexConfig, "a1");
        
        Assert.assertNotNull("no predecessor condition is kept", testQuery.getCondition(a2.getName()));
        
        //make a1 predecessor of a3: a1 <- a3
        FilterPredecessor fd = new FilterPredecessor();
        fd.setName(a1.getName());;
        a3.getFilterOptions().setPredecessor(fd);

        query.addCondition(a3.getName(), "a3Val");
        
        Mockito.when(indexConfig.createAttributesIterator()).thenReturn(attributeList.iterator()); 
        testQuery = FilterUtils.INSTANCE.getRemovedDescendantFilterConditionsQuery(query, indexConfig, "a1");
        Assert.assertNotNull("no predecessor condition is still kept", testQuery.getCondition(a2.getName()));
        Assert.assertNull("predecessor condition is removed", testQuery.getCondition(a3.getName()));
        Assert.assertNotNull("condition for a4 is still there", testQuery.getCondition(a4.getName()));
        
        //make a1 <- a2 <- a3
        a3.getFilterOptions().getPredecessor().setName(a2.getName());
        FilterPredecessor fd2 = new FilterPredecessor();
        fd2.setName(a1.getName());;
        a2.getFilterOptions().setPredecessor(fd2);
        
        Mockito.when(indexConfig.createAttributesIterator()).thenReturn(attributeList.iterator());
        testQuery = FilterUtils.INSTANCE.getRemovedDescendantFilterConditionsQuery(query, indexConfig, "a1");
        Assert.assertNull("predecessor 2 condition is removed", testQuery.getCondition(a2.getName()));
        Assert.assertNull("predecessor 3 condition is removed", testQuery.getCondition(a3.getName()));
        Assert.assertNotNull("condition for a4 is still there", testQuery.getCondition(a4.getName()));
        
        //make a1 <- a2 and a1 <- a3
        a3.getFilterOptions().getPredecessor().setName(a1.getName());
        a2.getFilterOptions().getPredecessor().setName(a1.getName());;
        
        Mockito.when(indexConfig.createAttributesIterator()).thenReturn(attributeList.iterator());
        testQuery = FilterUtils.INSTANCE.getRemovedDescendantFilterConditionsQuery(query, indexConfig, "a1");
        Assert.assertNull("predecessor 2 condition is removed", testQuery.getCondition(a2.getName()));
        Assert.assertNull("predecessor 3 condition is removed", testQuery.getCondition(a3.getName()));
        Assert.assertNotNull("condition for a4 is still there", testQuery.getCondition(a4.getName()));
        
        
        //when there is still a condition in the query referencing a1 the predecessors condtions are kept
        query.addCondition(a1.getName(), "a1Val");
        
        Mockito.when(indexConfig.createAttributesIterator()).thenReturn(attributeList.iterator());
        testQuery = FilterUtils.INSTANCE.getRemovedDescendantFilterConditionsQuery(query, indexConfig, "a1");
        Assert.assertNotNull("predecessor 2 condition is not removed", testQuery.getCondition(a2.getName()));
        Assert.assertNotNull("predecessor 3 condition is not removed", testQuery.getCondition(a3.getName()));

    }

    @Test
    public void getRemovedSelectedChildCategoriesQueryTest()
    {
        final String CONTEXTCATEGORYUUID = "ContextCategoryUUID";
        
        query.addCondition(CONTEXTCATEGORYUUID, "CatID-1");
        query.addCondition(query.getCondition(CONTEXTCATEGORYUUID).
                        getExtendedCondition(AttributeCondition.OPERATORS.OR.name(), "CatID-2"));
        
        List<FilterAttributeEntry> entries = new ArrayList<>(); 
        CategoryFilterAttributeEntry entry0 = new CategoryFilterAttributeEntry(CONTEXTCATEGORYUUID, "CatID-Removed", 1, "Category Removed", true); 
        CatalogCategory removedCategory = mock(CatalogCategory.class); 
        entry0.setCatalogCategory(removedCategory);
        CategoryFilterAttributeEntry entry1 = new CategoryFilterAttributeEntry(CONTEXTCATEGORYUUID, "CatID-1", 1, "Category 1", true); 
        entry1.setCatalogCategory(mock(CatalogCategory.class));
        CategoryFilterAttributeEntry entry2 = new CategoryFilterAttributeEntry(CONTEXTCATEGORYUUID, "CatID-2", 1, "Category 2", true); 
        entry2.setCatalogCategory(mock(CatalogCategory.class));
        CategoryFilterAttributeEntry entry3 = new CategoryFilterAttributeEntry(CONTEXTCATEGORYUUID, "CatID-3", 1, "Category 3", false); 
        entry3.setCatalogCategory(mock(CatalogCategory.class));
        CategoryFilterAttributeEntry entry4 = new CategoryFilterAttributeEntry(CONTEXTCATEGORYUUID, "CatID-4", 1, "Category 4", false); 
        
        entries.add(entry0);
        entries.add(entry1);
        entries.add(entry2);
        entries.add(entry3);
        entries.add(entry4);
        
        Mockito.when(removedCategory.getUUID()).thenReturn("CatID-Removed"); 
        Mockito.when(entry0.getCatalogCategory().getUUID()).thenReturn("CatID-Removed"); 
        Mockito.when(entry1.getCatalogCategory().getUUID()).thenReturn("CatID-1"); 
        Mockito.when(entry2.getCatalogCategory().getUUID()).thenReturn("CatID-2"); 
        Mockito.when(entry3.getCatalogCategory().getUUID()).thenReturn("CatID-3"); 
        
        // Cat1 - Cat2 - CatID-Removed
        Mockito.when(removedCategory.getParent()).thenReturn(entry2.getCatalogCategory());
        Mockito.when(entry2.getCatalogCategory().getParent()).thenReturn(entry1.getCatalogCategory());

        
        FilterAttribute filter = new TestFilterAttribute(new Attribute("CategoryUUIDLevelMulti"), entries); 
        SearchIndexQuery processedQuery = FilterUtils.INSTANCE.getRemovedSelectedChildCategoriesQuery(query, filter, "CatID-Removed");
        
        Assert.assertNotNull(processedQuery.getCondition(CONTEXTCATEGORYUUID));
        Assert.assertTrue("Cat1 is no child of removed condition", processedQuery.getCondition(CONTEXTCATEGORYUUID).getValues().contains("CatID-1"));
        Assert.assertTrue("Cat2 is no child of removed condition", processedQuery.getCondition(CONTEXTCATEGORYUUID).getValues().contains("CatID-2"));
        Assert.assertFalse("Unselected Category 3 not in the processed query", processedQuery.getCondition(CONTEXTCATEGORYUUID).getValues().contains("CatID-3"));
        Assert.assertFalse("Unselected Category 4 not in the processed query", processedQuery.getCondition(CONTEXTCATEGORYUUID).getValues().contains("CatID-3"));
        
        // Cat1 - CatID-Removed - Cat2 
        Mockito.when(removedCategory.getParent()).thenReturn(entry1.getCatalogCategory());
        Mockito.when(entry1.getCatalogCategory().getParent()).thenReturn(null);
        Mockito.when(entry2.getCatalogCategory().getParent()).thenReturn(removedCategory);
        
        processedQuery = FilterUtils.INSTANCE.getRemovedSelectedChildCategoriesQuery(query, filter, "CatID-Removed");
        
        Assert.assertNotNull(processedQuery.getCondition(CONTEXTCATEGORYUUID));
        Assert.assertTrue("Cat1 is no child of removed condition", processedQuery.getCondition(CONTEXTCATEGORYUUID).getValues().contains("CatID-1"));
        Assert.assertFalse("Cat2 is a child of removed condition", processedQuery.getCondition(CONTEXTCATEGORYUUID).getValues().contains("CatID-2"));
        Assert.assertFalse("Unselected Category 3 not in the processed query", processedQuery.getCondition(CONTEXTCATEGORYUUID).getValues().contains("CatID-3"));
        Assert.assertFalse("Unselected Category 4 not in the processed query", processedQuery.getCondition(CONTEXTCATEGORYUUID).getValues().contains("CatID-3"));

        // CatID-Removed - Cat1 - Cat2 
        Mockito.when(removedCategory.getParent()).thenReturn(null);
        Mockito.when(entry1.getCatalogCategory().getParent()).thenReturn(removedCategory);
        Mockito.when(entry2.getCatalogCategory().getParent()).thenReturn(entry1.getCatalogCategory());
        
        processedQuery = FilterUtils.INSTANCE.getRemovedSelectedChildCategoriesQuery(query, filter, "CatID-Removed");

        Assert.assertNull(processedQuery.getCondition(CONTEXTCATEGORYUUID));
        
    }
    
    @Test
    public void getRemovedCategoryFilterConditionsQueryTest()
    {
        //query with a1=selected and a2=selected
        query.addCondition(a1.getName(), "a1Value");
        query.addCondition(a2.getName(), "a2Value");
        
        FilterOptions fo = new FilterOptions();
        CategoryConfiguration catConfig = new CategoryConfiguration(); 
        catConfig.setCategoryName(category.getName());
        catConfig.setDomain(category.getDomain().getDomainName());
        fo.addCategory(catConfig);
        a1.setFilterOptions(fo);
        
        //a1 is a filter that depend on a selected category
        query.addFilterGroup(a1.getName());
        //a2 is a different optionally displayed filter  
        query.addFilterGroup(a2.getName());
        
        SearchIndexQuery processedQuery = FilterUtils.INSTANCE.getRemovedCategoryFilterConditionsQuery(query, indexConfig, category, true);
        
        Assert.assertNull("the condition dependent on the category is removed", processedQuery.getCondition(a1.getName()));
        Assert.assertNotNull("the independent condition remains", processedQuery.getCondition(a2.getName()));
        
        
        //removed category has a parent not assigned to the filter options 
        CatalogCategory parentCategory = mock(CatalogCategory.class); 
        Mockito.when(parentCategory.getName()).thenReturn("ParentCatName");
        Mockito.when(parentCategory.getDomain()).thenReturn(categoryDomain); 
        Mockito.when(category.getParent()).thenReturn(parentCategory);
        
        processedQuery = FilterUtils.INSTANCE.getRemovedCategoryFilterConditionsQuery(query, indexConfig, category, false);
        
        Assert.assertNull("the condition dependent on the category is removed", processedQuery.getCondition(a1.getName()));
        Assert.assertNotNull("the independent condition remains", processedQuery.getCondition(a2.getName()));
        
        
        //parent category is also at the filter options 
        CategoryConfiguration parentCatConfig = new CategoryConfiguration(); 
        parentCatConfig.setCategoryName(parentCategory.getName());
        parentCatConfig.setDomain(parentCategory.getDomain().getDomainName());
        fo.addCategory(parentCatConfig);
        
        processedQuery = FilterUtils.INSTANCE.getRemovedCategoryFilterConditionsQuery(query, indexConfig, category, false);
        
        Assert.assertNotNull("the condition remains; parent is also in filter options", processedQuery.getCondition(a1.getName()));
        Assert.assertNotNull("the independent condition remains", processedQuery.getCondition(a2.getName())); 
    }
    
    @Test
    public void isCategoryFilterItemVisibleForSelectedFiltersTest()
    {
        query.addCondition(a1.getName(), "a1Value");
        
        boolean isVisible = FilterUtils.INSTANCE.isCategoryFilterItemVisibleForSelectedFilters(query, indexConfig, category);
        Assert.assertTrue("a query with no filter group", isVisible);
        
        query.addFilterGroup(a1.getName());
        
        isVisible = FilterUtils.INSTANCE.isCategoryFilterItemVisibleForSelectedFilters(query, indexConfig, category);
        Assert.assertTrue("a query with filter group but no category config is", isVisible);
        
        
        FilterOptions options = new FilterOptions();
        CategoryConfiguration differentCatConfig = new CategoryConfiguration(); 
        differentCatConfig.setCategoryName("differentCategory");
        differentCatConfig.setDomain(category.getDomain().getDomainName());
        options.addCategory(differentCatConfig);
        a1.setFilterOptions(options);
        
        isVisible = FilterUtils.INSTANCE.isCategoryFilterItemVisibleForSelectedFilters(query, indexConfig, category);
        Assert.assertTrue("a query with filter group and a different category configured", !isVisible);
        
        CategoryConfiguration catConfig = new CategoryConfiguration(); 
        catConfig.setCategoryName(category.getName());
        catConfig.setDomain(category.getDomain().getDomainName());
        options.addCategory(catConfig);
        
        isVisible = FilterUtils.INSTANCE.isCategoryFilterItemVisibleForSelectedFilters(query, indexConfig, category);
        Assert.assertTrue("a query with filter group and the current category configured", isVisible);
        
    }
    
    
    private static class TestFilterAttribute extends FilterAttributeBase
    {
        List<FilterAttributeEntry> entries; 
        public TestFilterAttribute(Attribute configurationAttribute, List<FilterAttributeEntry> entries)
        {
            super(configurationAttribute);
            this.entries = entries; 
        }

        @Override
        public Iterator createFilterAttributeEntryIterator()
        {
            return entries.iterator();
        }
    }
    
    
    public static class CategoryFilterAttributeEntry extends FilterAttributeEntryBase
    {

        public CategoryFilterAttributeEntry(String name, String value, int count, String displayValue, boolean selected)
        {
            super(name, value, count, displayValue, selected);
        }
        
        CatalogCategory category;

        public CatalogCategory getCatalogCategory()
        {
            return category;
        }

        public void setCatalogCategory(CatalogCategory category)
        {
            this.category = category;
        } 
        
        
    }
}
