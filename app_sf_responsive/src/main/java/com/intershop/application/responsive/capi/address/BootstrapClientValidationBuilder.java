package com.intershop.application.responsive.capi.address;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.intershop.beehive.core.capi.localization.LocaleInformation;
import com.intershop.beehive.core.capi.localization.LocalizationProvider;
import com.intershop.beehive.core.capi.localization.context.LocalizationContext;
import com.intershop.beehive.core.capi.naming.NamingMgr;
import com.intershop.beehive.core.capi.request.Request;
import com.intershop.beehive.core.capi.request.WebForm;
import com.intershop.beehive.core.capi.webform.ClientValidationBuilder;
import com.intershop.beehive.core.capi.webform.FormField;
import com.intershop.beehive.core.capi.webform.FormParameter;
import com.intershop.beehive.core.capi.webform.ValidationElement;
import com.intershop.beehive.core.capi.webform.client.AbstractClientValidationBuilder;
import com.intershop.beehive.core.internal.webform.loader.WebformEMFEngineContextFactory;
import com.intershop.beehive.emf.capi.loader.EMFEngine;
import com.intershop.beehive.emf.capi.loader.EMFEngineContext;
import com.intershop.beehive.emf.core.ParameterValueBinding;
import com.intershop.beehive.emf.webform.ClientValidatorBindingDefinition;
import com.intershop.beehive.emf.webform.ValidatorBindingDefinition;
import com.intershop.beehive.emf.webform.ValidatorDefinition;
import com.intershop.beehive.emf.webform.ValidatorParameterDefinition;
import com.intershop.beehive.emf.webform.WebFormPackageDefinition;

public class BootstrapClientValidationBuilder extends AbstractClientValidationBuilder implements ClientValidationBuilder
{

    @Inject
    private LocalizationProvider locProvider;
   
    /**
     * The model engine used for model loading
     */
    private final EMFEngine MODEL_ENGINE = getModelEngine();

    private static final String KEY_PREFIX = "KeyPrefix";
    private static final String LOCALIZED_ERROR_MESSAGE = "LocalizedErrorMessage";
    private static final String REQUIRED_TRUE = "required='true'";
    private static final String MISSING_ERROR_SUFFIX = "missing.error";
    private static final String ERROR_DEFAULT_SUFFIX = "error.default";
    private static final String ERROR_REQUIRED_SUFFIX = "error.required";
    private static final String GENERAL_MISSING_ERROR = "general.missing.error";
    private static final String COUNTRY_CODE = "CountryCode";
    private static final String POINT = ".";
    private static final String SPACER = " ";

    private Object[]params=null;
    
    public BootstrapClientValidationBuilder()
    {
        NamingMgr.injectMembers(this);
    }
    
