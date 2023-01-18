package com.intershop.application.responsive.pipelet;

import java.util.Collection;

import javax.inject.Inject;

import com.intershop.application.responsive.capi.checkout.CheckoutDispatcher;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.component.application.capi.CurrentApplicationBOProvider;

/**
 * Pipelet dispatches to the correct page in case a basket validation or order creation failure occurred.
 */
public class DispatchCheckout extends Pipelet
{
    @Inject
    private CurrentApplicationBOProvider applicationBOProvider;

    @Override
    public int execute(PipelineDictionary aPipelineDictionary) throws PipeletExecutionException
    {
        Collection<String> failures = aPipelineDictionary.getRequired("Failures");
        CheckoutDispatcher dispatcher = applicationBOProvider.get().getNamedObject("CheckoutDispatcher");
        
        String jumpTarget = dispatcher.getJumpTarget(failures);
        if (jumpTarget == null || jumpTarget.isEmpty())
        {
            return PIPELET_ERROR;
        }
        
        aPipelineDictionary.put("JumpTarget", jumpTarget);
        return PIPELET_NEXT;
    }

}
