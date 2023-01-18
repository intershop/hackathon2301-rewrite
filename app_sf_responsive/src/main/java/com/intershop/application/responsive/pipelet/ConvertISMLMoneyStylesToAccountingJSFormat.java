package com.intershop.application.responsive.pipelet;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.inject.Inject;
import com.intershop.beehive.core.capi.currency.Currency;
import com.intershop.beehive.core.capi.currency.CurrencyMgr;
import com.intershop.beehive.core.capi.currency.StaticMoneyFormat;
import com.intershop.beehive.core.capi.localization.LocaleConstants;
import com.intershop.beehive.core.capi.localization.LocaleInformation;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;

/**
* Converts the ISML money styles MONEY_SHORT, MONEY_LONG etc. to a format used by accounting.js.
*/
public class ConvertISMLMoneyStylesToAccountingJSFormat extends Pipelet
{
    @Inject
    private CurrencyMgr currencyMgr;
    
    @SuppressWarnings("rawtypes")
    @Override
    public int execute(PipelineDictionary aPipelineDictionary) throws PipeletExecutionException
    {
        LocaleInformation localeInfo = aPipelineDictionary.getRequired("LocaleInformation");
        Map<String, String> formats = new HashMap<>();
        Iterator currencies = currencyMgr.createActiveCurrenciesIterator();
        while (currencies.hasNext())
        {
            Currency currency = (Currency)currencies.next();
            formats.put(currency.getMnemonic() + "_MONEY_LONG", createMoneyAccountingJSFormat(LocaleConstants.MONEY_LONG, localeInfo, currency));
            formats.put(currency.getMnemonic() + "_MONEY_SHORT", createMoneyAccountingJSFormat(LocaleConstants.MONEY_SHORT, localeInfo, currency));
            formats.put(currency.getMnemonic() + "_EURO_SHORT", createMoneyAccountingJSFormat(LocaleConstants.EURO_SHORT, localeInfo, currency));
            formats.put(currency.getMnemonic() + "_EURO_LONG", createMoneyAccountingJSFormat(LocaleConstants.EURO_LONG, localeInfo, currency));
            formats.put(currency.getMnemonic() + "_EURO_COMBINED", createMoneyAccountingJSFormat(LocaleConstants.EURO_COMBINED, localeInfo, currency));
            formats.put(currency.getMnemonic() + "_MONEY_INPUT", createMoneyAccountingJSFormat(LocaleConstants.MONEY_INPUT, localeInfo, currency));
        }
        aPipelineDictionary.put("AccountingJSMoneyFormats", formats);
        return PIPELET_NEXT;
    }

    
    /**
     * Creates a string that can be used by accounting.js to format money values. 
     * 
     * @param style
     *      An ISML formatter style like MONEY_LONG, MONEY_SHORT etc.
     *      
     * @param localeInfo
     *      A {@link LocaleInformation} object that specifies the locale.
     *      
     * @return
     *      An string which can be used by accounting.js to format money values.
     */
    private String createMoneyAccountingJSFormat(int style, LocaleInformation localeInfo, Currency currency)
    {
        String currencyMnemonic = currency.getMnemonic();
        String currencySymbol = currency.getCurrencySymbol();
        StaticMoneyFormat moneyFormat = localeInfo.getMoneyFormatForStyle(style);
        DecimalFormat decimalFormat = moneyFormat.getDecimalFormat();
        StringBuilder options = new StringBuilder();
        int minimumFractionDigits = decimalFormat.getMinimumFractionDigits();
        char decimalSeparator = decimalFormat.getDecimalFormatSymbols().getDecimalSeparator();
        char groupingSeparator = decimalFormat.getDecimalFormatSymbols().getGroupingSeparator();
        
        // Prefixes and suffixes may contain '*' or 'M'. They have to be replaced with currency symbol or code.
        // See class StaticMoneyFormat. 
        String negativePrefix = decimalFormat.getNegativePrefix();
        negativePrefix = negativePrefix.replace(StaticMoneyFormat.CURRENCYSYMBOL_PLACEHOLDER, currencySymbol);
        negativePrefix = negativePrefix.replace(StaticMoneyFormat.CURRENCYCODE_PLACEHOLDER, currencyMnemonic);
        String negativeSuffix = decimalFormat.getNegativeSuffix();
        negativeSuffix = negativeSuffix.replace(StaticMoneyFormat.CURRENCYSYMBOL_PLACEHOLDER, currencySymbol);
        negativeSuffix = negativeSuffix.replace(StaticMoneyFormat.CURRENCYCODE_PLACEHOLDER, currencyMnemonic);
        String positivePrefix = decimalFormat.getPositivePrefix();
        positivePrefix = positivePrefix.replace(StaticMoneyFormat.CURRENCYSYMBOL_PLACEHOLDER, currencySymbol);
        positivePrefix = positivePrefix.replace(StaticMoneyFormat.CURRENCYCODE_PLACEHOLDER, currencyMnemonic);
        String positiveSuffix = decimalFormat.getPositiveSuffix();
        positiveSuffix = positiveSuffix.replace(StaticMoneyFormat.CURRENCYSYMBOL_PLACEHOLDER, currencySymbol);
        positiveSuffix = positiveSuffix.replace(StaticMoneyFormat.CURRENCYCODE_PLACEHOLDER, currencyMnemonic);
        
        options.append("{ ");
        options.append("precision: " + minimumFractionDigits + ", ");
        options.append("decimal: \"" + decimalSeparator + "\", ");
        options.append("thousand: \"" + groupingSeparator + "\", ");
        options.append("format: { pos: \"" + positivePrefix + "%v" + positiveSuffix + "\", ");
        options.append("neg: \"" + negativePrefix + "%v" + negativeSuffix + "\" } }");
        return options.toString();
    }
}
