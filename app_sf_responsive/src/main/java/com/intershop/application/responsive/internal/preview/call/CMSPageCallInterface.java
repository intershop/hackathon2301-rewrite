package com.intershop.application.responsive.internal.preview.call;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.intershop.beehive.core.capi.app.AppContextUtil;
import com.intershop.beehive.core.capi.url.URLInformation;
import com.intershop.beehive.emf.core.Parameter;
import com.intershop.component.application.capi.ApplicationBO;
import com.intershop.component.pmc.capi.definition.pagelet.ContentEntryPointDefinition;
import com.intershop.component.pmc.capi.pagelet.PageletEntryPoint;
import com.intershop.component.preview.capi.call.ApplicationCallInterface;
import com.intershop.sellside.pmc.capi.PageletModelUtils;
import com.intershop.sellside.pmc.capi.modelrepository.ApplicationBOPageletModelRepository;
import com.intershop.sellside.application.b2c.capi.preview.call.B2CApplicationCallInterface;

public class CMSPageCallInterface implements ApplicationCallInterface
{

    private static final String PIPELINE = "ViewContent-Start";
    private static final String HOMEPAGE_PAGE_ID = "systempage.homepage.pagelet2-Page";
    private static final String HOMEPAGE_PIPELINE = "ViewHomepage-Start";
    private static final Set<String> PAGE_BLACKLIST = ImmutableSet.of("systempage.search.result.pagelet2-Page", "systempage.search.didYouMean.pagelet2-Page", "systempage.search.noResult.pagelet2-Page");

    @Override
    public String call(Object o)
    {
        return call(o, null);
    }

    @Override
    public String call(Object o, Map<String, String> params)
    {
        PageletEntryPoint page = PageletEntryPoint.class.cast(o);
        if (!page.isPage()) return null;
        
        // check whether the page is blacklisted or not
        // (used for pages that would pass the mandatory call parameter filter but are not supposed to be generically called)
        if (PAGE_BLACKLIST.contains(page.getId()))
        {
            return null;
        }
        // return a specific pipeline call for the Homepage
        if (page.getId().equals(HOMEPAGE_PAGE_ID))
        {
            return B2CApplicationCallInterface.createURL(HOMEPAGE_PIPELINE, null);
        }
        // return the default content rendering pipeline only for pages that do not have mandatory call parameters
        ContentEntryPointDefinition cepd = page.getContentEntryPointDefinition();
        ApplicationBO app = AppContextUtil.getCurrentAppContext().getVariable(ApplicationBO.CURRENT);
        for(Parameter p : PageletModelUtils.INSTANCE.getCallParameterDefinitions(app.getExtension(ApplicationBOPageletModelRepository.class), cepd))
        {
            if (!p.isOptional()) return null;
        }
        
        HashMap<String, String> pipelineParams = new HashMap<String, String>();
        if (params != null) pipelineParams.putAll(params);
        pipelineParams.put("PageletEntryPointID", page.getId());
        pipelineParams.put("ResourceSetID", page.getResourceSetId());
        return B2CApplicationCallInterface.createURL(PIPELINE, pipelineParams);
    }
    
    @Override
    public String resolve(String urlString)
    {
        URLInformation urlInfo = B2CApplicationCallInterface.resolveURLInformation(urlString);
        
        // check pipeline
        String pipeline = String.format("%s-%s", urlInfo.getPipeline(), urlInfo.getStartNode());
        if (pipeline.equals(PIPELINE) || pipeline.equals(HOMEPAGE_PIPELINE) ) return urlString;
        
        // check required parameters
        Map<String, String> extractedUrlParameters = urlInfo.getParameters();
        if (urlInfo.getPipeline().contains("Content") && extractedUrlParameters.containsKey("PageletEntryPointID"))
        {
            return B2CApplicationCallInterface.createURL(PIPELINE,
                            Collections.singletonMap("PageletEntryPointID", extractedUrlParameters.get("PageletEntryPointID")), urlInfo);
        }
        else
        {
            return null;
        }
    }
}