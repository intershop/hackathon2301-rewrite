package tests.com.intershop.application.responsive.internal.checkout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.intershop.application.responsive.capi.checkout.DispatchEntry;
import com.intershop.application.responsive.internal.checkout.CheckoutDispatcherImpl;

public class CheckoutDispatcherImplTest
{
    private CheckoutDispatcherImpl dispatcher = new CheckoutDispatcherImpl();

    private Map<Collection<String>, String> failuresWithTargets = new LinkedHashMap<Collection<String>, String>() {{
        put(Arrays.asList("Products", "Value"), "ViewCart-View");
        put(Arrays.asList("Addresses"), "ViewCheckoutAddresses-View");
        put(Arrays.asList("Shipping"), "ViewCheckoutShipping-View");
        put(Arrays.asList("Payment", "Promotion"), "ViewCheckoutPayment-View");
        put(Arrays.asList("Order"), "ViewCheckoutReview-View");
    }};
                        
    @Before
    public void init()
    {
        Iterator<Collection<String>> iterator = failuresWithTargets.keySet().iterator();
        int i = 0;
        while (iterator.hasNext())
        {
            Collection<String> key = iterator.next();
            dispatcher.addDispatchEntry(createDispatchEntry(key, ++i, failuresWithTargets.get(key)));
        }
    }
    
    @Test
    public void getNoJumpTargetWithEmptyFailures()
    {
        String jumptarget = dispatcher.getJumpTarget(new ArrayList<>());
        assertNull(jumptarget);       
    }
    
    @Test
    public void getNoJumpTargetWithNotExistingFailures()
    {
        String jumptarget = dispatcher.getJumpTarget(Arrays.asList("ANonExistentFailure"));
        assertNull(jumptarget); 
    } 
    
    @Test
    public void getJumpTargetWithPriorities()
    {
        String jumpTarget = dispatcher.getJumpTarget(Arrays.asList("Addresses"));
        assertNotNull(jumpTarget);
        assertEquals("ViewCheckoutAddresses-View", jumpTarget);
        
        jumpTarget = dispatcher.getJumpTarget(Arrays.asList("Addresses", "Payment", "Order"));
        assertNotNull(jumpTarget);
        assertEquals("ViewCheckoutAddresses-View", jumpTarget);
        
        jumpTarget = dispatcher.getJumpTarget(Arrays.asList("Addresses", "Payment", "Order", "Products"));
        assertNotNull(jumpTarget);
        assertEquals("ViewCart-View", jumpTarget);
    }
    
    private DispatchEntry createDispatchEntry(Collection<String> failures, int position, String target)
    {
        DispatchEntry entry = new DispatchEntry();
        failures.forEach(f -> entry.addFailureCode(f));
        entry.setPosition(position);
        entry.setTarget(target);
        return entry;
    }
}
