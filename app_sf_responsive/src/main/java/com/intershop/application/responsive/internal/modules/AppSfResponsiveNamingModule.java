package com.intershop.application.responsive.internal.modules;

import javax.inject.Singleton;

import com.intershop.application.responsive.capi.basket.BasketCookieHandler;
import com.intershop.application.responsive.internal.basket.BasketCookieHandlerImpl;
import com.intershop.beehive.core.capi.naming.AbstractNamingModule;

public class AppSfResponsiveNamingModule extends AbstractNamingModule
{
    @Override
    protected void configure()
    {
        bind(BasketCookieHandler.class).to(BasketCookieHandlerImpl.class).in(Singleton.class);
    }
}
