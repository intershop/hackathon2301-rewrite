package com.intershop.application.responsive.internal.basket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import com.google.inject.Inject;
import com.intershop.beehive.core.capi.common.SystemException;
import com.intershop.beehive.core.capi.currency.CurrencyMgr;
import com.intershop.beehive.core.capi.domain.Application;
import com.intershop.beehive.core.capi.domain.CurrentApplicationProvider;
import com.intershop.beehive.core.capi.domain.Domain;
import com.intershop.beehive.core.capi.localization.LocaleInformation;
import com.intershop.beehive.core.capi.localization.LocaleMgr;
import com.intershop.beehive.core.capi.log.Logger;
import com.intershop.beehive.core.capi.preference.DomainPreferenceMgr;
import com.intershop.beehive.core.capi.preference.PreferenceNotFoundException;
import com.intershop.beehive.core.capi.preference.ValueTypeViolationException;
import com.intershop.beehive.core.capi.template.TemplateIdentifier;
import com.intershop.component.basket.capi.BasketBO;
import com.intershop.component.basket.capi.remote.service.handler.BasketServiceOrderCreationHandler;
import com.intershop.component.mail.capi.Mail;
import com.intershop.component.mail.capi.MailConstants;
import com.intershop.component.mail.capi.MailMgr;
import com.intershop.component.mail.capi.MailResult;
import com.intershop.component.mail.capi.SendMailService;
import com.intershop.component.order.capi.OrderBORepository;
import com.intershop.component.service.capi.chain.envelope.Envelope;

public class BasketServiceOrderCreationHandlerImpl implements BasketServiceOrderCreationHandler
{   
    @Inject
    private CurrentApplicationProvider currentApplicationProvider;

    @Inject
    private DomainPreferenceMgr domainPreferenceMgr;
    
    @Inject
    private MailMgr mailMgr;
    
    @Inject
    private LocaleMgr localeMgr;

    @Inject
    private CurrencyMgr currencyMgr;

    public BasketServiceOrderCreationHandlerImpl()
    {
        super();        
    }
    
    @Override
    public void orderCreationError(BasketBO basketBO, String recurringOrderID, String recurringOrderNo, String... errorCodes)
    {               
        String template = null;
        String from = null;
        String to = null;
        String subject = null;
        Domain orderDomain = getOrderDomain();
        try
        {
            template = domainPreferenceMgr.getStringPreference("OrderCreationServiceEmailTemplate", orderDomain);
            from = domainPreferenceMgr.getStringPreference("OrderCreationServiceEmailFrom", orderDomain); 
            to = domainPreferenceMgr.getStringPreference("OrderCreationServiceEmailTo", orderDomain);
            subject = domainPreferenceMgr.getStringPreference("OrderCreationServiceEmailSubject", orderDomain);
        }
        catch(PreferenceNotFoundException | ValueTypeViolationException e)
        {
            Logger.error(this, "OrderCreationServiceEmail[Template|From|To|Subject] preference(s) not found or invalid value(s).");
            return;
        }
              
        final LocaleInformation leadLocale = localeMgr.getLeadLocale(); 
        TemplateIdentifier mailTemplate = new TemplateIdentifier(template);
        mailTemplate.setLanguage(leadLocale.getLocaleID());

        Map<String, Object> params = new HashMap<>();
        params.put("BasketBO", basketBO);
        params.put("ErrorCodes", errorCodes);
        params.put("RecOrderID", recurringOrderID);
        params.put("RecOrderNo", recurringOrderNo);
        
        Mail mail = null;
        try
        {
            mail = mailMgr.createMail(mailTemplate, leadLocale, currencyMgr.getCurrency(basketBO.getPurchaseCurrencyCode()), params);
        }
        catch(SystemException | ServletException | IOException e)
        {
            Logger.error(this, "Mail could not be created from template '{}' with locale '{}'.",
                            template, localeMgr.getLeadLocale().getLocaleID());
            return;
        }
        mail.setSubject(subject);
        mail.addReceiver(MailConstants.ADDRESS_TYPE_FROM, from);
        mail.addReceiver(MailConstants.ADDRESS_TYPE_TO, to);
       
        SendMailService service = mailMgr.getSendMailService();
        if (service == null)
        {
            Logger.error(this, "No service of class '{}' available.", SendMailService.class.getName());
            return;
        }
        Envelope<Mail, MailResult> envelope = service.sendMail(mail);

        if (envelope.isDone() && envelope.getServiceException() != null)
        {
            Logger.error(this, "Service exception occurred {}.", envelope.getServiceException().getMessage());
        }
    }
    
    private Domain getOrderDomain() 
    {
        Application application = currentApplicationProvider.get();
        Domain domain = application.getRelatedDomain(OrderBORepository.EXTENSION_ID);
        if (domain == null)
        {
            domain = application.getRelatedDomain("DefaultRepository");
        }

        if (domain == null)
        {
            domain = application.getDomain();
        }
        return domain; 
    }
}
