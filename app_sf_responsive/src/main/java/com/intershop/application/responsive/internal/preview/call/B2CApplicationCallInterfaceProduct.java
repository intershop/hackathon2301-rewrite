package com.intershop.application.responsive.internal.preview.call;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.intershop.beehive.core.capi.url.URLInformation;
import com.intershop.beehive.xcs.capi.product.Product;
import com.intershop.component.preview.capi.call.ApplicationCallInterface;
import com.intershop.sellside.application.b2c.capi.preview.call.B2CApplicationCallInterface;

public class B2CApplicationCallInterfaceProduct implements ApplicationCallInterface
{
    private static final String PIPELINE = "ViewProduct-Start";

    @Override
    public String call(Object o)
    {
        return call(o, null);
    }

    @Override
    public String call(Object o, Map<String, String> params)
    {
        Product p = Product.class.cast(o);
        Map<String, String> productParams = new LinkedHashMap<String, String>();
        if (params != null) productParams.putAll(params);
        productParams.put("SKU", p.getSKU());
        return B2CApplicationCallInterface.createURL(PIPELINE, productParams);
    }

    @Override
    public String resolve(String urlString)
    {
        URLInformation urlInfo = B2CApplicationCallInterface.resolveURLInformation(urlString);
        
        // check pipeline
        String pipeline = String.format("%s-%s", urlInfo.getPipeline(), urlInfo.getStartNode());
        if (PIPELINE.equals(pipeline)) return urlString;
        
        // check required parameters
        Map<String, String> extractedUrlParameters = urlInfo.getParameters();
        if (urlInfo.getPipeline().contains("Product") && extractedUrlParameters.containsKey("SKU"))
        {
            return B2CApplicationCallInterface.createURL(PIPELINE,
                            Collections.singletonMap("SKU", extractedUrlParameters.get("SKU")), urlInfo);
        }
        else
        {
            return null;
        }        
    }
}