    @Override
    protected String getClientValidationConfig(ValidationElement element)
    {
        LocaleInformation locale = Request.getCurrent().getLocale();
        LocalizationContext localizationContext = LocalizationContext.create(locale);
        
        // nothing to do if the collection is empty
        Map<ValidatorBindingDefinition, ClientValidatorBindingDefinition> clientValidatorAssignments = getClientValidatorAssignments();
        if (clientValidatorAssignments.isEmpty() && element.isOptional())
        {
            return "";
        }
        
        // the HTML client string
        StringBuilder htmlClientString = new StringBuilder();
        EMFEngineContext ctx = WebformEMFEngineContextFactory.INSTANCE.createContext();

        String webFormName = element.getForm().getID();
        String paramName = element.getID().toLowerCase();
        String messageFirstPart = element.getMessage();
        String locMessage = null;
        
        //FIXME What to do if messagePrefix is null
        String messagePrefix = (String)Request.getCurrent().getPipelineDictionary().get(webFormName + KEY_PREFIX); 
        
        if (messageFirstPart == null) 
        {
            messageFirstPart = messagePrefix + POINT + paramName;
        }
        
        // handle required option first
        if (!element.isOptional()) 
        {
            htmlClientString.append(REQUIRED_TRUE);
            htmlClientString.append(SPACER);
            String key = messageFirstPart + POINT + MISSING_ERROR_SUFFIX;
            locMessage = locProvider.getText(localizationContext, key, params);
            if (locMessage == null)
            {
                key = messageFirstPart + POINT + ERROR_REQUIRED_SUFFIX;
                locMessage = locProvider.getText(localizationContext, key, params);

                if (locMessage == null)
                {
                    locMessage = locProvider.getText(localizationContext, messagePrefix + POINT + GENERAL_MISSING_ERROR, params);
                }
            }
            htmlClientString.append("data-bv-notempty-message='" + locMessage + "'"); 
            htmlClientString.append(SPACER);
            locMessage = null;
        }
        
        // loop through the validator bindings,
        // try to get the assigned validators
        // use an iterator to simply ask .hasNext()
        Iterator<ValidatorBindingDefinition> vbDefsIterator = clientValidatorAssignments.keySet().iterator();
        while (vbDefsIterator.hasNext())
        {
            // get the validator binding
            ValidatorBindingDefinition vbDef = vbDefsIterator.next();
            
            ValidatorDefinition vDef = (ValidatorDefinition)MODEL_ENGINE.getReferencable(
                            WebForm.class, 
                            WebFormPackageDefinition.eINSTANCE.getValidator(), 
                            vbDef.getValidator(), 
                            ctx);
            
            String messageSuffix = vDef.getMessage();
            
            // get the assigned client validator binding
            ClientValidatorBindingDefinition cvbDef = clientValidatorAssignments.get(vbDef);
            
            if (cvbDef == null)
            {
                continue;
            }
            
            // get the pattern, the pattern is the string that will be replaced
            // with the values from the parameter binding of the assigned validator
            String pattern = cvbDef.getPattern();
            
            // check if the pattern is empty, if so, do nothing, skip to
            // the next validator binding
            if (pattern.isEmpty())
            {
                continue;
            }

            // from the pattern get a map of replacements
            Map<String, String> replacements = getReplacementsFromPattern(pattern);
            
            // no replacements, nothing to do
            if (!replacements.isEmpty())
            {
                boolean localizedMessageFound = false;
               
                // check if there are parameter bindings
                List<ParameterValueBinding<ValidatorParameterDefinition>> parameterBindings = vbDef.getParameterBindings();
                
                // no bindings, nothing to do
                if (!parameterBindings.isEmpty())
                {
                    String parameterName;
                    String parameterValue;
                    
                    for (ParameterValueBinding<ValidatorParameterDefinition> parameterBinding : parameterBindings)
                    {
                        parameterName = parameterBinding.getName();
                        parameterValue = parameterBinding.getValue();

                        // add the localized message
                        if (parameterName.equals(LOCALIZED_ERROR_MESSAGE)) 
                        {
                            locMessage = locProvider.getText(localizationContext, parameterValue, params);
                            if (locMessage == null) 
                            {
                                continue;
                            }
                            parameterValue = locMessage;
                            localizedMessageFound = true;
                        }
                        // see if the parameter name is in one the replacements map
                        if (replacements.containsKey(parameterName))
                        {
                            // get the replacement string like {minlength}
                            String toBeReplacedString = replacements.get(parameterName);
                            
                            // no check if the given element is a FormParameter
                            // if so, the FormParameter could have FormFields which are 
                            // referenced in the validator bindings
                            if (element instanceof FormParameter)
                            {
                                FormField field = ((FormParameter)element).getField(parameterValue);
                                if (field != null)
                                {
                                    parameterValue = field.getQualifiedName();
                                }
                            }

                            // if found in the pattern, replace it
                            
                            
                            pattern = pattern.replace(toBeReplacedString, parameterValue);
                        }
                    }
                }

                // generate localized error message if key is not provided in webform
                if (replacements.containsKey(LOCALIZED_ERROR_MESSAGE) && localizedMessageFound == false)
                {
                    // country specific error key e.g. "account.address.de.postalcode.error.regexp"
                    String countryCode = getCountryCode(element);
                    if (locMessage == null && countryCode != null)
                    {
                        locMessage = locProvider.getText(localizationContext, messagePrefix + POINT + countryCode.toLowerCase() + POINT + paramName + POINT + messageSuffix, params);
                    }
                    
                    if (locMessage == null) 
                    {
                        locMessage = locProvider.getText(localizationContext, messageFirstPart + POINT + messageSuffix, params);
                        
                        if (locMessage == null)
                        {
                            // fallback to default error message of the field
                            locMessage = locProvider.getText(localizationContext, messageFirstPart + POINT + ERROR_DEFAULT_SUFFIX, params);
                            
                            if (locMessage == null) 
                            {
                                // fallback to general error message
                                locMessage = locProvider.getText(localizationContext, messagePrefix + POINT + GENERAL_MISSING_ERROR, params);
                            }
                        }
                    }
                    
                    if (locMessage != null)
                    {
                        pattern = pattern.replace(replacements.get(LOCALIZED_ERROR_MESSAGE), locMessage);
                        locMessage = null;
                    }
                }
            }

            htmlClientString.append(pattern);

            // check if there will come some other validator bindings for this element
            if (vbDefsIterator.hasNext())
            {
                htmlClientString.append(SPACER);
            }
        }
        
        return htmlClientString.toString();
    }

    private String getCountryCode(ValidationElement element)
    {
        FormParameter countryCodeParam = element.getForm().getParameter(COUNTRY_CODE);
        if (countryCodeParam != null)
        {
            Object value = countryCodeParam.getValue();
            if (value != null)
            {
                return value.toString();
            }
        }
        return null;
    }
}
