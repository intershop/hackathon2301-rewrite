package com.intershop.application.responsive.internal.checkout;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import com.intershop.application.responsive.capi.checkout.CheckoutDispatcher;
import com.intershop.application.responsive.capi.checkout.DispatchEntry;

public class CheckoutDispatcherImpl implements CheckoutDispatcher
{
    private Set<DispatchEntry> dispatchEntries = new TreeSet<>();

    @Override
    public String getJumpTarget(Collection<String> failures)
    {
        if (dispatchEntries.isEmpty())
        {
            return null;
        }
        
        for (DispatchEntry entry : dispatchEntries)
        {
            if (!Collections.disjoint(failures, entry.getFailureCodes()))
            {
                return entry.getTarget();
            }
        }
        return null;
    }

    public void addDispatchEntry(DispatchEntry dispatchEntry)
    {
         dispatchEntries.add(dispatchEntry);
    }
    
}
