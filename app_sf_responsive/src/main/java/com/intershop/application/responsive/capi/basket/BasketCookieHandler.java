package com.intershop.application.responsive.capi.basket;

import java.util.Optional;

import com.intershop.component.basket.capi.BasketBO;
import com.intershop.component.basket.capi.BasketBORepository;

/**
 * Interface for handling basket cookie life-cycle.
 */
public interface BasketCookieHandler
{
    /**
     * Retrieves the basket from the cookie for the given repository.
     * 
     * @param repository
     *            Repository for basket lookup
     * @return Basket from cookie if present and basket can be found in repository
     */
    Optional<BasketBO> getBasketBOFromCookie(BasketBORepository repository);

    /**
     * Creates a cookie for the given basket.
     * 
     * @param basketBO
     *            Basket
     */
    void createCookie(BasketBO basketBO);

    /**
     * Removes the cookie for the given repository.
     * 
     * @param repository
     *            Repository
     */
    void removeCookie(BasketBORepository repository);
}
