/*
 * Formatter for interval values (e.g.: P3M | P14D | P2W ... ).
 * @param {string} interval Interval value like these format "P3M | P14D".
 * @return {string} Returns a readable string like "2 Week(s) | 14 Day(s)"
 */
Handlebars.registerHelper("intervalFormatter", function(interval) {
    
    var localizations = {
        'D': $.i18n('subscription.period.days'),
        'W': $.i18n('subscription.period.weeks'),
        'M': $.i18n('subscription.period.months'),
        'Y': $.i18n('subscription.period.years')
    };
    
    if (typeof interval !== 'undefined') {
        
        var time = interval.substr(1, (interval.length-2));
        var type = interval.substr((interval.length-1), 1);
        
        if (time % 7 === 0) {
            time = (time / 7);
            type = "W";
        }
        
        var formattedInterval = time + ' ' + localizations[type];
        
    }else {
        
        var formattedInterval = '';
        
    }
    
    return formattedInterval;
    
});


/*
 * Formatter for date values.
 * @param {string} timestamp Timestamp of the date.
 * @param {string} style The style parameter specifies the format of the date value like ISML formatter.
 * @return {string} Returns a readable string like "07/20/16 or 20.07.2016"
 */
Handlebars.registerHelper("dateFormatter", function(timestamp, style) { 
    
    if (typeof timestamp !== 'undefined') {
    
        var formattedTimeStamp = timestamp;
        
        // If style is available, the timestamp will be formatted using server configurations.
        if (typeof RESTConfiguration.get('DateFormat')[style] === 'undefined') 
        {
            console.warn('Date format "' + style + '" not found in configuration! Date value "' + timestamp + '" is displayed unformatted!');      
        } 
        else 
        {
            formattedTimeStamp = DateFormat.format.date(timestamp, RESTConfiguration.get('DateFormat')[style]);
        }
        
    }else{
        formattedTimeStamp = '-';
    }
    
    return formattedTimeStamp;
    
});


/*
 * Formatter for money values.
 * @param {object} lineitem JSON object of the current line item.
 * @return {string} Returns a formatted money value like '$ 1,019.00' or '200.000,00 €'
 */
Handlebars.registerHelper("moneyFormatter", function(lineitem, style, pType) {
    
    var formattedMoney = "";
    
    if (typeof lineitem.totalGross === 'undefined') {
        return "N/A";
    }
    
    // If style is available, the money value will be formatted by accounting.js library based on server configurations.
    if (typeof RESTConfiguration.get('MoneyFormat')[lineitem.totalGross.currencyMnemonic + '_' + style] === 'undefined') {
        
        console.warn('Money format "' + style + '" not found in configuration! Money value is displayed unformatted!');
        
    }else{
        
        var options = RESTConfiguration.get('MoneyFormat')[lineitem.totalGross.currencyMnemonic + '_' + style];
        
        var moneyValue = null;
		
        var priceType = lineitem.priceType;
        if (typeof pType !== 'undefined') {
            var priceType = lineitem.priceType = pType;
        }
        
        if (typeof lineitem.totalNet !== 'undefined' && typeof lineitem.totalGross !== 'undefined') {
            
            if (priceType === 'net' && typeof lineitem.totalNet !== 'undefined') {
    			moneyValue = lineitem.totalNet.value;
            }else if (typeof lineitem.totalGross !== 'undefined') {
                moneyValue = lineitem.totalGross.value;
            }
            
    		if (typeof moneyValue !== 'undefined') {
    			formattedMoney= accounting.formatMoney(moneyValue, options);
    		}
    		
        }
        
    }
    
    return formattedMoney;
    
});



/*
 * Helper function to generate webshop links.
 * @param {string} pipeline The name of the pipeline to be called.
 */
Handlebars.registerHelper("url", function(pipeline, options) {
    
    // Generated url string + pipeline
    var url = RESTConfiguration.getWebUrl() + pipeline;
    
    // Read all additional attributes from helper
    var attrs = [];
    for (var prop in options.hash) {
        attrs.push( Handlebars.escapeExpression(prop) + '=' + Handlebars.escapeExpression(options.hash[prop])); 
    }
    
    // Add additional url parameters to url string
    if (attrs.length) {
        url = url + '?' + attrs.join('&'); 
    }
    
    return url;
    
});


/*
 * Helper function to generate a dropdown for a period selection
 * @param {string} name The field name of the generated dropdown element.
 * @param {string} period The period value (D|W|M|Y) of the selected option.
 */
Handlebars.registerHelper("periodSelector", function(name, period, testingID) {
    
    var options = [
        { 
            value: 'D',
            desc: $.i18n('subscription.period.days')
        },
        { 
            value: 'W',
            desc: $.i18n('subscription.period.weeks')
        },
        { 
            value: 'M',
            desc: $.i18n('subscription.period.months')
        },
        { 
            value: 'Y',
            desc: $.i18n('subscription.period.years')
        }
    ];
    
    var dropdown = '<select class="form-control" name="'+name+'" data-testing-id="'+testingID+'">';
        
        for (var i=0; i < options.length; i++) {
            
            if (period === options[i].value) {
                select = ' selected="selected"';
            }else{
                select = '';
            }
            
            var dropdown = dropdown.concat('<option value="'+options[i].value+'"'+select+'>'+options[i].desc+'</option>');
            
        }
        
    var dropdown = dropdown.concat('</select>'); 
    
    return new Handlebars.SafeString(dropdown);
    
});


/*
 * Helper function to render a buyer name.
 * @param {object} buyer The object with all buyer related information.
 * @example
        buyer: {
            businessPartnerNo: "fbirdo",
            department: "",
            firstName: "Fritz",
            lastName: "Birdo",
            login: "fbirdo@test.intershop.de",
            title: "",
            type: "RecurringOrderBuyer"
        }
 */
Handlebars.registerHelper("formattedBuyer", function(buyer) {
    
    if (typeof buyer !== 'undefined') {
        
        if (typeof buyer.firstName !== 'undefined') {
            var firstName = buyer.firstName;
        }else{
            var firstName = "";
        }
        
        if (typeof buyer.lastName !== 'undefined') {
            var lastName = buyer.lastName;
        }else{
            var lastName = "";
        }
        
        return Handlebars.escapeExpression(firstName + ' ' + lastName);
        
    }else{
        return '';
    }
    
});
