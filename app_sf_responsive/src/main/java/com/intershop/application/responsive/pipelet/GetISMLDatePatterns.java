package com.intershop.application.responsive.pipelet;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import com.intershop.beehive.core.capi.localization.LocaleConstants;
import com.intershop.beehive.core.capi.localization.LocaleInformation;
import com.intershop.beehive.core.capi.pipeline.Pipelet;
import com.intershop.beehive.core.capi.pipeline.PipeletExecutionException;
import com.intershop.beehive.core.capi.pipeline.PipelineDictionary;
import com.intershop.beehive.foundation.quantity.StaticDateFormat;

/**
 * Gets the patterns of the ISML date styles DATE_LONG, DATE_SHORT etc.
 *
 */
public class GetISMLDatePatterns extends Pipelet
{

    @Override
    public int execute(PipelineDictionary aPipelineDictionary) throws PipeletExecutionException
    {
        LocaleInformation localeInfo = aPipelineDictionary.getRequired("LocaleInformation");
        Map<String, String> patterns = new HashMap<>();
        patterns.put("DATE_SHORT", getISMLDatePattern(LocaleConstants.DATE_SHORT, localeInfo));
        patterns.put("DATE_LONG", getISMLDatePattern(LocaleConstants.DATE_LONG, localeInfo));
        patterns.put("DATE_TIME", getISMLDatePattern(LocaleConstants.DATE_TIME, localeInfo));
        patterns.put("DATE_INPUT", getISMLDatePattern(LocaleConstants.DATE_INPUT, localeInfo));
        patterns.put("TIME_INPUT", getISMLDatePattern(LocaleConstants.TIME_INPUT, localeInfo));
        patterns.put("DATE_TIME_INPUT", getISMLDatePattern(LocaleConstants.DATE_TIME_INPUT, localeInfo));
        aPipelineDictionary.put("ISMLDatePatterns", patterns);
        return PIPELET_NEXT;
    }
    
    /**
     * Gets the pattern of the given ISML date style.
     * 
     * @param dateStyle
     *      A ISML date style like DATE_LONG.
     *      
     * @param locale
     *      A {@link LocaleInformation} object that specifies the locale.
     *      
     * @return
     *      The pattern of the given date style.
     */
    private String getISMLDatePattern(int dateStyle, LocaleInformation locale)
    {
        StaticDateFormat dateFormat = locale.getDateFormatForStyle(dateStyle);
        SimpleDateFormat simpleDateFormat = (SimpleDateFormat)dateFormat.getDateFormat();
        return simpleDateFormat.toPattern();
    }
}
