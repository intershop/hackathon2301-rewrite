package com.intershop.application.responsive.pipelet;

import java.util.function.Predicate;

import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.component.basket.capi.BasketBO;
import com.intershop.component.payment.capi.PaymentInstrumentBO;
import com.intershop.component.payment.capi.PaymentInstrumentBORepository;

/**
 * Resolves the payment instrument for a payment service which has no data stored in a property group and therefor only
 * occur once at the basket. In assumption that it is useful to validate user entered data, the absence of the
 * 'Validate' capability is used to detect if this approach is feasible. (Payment instruments that implement the
 * 'Validate' capability are responsible for themselves.)
 * 
 * If no matching payment service was found for the given ID, or no instrument is found at the basket the error exit
 * will be taken.
 */
public class GetPaymentInstrumentByServiceID extends Pipelet
{

    @Override
    public int execute(PipelineDictionary dict) throws PipeletExecutionException
    {
        BasketBO basketBO = dict.getRequired("BasketBO");
        PaymentInstrumentBORepository repository = basketBO.getExtension("Payment");

        if (repository != null)
        {
            String id = dict.getRequired("PaymentInstrumentID");

            PaymentInstrumentBO paymentInstrumentBO = repository.getPaymentInstrumentBOs()
                            .stream()
                            .filter(withPaymentServiceID(id)
                              .and(withoutValidateCapability()))
                            .findAny()
                            .orElse(null);

            if (paymentInstrumentBO != null)
            {
                dict.put("PaymentInstrumentBO", paymentInstrumentBO);
                return PIPELET_NEXT;
            }
        }
        
        return PIPELET_ERROR;
    }

    private Predicate<PaymentInstrumentBO> withPaymentServiceID(String id)
    {
        return paymentInstrumentBO -> paymentInstrumentBO.getPaymentServiceBO().getID().equals(id);
    }

    private Predicate<PaymentInstrumentBO> withoutValidateCapability()
    {
        return paymentInstrumentBO -> paymentInstrumentBO.getPaymentServiceBO().getExtension("Validate") == null;
    }

}
