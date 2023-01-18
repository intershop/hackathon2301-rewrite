package com.intershop.application.responsive.pipelet.basket;

import javax.inject.Inject;

import com.intershop.application.responsive.capi.basket.BasketCookieHandler;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.component.basket.capi.BasketBORepository;

/**
 * Removes the cookie for the given repository. Only works if basket expiration is time-based (if default implementation
 * of BasketCookieHandler is used); if not, no cookie will be removed. Also requires a request context which supports
 * cookies (i. e. this will not work in REST).
 * 
 * @see BasketCookieHandler#removeCookie(BasketBORepository)
 */
public class RemoveBasketCookie extends Pipelet
{
    @Inject
    private BasketCookieHandler cookieHandler;

    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        BasketBORepository repository = dict.getRequired("BasketBORepository");
        cookieHandler.removeCookie(repository);
        return PIPELET_NEXT;
    }

}
