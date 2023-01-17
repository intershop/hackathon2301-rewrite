package tests.embedded;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.intershop.tools.etest.server.EmbeddedServerRule;

public class DummyIntegrationTest
{

    public EmbeddedServerRule server = new EmbeddedServerRule(this);
    
    @Test
    public void testIsOrderApprovalRequiredWithoutCustomer() throws Exception
    {
        assertEquals("test", "test");
    }
}
