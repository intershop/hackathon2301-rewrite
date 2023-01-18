package com.intershop.application.responsive.internal.searchindex;

import com.intershop.beehive.xcs.capi.catalog.CatalogCategory;
import com.intershop.component.search.capi.FilterAttribute;
import com.intershop.component.search.capi.SearchIndexConfiguration;
import com.intershop.component.search.capi.SearchIndexQuery;

/**
 * @deprecated Since ICM 7.10.19 use {@link com.intershop.component.search.capi.FilterUtils} instead
 */
@Deprecated
public enum FilterUtils
{
    INSTANCE;

    /**
     * returns a query with removed attribute conditions if these conditions are based upon attributes that have a
     * predecessor filter option configuration. if there are no such predecessor filter configurations the original
     * query is returned.
     * 
     * @param originalQuery the original query
     * @param indexConfig the index configuration to check for predecessor options
     * @param attributeName the attribute name to check if there is a predecessor filter condition to this attribute
     *            name
     * @return query with potential removed attribute conditions
     * @deprecated Since ICM 7.10.19 use
     *             {@link com.intershop.component.search.capi.FilterUtils#getRemovedDescendantFilterConditionsQuery(SearchIndexQuery, SearchIndexConfiguration, String)}
     *             instead
     */
    @Deprecated
    public SearchIndexQuery getRemovedDescendantFilterConditionsQuery(SearchIndexQuery originalQuery,
                    SearchIndexConfiguration indexConfig, String attributeName)
    {
        return com.intershop.component.search.capi.FilterUtils.INSTANCE
                        .getRemovedDescendantFilterConditionsQuery(originalQuery, indexConfig, attributeName);
    }

    /**
     * removes condition values from the given query that are child categories of the given removedCategoryUUID
     * 
     * @param originalQuery
     * @param filter
     * @param removedCategoryUUID
     * @return a modified query with removed condition values or the original query
     * @deprecated Since ICM 7.10.19 use
     *             {@link com.intershop.component.search.capi.FilterUtils#getRemovedSelectedChildCategoriesQuery(SearchIndexQuery, FilterAttribute, String)}
     *             instead
     */
    @Deprecated
    public SearchIndexQuery getRemovedSelectedChildCategoriesQuery(SearchIndexQuery originalQuery,
                    FilterAttribute filter, String removedCategoryUUID)
    {
        return com.intershop.component.search.capi.FilterUtils.INSTANCE
                        .getRemovedSelectedChildCategoriesQuery(originalQuery, filter, removedCategoryUUID);
    }

    /**
     * @deprecated Since ICM 7.10.19 use
     *             {@link com.intershop.component.search.capi.FilterUtils#getRemovedCategoryFilterConditionsQuery(SearchIndexQuery, SearchIndexConfiguration, CatalogCategory, boolean)}
     *             instead
     */
    @Deprecated
    public SearchIndexQuery getRemovedCategoryFilterConditionsQuery(SearchIndexQuery originalQuery,
                    SearchIndexConfiguration configuration, CatalogCategory category, boolean isSearch)
    {
        return com.intershop.component.search.capi.FilterUtils.INSTANCE
                        .getRemovedCategoryFilterConditionsQuery(originalQuery, configuration, category, isSearch);
    }

    /**
     * checks if a category filter item given by the category of this item should be displayed for the current query
     * conditions (selected filter values) and specified category specific filters that are set at filter groups of the
     * query
     * 
     * @param query the current search index query
     * @param configuration the current index configuration
     * @param category the category of the filter item
     * @return true if the filter item should be shown.
     * @deprecated Since ICM 7.10.19 use
     *             {@link com.intershop.component.search.capi.FilterUtils#isCategoryFilterItemVisibleForSelectedFilters(SearchIndexQuery, SearchIndexConfiguration, CatalogCategory)}
     *             instead
     */
    @Deprecated
    public boolean isCategoryFilterItemVisibleForSelectedFilters(SearchIndexQuery query,
                    SearchIndexConfiguration configuration, CatalogCategory category)
    {
        return com.intershop.component.search.capi.FilterUtils.INSTANCE
                        .isCategoryFilterItemVisibleForSelectedFilters(query, configuration, category);
    }
}
