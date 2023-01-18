package com.intershop.application.responsive.pipelet.basket;

import javax.inject.Inject;

import com.intershop.application.responsive.capi.basket.BasketCookieHandler;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.component.basket.capi.BasketBO;


/**
 * Creates a cookie for the given basket. Only works if basket expiration is time-based (if default implementation of
 * BasketCookieHandler is used); if not, no cookie will be created. Also requires a request context which supports
 * cookies (i. e. this will not work in REST).
 * 
 * @see BasketCookieHandler#createCookie(BasketBO)
 */
public class CreateBasketCookie extends Pipelet
{
    @Inject
    private BasketCookieHandler cookieHandler;

    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        BasketBO basketBO = dict.getRequired("BasketBO");
        cookieHandler.createCookie(basketBO);
        return PIPELET_NEXT;
    }

}
