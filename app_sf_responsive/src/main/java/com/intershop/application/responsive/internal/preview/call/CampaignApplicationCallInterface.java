package com.intershop.application.responsive.internal.preview.call;

import java.util.Map;

import com.intershop.beehive.core.capi.url.URLInformation;
import com.intershop.component.preview.capi.call.ApplicationCallInterface;
import com.intershop.sellside.application.b2c.capi.preview.call.B2CApplicationCallInterface;

public class CampaignApplicationCallInterface implements ApplicationCallInterface
{
    private static final String PIPELINE = "ViewHomepage-Start";

    @Override
    public String call(Object o)
    {
        return call(o, null);
    }

    @Override
    public String call(Object o, Map<String, String> params)
    {
        return B2CApplicationCallInterface.createURL(PIPELINE, params);
    }

    @Override
    public String resolve(String urlString)
    {
        URLInformation urlInfo = B2CApplicationCallInterface.resolveURLInformation(urlString);
        
        // check pipeline
        String pipeline = String.format("%s-%s", urlInfo.getPipeline(), urlInfo.getStartNode());
        if(!PIPELINE.equals(pipeline))
            return null;
        
        return urlString;
    }
}
