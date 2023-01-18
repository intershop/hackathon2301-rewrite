package com.intershop.application.responsive.internal.modules;

import javax.inject.Singleton;

import com.intershop.application.responsive.internal.checkout.PaymentRedirectEntryPointProviderImpl;
import com.intershop.beehive.core.capi.naming.AbstractNamingModule;
import com.intershop.component.payment.capi.PaymentRedirectEntryPointProvider;

public class PaymentRedirectEntryPointNamingModule extends AbstractNamingModule
{
    @Override
    protected void configure()
    {
        bind(PaymentRedirectEntryPointProvider.class).to(PaymentRedirectEntryPointProviderImpl.class).in(Singleton.class);
    }
}
