package com.intershop.application.responsive.pipelet.basket;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.core.capi.pipeline.PipelineInitializationException;
import com.intershop.component.address.capi.AddressBO;
import com.intershop.component.basket.capi.BasketBO;
import com.intershop.component.basket.capi.extension.BasketBOShippingBucketExtension;
import com.intershop.component.customer.capi.CustomerBO;

/**
 * Collects all available addresses for checkout from basket and if registered checkout, from customer.
 */
public class PrepareCheckoutAddresses extends Pipelet
{

    private boolean cfg_invoiceAddresses;
    private boolean cfg_shippingAddresses;

    @Override
    public int execute(PipelineDictionary aPipelineDictionary) throws PipeletExecutionException
    {

        BasketBO basketBO = aPipelineDictionary.get("BasketBO");
        CustomerBO customerBO = basketBO.getCustomerBO();        
        
        Set<AddressBO> addresses = new TreeSet<>(new Comparator<AddressBO>()
                        {
                            @Override
                            public int compare(AddressBO o1, AddressBO o2)
                            {
                                return o1.getURN().compareTo(o2.getURN());
                            }       
                        });
        
        Collection<? extends AddressBO> basketShipToAddresses = ((BasketBOShippingBucketExtension)basketBO.getExtension("ShippingBucket")).getShipToAddresses();
        
        if (cfg_shippingAddresses && cfg_invoiceAddresses)
        {
            addresses.addAll(basketBO.getAddressBOs());        
            addresses.addAll(basketShipToAddresses);
            if (customerBO != null)
            {
                addresses.addAll(customerBO.getAddressBOs());
            }
        }
        else
        {
            if (cfg_shippingAddresses)
            {
                addresses.addAll(basketBO.getAddressBOs().stream().filter(AddressBO::isShipToAddress).collect(Collectors.toSet()));               
                addresses.addAll(basketShipToAddresses);
                if (customerBO != null)
                {
                    addresses.addAll(customerBO.getShipToAddressBOs());
                }
            }
            else if (cfg_invoiceAddresses)
            {
                addresses.addAll(basketBO.getAddressBOs().stream().filter(AddressBO::isInvoiceToAddress).collect(Collectors.toSet()));                
                addresses.addAll(basketShipToAddresses.stream().filter(AddressBO::isInvoiceToAddress).collect(Collectors.toSet()));
                if (customerBO != null)
                {
                    addresses.addAll(customerBO.getInvoiceToAddressBOs());
                }
            }
        }

        // put sorted addresses into pipeline dict
        aPipelineDictionary.put("AddressBOs", addresses.stream().sorted(comparing(AddressBO::getFirstName, nullsFirst(naturalOrder())).thenComparing(AddressBO::getLastName, nullsFirst(naturalOrder())).thenComparing(AddressBO::getCity, nullsFirst(naturalOrder()))).collect(Collectors.toList()));
        return PIPELET_NEXT;
    }

    /**
     * The pipelet's initialization method is called whenever the pipeline used
     * to read and process pipelet configuration values that are required during
     * the pipelet execution later on.
     * 
     * @throws PipelineInitializationException
     *             Thrown if some error occurred when reading the pipelet
     *             configuration.
     */
    @Override
    public void init() throws PipelineInitializationException
    {
        cfg_invoiceAddresses = Boolean.valueOf((String)getConfiguration().get("invoiceAddresses")).booleanValue();
        cfg_shippingAddresses = Boolean.valueOf((String)getConfiguration().get("shippingAddresses")).booleanValue();
        if (!cfg_invoiceAddresses && !cfg_shippingAddresses)
        {
            throw new PipelineInitializationException("At least one of attributes 'InvoiceAddresses' or 'ShippingAddresses' needs to be set.");
        }

    }
}
