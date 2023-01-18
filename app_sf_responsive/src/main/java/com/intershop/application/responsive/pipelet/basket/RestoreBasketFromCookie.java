package com.intershop.application.responsive.pipelet.basket;

import java.util.Optional;

import javax.inject.Inject;

import com.intershop.application.responsive.capi.basket.BasketCookieHandler;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.component.basket.capi.BasketBO;
import com.intershop.component.basket.capi.BasketBORepository;
import com.intershop.component.user.capi.UserBO;

/**
 * Retrieves the basket from the basket cookie for the given repository.
 * Only works if basket expiration is time-based (if default implementation of
 * BasketCookieHandler is used); if not, no value will be returned and the error
 * exit will be used. Also requires a request context which supports cookies
 * (i. e. this will not work in REST).
 * 
 * @see BasketCookieHandler#getBasketBOFromCookie(BasketBORepository)
 */
public class RestoreBasketFromCookie extends Pipelet
{
    @Inject
    private BasketCookieHandler cookieHandler;

    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        UserBO userBO = dict.getRequired("UserBO");
        BasketBORepository repository = dict.getRequired("BasketBORepository");
        Optional<BasketBO> basketBO = cookieHandler.getBasketBOFromCookie(repository);
        if (basketBO.isPresent())
        {
            if (!userBO.isRegistered())
            {
                UserBO basketUserBO = basketBO.get().getUserBO();
                if (basketUserBO == null || !basketUserBO.isRegistered())
                {
                    // Restore a basket if it is either not assigned to any user
                    // or to an non-registered user.
                    // Assign the current user to the basket if it is a non-
                    // registered one.
                    dict.put("BasketBO", basketBO.get());
                    return PIPELET_NEXT;
                }
                else
                {
                    // Do not restore a basket that is assigned to a registered
                    // user, because this basket is wiped and open for changes
                    // of current non-registered user.
                    // A registered user does not expect that his/her baskets
                    // are presented to and changed by another user.
                }
            }
            else
            {
                // Basket(s) of a registered user should never be restored from
                // a cookie, but retrieved from the BasketBORepository instead.
            }
        }

        return PIPELET_ERROR;
    }
}