/**
 * 
 */
package com.intershop.application.responsive.internal.checkout;

import com.intershop.component.payment.capi.PaymentBO;
import com.intershop.component.payment.capi.PaymentRedirectEntryPointProvider;
import com.intershop.component.payment.capi.PaymentServiceBOFastCheckoutExtension;
import com.intershop.component.payment.capi.PaymentServiceBORedirectBeforeCheckoutExtension;

/**
 * Implementation of the {@link PaymentRedirectEntryPointProvider} for a_responsive
 * view pipeline logic.
 */
public class PaymentRedirectEntryPointProviderImpl implements PaymentRedirectEntryPointProvider
{
    
    @Override
    public String getCallbackPipeline(PaymentBO payment)
    {
        if (isRedirectBeforeCheckoutPayment(payment))
        {
            return "ViewCheckoutReview-CallbackBeforeCheckout";
        }
        return "ViewCheckoutReview-CallbackAfterCheckout";
    }

    @Override
    public String getJumpTargetPipeline(PaymentBO payment)
    {
        if (isRedirectBeforeCheckoutPayment(payment))
        {
            return "ViewCheckoutReview-Callback";
        }
        return "ViewCheckoutConfirmation-FromRedirect";
    }

    private boolean isRedirectBeforeCheckoutPayment(PaymentBO payment)
    {
        return 
            payment.getPaymentServiceBO().getExtension(PaymentServiceBORedirectBeforeCheckoutExtension.class) != null ||
            payment.getPaymentServiceBO().getExtension(PaymentServiceBOFastCheckoutExtension.class) != null;
    }
}
