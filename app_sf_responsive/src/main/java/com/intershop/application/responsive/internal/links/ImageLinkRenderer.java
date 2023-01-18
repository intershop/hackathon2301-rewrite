package com.intershop.application.responsive.internal.links;

import java.net.URI;

import com.intershop.beehive.core.capi.request.Request;
import com.intershop.beehive.core.capi.url.URLComposer;
import com.intershop.beehive.core.capi.url.URLUtils;
import com.intershop.sellside.pmc.capi.linkparser.StorefrontLinkRenderer;

public class ImageLinkRenderer implements StorefrontLinkRenderer
{
    @Override
    public String onLink(URI link, URLComposer composer, String protocol, Request request)
    {
        if (!link.getScheme().equals("image"))
        {
            return null;
        }
        String contentRef = link.getHost()+':'+link.getPath();
        return URLUtils.createContentURL(contentRef);
    }

}
