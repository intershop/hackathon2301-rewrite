package com.intershop.application.responsive.capi.checkout;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Simple container to hold data for checkout dispatching.
 */
public class DispatchEntry implements Comparable<DispatchEntry>
{
    private Integer position;
    private Collection<String> failureCodes = new ArrayList<>();
    private String target;
    
    public Integer getPosition()
    {
        return position;
    }

    public void setPosition(Integer position)
    {
        this.position = position;
    }

    public Collection<String> getFailureCodes()
    {
        return failureCodes;
    }

    public void addFailureCode(String failureCode)
    {
        failureCodes.add(failureCode);
    }

    public String getTarget()
    {
        return target;
    }

    public void setTarget(String target)
    {
        this.target = target;
    }
    
    @Override
    public int compareTo(DispatchEntry o)
    {
        return new Integer(this.getPosition()).compareTo(new Integer(o.getPosition()));
    }
  
}
