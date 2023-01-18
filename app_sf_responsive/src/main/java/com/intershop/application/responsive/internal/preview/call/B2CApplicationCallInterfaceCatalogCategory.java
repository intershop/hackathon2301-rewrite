/*
 * B2CApplicationCallInterfaceCatalogCategory.java
 *
 * Copyright (c) 2011 Intershop Communications AG
 * All rights reserved.
 */
package com.intershop.application.responsive.internal.preview.call;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import com.intershop.beehive.core.capi.url.URLInformation;
import com.intershop.beehive.xcs.capi.catalog.CatalogCategory;
import com.intershop.component.mvc.capi.catalog.Catalog;
import com.intershop.component.mvc.capi.catalog.MVCatalogMgr;
import com.intershop.component.preview.capi.call.ApplicationCallInterface;
import com.intershop.sellside.application.b2c.capi.preview.call.B2CApplicationCallInterface;

/**
 *
 */
public class B2CApplicationCallInterfaceCatalogCategory implements ApplicationCallInterface
{

    private static final String PARAM_CATEGORY_DOMAIN_NAME = "CatalogID";
    private static final String PARAM_CATEGORY_NAME = "CategoryName";
    private static final String PIPELINE = "ViewStandardCatalog-Browse";    
    
    @Inject
    private MVCatalogMgr catalogMgr;
    
    @Override
    public String call(Object o)
    {
        return call(o, null);
    }

    @Override
    public String call(Object o, Map<String, String> params)
    {
        CatalogCategory category = CatalogCategory.class.cast(o);
        Map<String, String> catalogParams = new LinkedHashMap<String, String>();
        if (params != null) catalogParams.putAll(params);
        
        Catalog catalog = catalogMgr.getCatalogByCatalogDomain(category.getDomain());
        catalogParams.put(PARAM_CATEGORY_NAME, category.getName());
        catalogParams.put(PARAM_CATEGORY_DOMAIN_NAME, catalog.getId());
        return B2CApplicationCallInterface.createURL(PIPELINE, catalogParams);           
    }
    
    @Override
    public String resolve(String urlString)
    {
        URLInformation urlInfo = B2CApplicationCallInterface.resolveURLInformation(urlString);
        
        // check pipeline
        String pipeline = String.format("%s-%s", urlInfo.getPipeline(), urlInfo.getStartNode());
        if (PIPELINE.equals(pipeline)) return urlString;
        
        // check required parameters
        Map<String, String> parameters = urlInfo.getParameters();
        String categoryName = parameters.get(PARAM_CATEGORY_NAME);
        String catDomainName = parameters.get(PARAM_CATEGORY_DOMAIN_NAME);
        if (urlInfo.getPipeline().contains("Catalog") && categoryName != null && catDomainName != null)
        {
            Map<String, String> catalogParams = new LinkedHashMap<String, String>();
            catalogParams.put(PARAM_CATEGORY_NAME, categoryName);
            catalogParams.put(PARAM_CATEGORY_DOMAIN_NAME, catDomainName);
            return B2CApplicationCallInterface.createURL(PIPELINE, catalogParams, urlInfo);
        }
        else
        {
            return null;
        }
    }
}
