package com.intershop.application.responsive.capi.checkout;

import java.util.Collection;

/**
 * Interface for handling the pipeline dispatching during the checkout in case a validation or order creation failure
 * occurred.
 */
public interface CheckoutDispatcher
{
    /**
     * Calculates and delivers a jump target for a list of failures that occurred during basket validation or order
     * creation.
     * 
     * @param failures
     *            a collection of all failure codes that occurred
     * @return the jump target
     */
    String getJumpTarget(Collection<String> failures);
}
