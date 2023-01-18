// global.js is used for the global client side operations.

// initialize the global variables, e.g. the RetailShop object
var RetailShop = RetailShop || {};
var sessionTimedOut = 0;

/*
 * Registering all the methods on initialization.
 */
RetailShop.init = function() {
    RetailShop.getAjaxContent();
    RetailShop.initCSRFGuardedLinks();
    RetailShop.searchAutoSuggestion();
    RetailShop.onClickSearchIcon();
    RetailShop.onCollapseChangeArrow();
    RetailShop.onClickMiniCart();
    RetailShop.addToExpressCart();
    RetailShop.addGiftCertificateToCart();
    RetailShop.verticalSlideMiniCart();
    RetailShop.initiatePopovers();
    RetailShop.carouselSwipe();
    RetailShop.onChangeAddressCountry();
    RetailShop.onSubmitNewsletterSignIn();
    RetailShop.preventToEnterSpaceInPassword();
    RetailShop.onClickDocumentHideMiniCart();
    RetailShop.countdownTextareaCharacters();
    RetailShop.submitFormWithOutsideButton();
    RetailShop.bindClickSourceAndTarget();
    RetailShop.markRatingStars();
    RetailShop.addCartRecipient();
    RetailShop.updateCompareList();
    RetailShop.onChangeCheckboxWithDependent();
    RetailShop.onClickLanguageSwitch();
    RetailShop.onClickDocumentHideLanguageSwitch();
    RetailShop.removeEmptyTabsForCollection();
    RetailShop.removeEmptyTOCEntriesForCollection();
}

/**
 * Gets the content for any DOM element that has an "data-ajax-content" attribute via Ajax.
 * The URL that is called to get the content needs to be provided as the value of
 * the "data-ajax-content" attribute.
 */
RetailShop.getAjaxContent = function() {
	var ajaxContentPlaceholder = $('[data-ajax-content]');
	ajaxContentPlaceholder.each(function() {
		var placeholder = $(this);
		if (placeholder.attr('data-ajax-content') !== '') {
			$.ajax({
				url: placeholder.attr('data-ajax-content'),
				dataType: 'html',
				success: function(data) {
					placeholder.html(data);
				}
			});
		}
	});
}

/**
 * Submits a POST request based on the href-attribute of a link. 
 * The POST request includes the synchronizer token.
 * This can be used to turn links (GET-requests) that cannot carry the
 * synchronizer token to forms (POST-requests) with a valid synchronizer token.
 */


RetailShop.initCSRFGuardedLinks = function() {
    $(document).on('click', 'a[data-csrf-guarded]', function(ev){
        var form = $('<form/>', {method: 'post', action: ev.currentTarget.href}).appendTo(document.body);
        // append the CSRF guard token if available, without the token the request will probably lead to a server error
        if (typeof SYNCHRONIZER_TOKEN_VALUE !== 'undefined' && SYNCHRONIZER_TOKEN_VALUE != '') {
            form.append($('<input/>', {type: 'hidden', name: SYNCHRONIZER_TOKEN_NAME, value: SYNCHRONIZER_TOKEN_VALUE}));
        }
        form.submit();
        ev.stopPropagation();
        return false;
    });
}


/**
 * Search Auto complete: Auto suggest search start
 */
RetailShop.searchAutoSuggestion = function(){
  
    // Auto suggest search start on press key in search box field 
    $('input.searchTerm').on('keyup', function(e){  
        if($(this).val().length > 0){
            // Select suggested search result items by Keyboard keys
            if(e.keyCode == 40 || e.keyCode == 38){
                var suggestionList = $(this).siblings('ul.search-suggest-results').children('li');
                // Move focus on suggested search result list by press down arrow key
                if(e.keyCode == 40){
                    var noSuggestionSeleted=true;
                    // If last list item selected then nothing happen on press down arrow key
                    if(suggestionList.last().hasClass('active-suggestion')){
                        return false;
                    }
                    if(suggestionList.length > 0 && noSuggestionSeleted == true){
                        $.each(suggestionList, function(idx, value){
                            if($(this).hasClass('active-suggestion') && (idx < suggestionList.length-1)){
                                $(this).removeClass('active-suggestion');
                                // Select next list item from auto suggested search list
                                $(this).next().addClass('active-suggestion');
                                noSuggestionSeleted=false;
                                return false;
                            }
                        });
                        // Select first auto suggested list item
                        if(noSuggestionSeleted == true){
                            suggestionList.first().addClass('active-suggestion');
                        }
                    }
                }
                // Move focus on suggested search result list by press up arrow key
                else if(e.keyCode == 38){
                    // If First list item selected then nothing happen on press up arrow key
                    if(suggestionList.first().hasClass('active-suggestion')){
                        return false;
                    }
                    if(suggestionList.length > 0){
                        $.each(suggestionList, function(idx, value){
                            if($(this).hasClass('active-suggestion')){
                                $(this).removeClass('active-suggestion');
                                // Select previous list item from auto suggested search list
                                $(this).prev().addClass('active-suggestion');
                                return false;
                            }
                        });
                    }
                }
                    
            } else {
                // Start AJAX from auto suggestion search when user stop typing
                if (sessionTimedOut) {
                    clearTimeout(sessionTimedOut);
                }
                sessionTimedOut = setTimeout(autoSuggestSearch($(this), 500));
            }
        }
        
    });
    
    // Auto suggest search item select by enter or tab key 
    $('input.searchTerm').on('keydown', function(e){
        var inputElement= $(this);
        var formElement = $(this).parents('form.search');
        var suggestionList = $(this).siblings('ul.search-suggest-results').children('li');
        // Move selected suggested list item into search field by tab key
        if(e.keyCode == 9){
            e.preventDefault();
            if($(suggestionList).length > 0){
                $(suggestionList).each( function(){
                    if($(this).hasClass('active-suggestion')){
                        inputElement.val($(this).find('.search-result').attr('data-search-result'));
                        inputElement.siblings('ul.search-suggest-results').empty().hide();
                        return false;
                    }
                });
            }
        }
        // Move selected suggested list item into search field and submit search form by enter key
        else if(e.keyCode == 13){
            e.preventDefault();
            // there are suggested values
            if($(suggestionList).length > 0){
                $(suggestionList).each( function(index){
                    //if something selected in auto suggestion list submit form with selected value
                    if($(this).hasClass('active-suggestion')){
                        $(this).find('.search-result')[0].click();
                        return false;
                    }
                    //if nothing selected in auto suggestion list submit form with entered value
                    else {
                        if(index == ($(suggestionList).length-1) ) {
                            inputElement.siblings('ul.search-suggest-results').empty().hide();
                            formElement.submit();
                        };
                    }
                });
            //there are not suggested values
            } else {
                if(inputElement.val().length > 0)
                {
                    // Submit search form
                    $(this).parents('form.search').submit();
                }
            }
        }    
    });
}
/**
 * Start auto suggest search  when user stop typing
 */
function autoSuggestSearch (searchbox){ 
    showSuggestSearchList(searchbox); 
}
/**
 * Display auto suggestion search list 
 */ 
function showSuggestSearchList(searchInput){
    var searchTerm = $.trim($(searchInput).val());
    var ajaxurl = $(searchInput).attr('data-suggesturl') + "&SearchTerm=" + searchTerm;
    $.ajax({
        url: ajaxurl,
        type: 'post',
        datatype: 'text/html',
        success: function(data){
            // Show auto suggestion list item block
            if($(data).filter('.suggest-results-list').length > 0){
                $(searchInput).siblings('.search-suggest-results').html($(data).filter('.suggest-results-list').html()).fadeIn(500);
            }else{
            // Hide Auto suggestion block when suggestion result are zero
                $(searchInput).siblings('.search-suggest-results').empty().fadeOut(500);
            }
        }
    });
}
/**
 * Click on auto suggest search list item  
 */
$(document).on("click", "ul.search-suggest-results li button", function(){
    var formElement = $(this).parents('form.search');
    formElement.children('input.searchTerm').val($(this).attr('data-search-result'));
    formElement.children('.search-suggest-results').empty().hide();
    // Submit Search form
    formElement.submit();
});
/**
 * Submit Search Form: If field has at least one letter then submit search form 
 */ 
RetailShop.onClickSearchIcon = function(){
        $('form.search').on('submit', function(e){
        // Check search field has at least one letter in string
        if(!($(this).children('.searchTerm').val().length > 0)){
            e.preventDefault();
        }
    });
}

/**
 * Bootstrap Accordion: Change arrow up/down arrow of accordion tabs
 */ 
RetailShop.onCollapseChangeArrow = function(){
    $('.filter-group h3').on('click', function(){
        if($(this).hasClass('collapsed')){
            $(this).find('span').removeClass('glyphicon-chevron-right').addClass('glyphicon-chevron-down');
        }else{
            $(this).find('span').removeClass('glyphicon-chevron-down').addClass('glyphicon-chevron-right');
        }
    });
    $('.panel-heading a').on('click', function(){
        if($(this).hasClass('collapsed')){
            $(this).find('span').removeClass('glyphicon-plus').addClass('glyphicon-minus');
        }else{
            $(this).find('span').removeClass('glyphicon-minus').addClass('glyphicon-plus');
        }
    });
}  

/**
 * Minicart active / inactive
 */
RetailShop.onClickMiniCart = function(){
    // change the class of mini cart to show active and inactive
    $(document).on('click', '.quick-cart-link > a', function(event){
        $(this).closest('.quick-cart-link').toggleClass('mini-cart-active');
    });
}

/**
 * add to cart via AJAX handler - add to mini cart (express cart)
 */
RetailShop.addToExpressCart = function(){
    $(document).on('success.form.bv','form.product-form:has([data-expresscart])', function(event) {
        // on cart pages execute a full request instead of ajax 
        if ( $("div[data-cart-page]").length) { 
            return true;
        }
        event.preventDefault();
        var form = $(this);
        var addBtn = form.find('.add-to-cart [type="submit"]');
        // disable add to cart button during cart processing
        addBtn.attr('disabled', true).addClass('processing-state');
        var param = form.serialize();
        $.ajax({
            url: addBtn.attr('data-expresscart-action'),
            type: 'post',
            data: param,
            success: function(data) {
                // re-enable the add to cart button 
                addBtn.attr('disabled', false).removeClass('processing-state');
                // if add to cart is done from within a dialog (e.g. express shop) then first close the dialog
                if($('.modal').length) {
                    $('.modal').modal('hide');
                }
                // scroll to the top of the page to see the updated mini cart
                window.scrollTo(0, 0);
                // update mini cart data from the response
                $('.quick-cart-link').html($(data).filter('.quick-cart-link').html());
                $('.quick-cart-link > a').trigger('click');
            }
        });
    });
}

/**
 * add gift certificate to cart handler
 */
RetailShop.addGiftCertificateToCart = function(){
    $(document).on('success.form.bv','#gift-certificate-form', function(event) {
        event.preventDefault();
        var $form = $(this);
        var $target = $form.closest('.modal');
        $.ajax({
            url: $form.attr('action'),
            type: 'post',
            data: $form.serialize(),
            success: function(data) {
                // a response with a link with the id="redirect" signals that this href should be loaded
                // this is used to make a https redirect to the cart that only works with https
                if ($(data).filter('a#redirect').length == 1) {
                    window.location.href = $(data).filter('a#redirect').attr(('href'));
                } else {
                    var $data = $(data);
                    if ($data.find('#miniCart, .quick-cart-link').length > 0) {
                        if($('.modal').length) {
                            $('.modal').modal('hide');
                        }
                        // scroll to the top of the page to see the updated mini cart
                        window.scrollTo(0, 0);
                        // update mini cart data from the response
                        $('.quick-cart-link').html($data.filter('.quick-cart-link').html());
                        $('.quick-cart-link > a').trigger('click');
                    } else {
                        $target.html(data);
                        Validation.bind($target.find('form.bv-form'));
                    }
                }
            }
        });
    });
}

/**
 * Move vertical up/down product rows in Mini Cart
 */
RetailShop.verticalSlideMiniCart = function(){
    // Click move down-arrow mini cart
    $(document).on("click", "#miniCart .btn-scroll-down", function(){
    	var tileHeight = $("#miniCart .product-rows-block .slider .product-row").last().outerHeight();
        var top = $("#miniCart .product-rows-block .slider").css('top');
        if(top === 'auto') {
            top = 0;
        }
        var productNum = Math.round(Math.abs(parseInt(top)/tileHeight));
        var productMax = $('.product-rows-block .product-row').length;
        // Check product count should be more than 2 
        if(productNum < productMax - 2) {
            productNum++;
            $("#miniCart .product-rows-block .slider").stop(true, true).animate({
                top: '-='+tileHeight
            }, 200, function() {
                if(productNum > 0) {
                    $("#miniCart .btn-scroll-up").removeClass("disabled");
                }
                if(productNum == productMax - 2) {
                    $("#miniCart .btn-scroll-down").addClass("disabled");
                }
            });
        }
    });
    // Click move up-arrow mini cart
    $(document).on("click", "#miniCart .btn-scroll-up", function(){
    	var tileHeight = $("#miniCart .product-rows-block .slider .product-row").last().outerHeight();
        var productNum = Math.round(parseInt($("#miniCart .product-rows-block .slider").css('top'))/tileHeight*(-1));
        var productMax = $('.product-rows-block .product-row').length;
        // Check up-arrow block is enable
        if(productNum > 0) {
            productNum--;
            $("#miniCart .product-rows-block .slider").stop(true, true).animate({
                top: '+='+tileHeight
            }, 200, function() {
                if(productNum < productMax - 2) {
                    $("#miniCart .btn-scroll-down").removeClass("disabled");
                }
                if(productNum == 0) {
                    $("#miniCart .btn-scroll-up").addClass("disabled");
                }
            });
        }
    });
}
/**
 * Initiate global bootstrap popovers for modules/common/tooltip.isml for mobile
 * devices: on click, for all other devices: on hover
 */
RetailShop.initiatePopovers = function() {
	if (Modernizr.touchevents) {
		$('[data-toggle="popover"]').popover({
			trigger : 'click'
		});
	} else {
		$('[data-toggle="popover"]').popover({
			trigger : 'hover'
		});
	}
}

RetailShop.carouselSwipe = function(){
	 $(".carousel").swiperight(function() {  
		 $(this).carousel('prev');  
	 });  
	 $(".carousel").swipeleft(function() {  
	     $(this).carousel('next');  
	 });   
}

/**
 * Click on page out side mini-cart and suggested product list box hide these box if they display  
 */
RetailShop.onClickDocumentHideMiniCart = function(){
    var flag = false; // Boolean variable
    // On mouse hover at mini-cart or suggested product list box flag value is set true
    $(document).on('mouseenter', '#miniCart, .search-suggest-results', function() {
        flag = true;
    });
    $(document).on('mouseleave', '#miniCart, .search-suggest-results', function() {
        flag = false;
    });
    
    $(document).on('click', function() {
        if (!flag) {
            // Check suggested product list box is visible
            if($('.search-suggest-results').is(':visible')){
                $('.search-suggest-results').fadeOut(500);
            }
            //  Check mini-cart box is visible
            if($('#miniCart').hasClass('in')){
                $('.quick-cart-link > a').trigger('click');
            }
        }
    });
}

/**
 * Request and display Address Form when changing Country
 */
RetailShop.onChangeAddressCountry = function(){
    $(document).on('change', '[data-address-form="data-address-form"] select[data-country-list="data-country-list"]', function(){
        //find parent span element that holds dynamic address form fields
        var parent = $(this).closest('[data-address-form="data-address-form"]');
        //get surrounding form
        var form = $(this).closest("form");
        //determine form type
        var formType = $(this).attr("name").split("_")[0];
        //get form values
        var formValues = $(form).serialize()+ "&formType=" + formType;
        //get url
        var url = $(this).data("url");
        $.ajax({
            type: 'post',
            url : url,
            data : formValues,
            success : function(data){
                var content = $(data).find('select[data-country-list="data-country-list"][name^="' + formType + '_"]').closest('[data-address-form="data-address-form"]').html();
                Validation.unbind(form);
                $(parent).fadeOut("400", function(){
                    $(parent).html(content).fadeIn("400", function(){
                        Validation.bind(form);
						$('[data-toggle="popover"]', $(parent)).popover({trigger: 'hover'});
                    });
                });
            }
        });
    });
}

/**
 * handling newsletter sign in
 */
RetailShop.onSubmitNewsletterSignIn = function() {
    $(document).on('submit', '.newsletter-form', function(event){
        event.preventDefault();
        var form = $(this);
        var url = form.attr('action');
        var formData = form.serialize() + "&" + form.find('button[type="submit"]').attr('name') + "=" + form.find('button[type="submit"]').attr('name');
        
        Dialog = new dialog();
        Dialog.options({
            formData: formData
        });
        Dialog.create(url, function() {
            if($('.signup-success-msg', this.modal).length == 1){
                $('#newsletter_email').val('');
            }
        });
        Dialog.open();
    });
}

/**
 * All Password field: Prevent to enter space as a character
 */
RetailShop.preventToEnterSpaceInPassword = function(){
    $('input[type=password]').on('keydown', function(event) {
        if (event.keyCode == 32){
            event.preventDefault();
        }
    });
}

/**
 * counts down the remaining input characters of a textarea
 * The textarea should have the following attributes
 * 		data-charactercounter=<id of the character counter>
 *		maxlength=<max number of input characters>
 */
RetailShop.countdownTextareaCharacters = function(){
    $(document).on('keyup', 'textarea[data-charactercounter]', function(event) {
        var len = this.value.length;
        var maxlen = $(this).attr('maxlength');
        if (maxlen > 0) {
            $('#'+$(this).data('charactercounter')).text(maxlen - len);
        }
    });
    
    // init counter
    $('textarea[data-charactercounter]').trigger('keyup');
}

/**
 * submit a form with a (submit) button outside the form
 * IE fix - other browsers support this HTML5 standard
 */ 
RetailShop.submitFormWithOutsideButton = function(){
	$(document).on('click', 'button[form]', function(event) {
		$(this).attr('disabled', true);		// disable button for further submits
		var $form=$('#'+$(this).attr('form'));
		$form.append($('<input/>', {type: 'hidden', name: $(this).attr("name"), value: $(this).attr("value")}));
		$form.trigger('beforesubmit');  /* do some action before submit */
		$form.submit();
	});
}

/**
 * bind a click source (e.g. a button) that will trigger a click event at the fitting click target 
 * that will then lead to an actual form submit (e.g. for adding a RetailSet with the set quantities to the cart)
 */
RetailShop.bindClickSourceAndTarget = function(){
    $(document).on("click",'[data-click-source]', function(event) {
        event.preventDefault();
        $('[data-click-target="'+ $(this).attr("data-click-source") +'"]').first().click();
    });
}

RetailShop.markRatingStars = function(){
    $(document).on('mouseenter', ".rating-input li", function(){
      $( ".rating-input li:hover" ).prevAll().addClass( "stars-before" );
    });

    $(document).on('mouseleave', ".rating-input li", function(){
      $( ".rating-input li" ).prevAll().removeClass( "stars-before" );
    });
}

/**
 * Display input field on email template when clicking on add recipients link.
 */
RetailShop.addCartRecipient = function() {
    $(document).on('click', '.kor-input-add', function(ev){
        if($('[data-inputindex="1"]').is(':hidden')) {
            $('[data-inputindex="1"]').removeClass('hidden').slideDown();
        }
        else if($("[data-inputIndex=2]").is(':hidden')) {
            $('[data-inputindex="2"]').removeClass('hidden').slideDown();
        } 
        else if($("[data-inputIndex=3]").is(':hidden')) {
            $('[data-inputindex="3"]').removeClass('hidden').slideDown();
        } 
        else if($("[data-inputIndex=4]").is(':hidden')) {
           $('[data-inputindex="4"]').removeClass('hidden').slideDown();
            $(".kor-input-add").hide();
        }
    });
}

/**
 * Sets the compare state of compare triggers to "is-selected" for already selected products.
 */
RetailShop.setCompareState = function() {
    if ($('#compare-count').data('compare-ids') !== undefined) {
        var compareitems = $('#compare-count').data('compare-ids').split(',');
        $('.add-to-compare-trigger').each(function(){
            if($.inArray($(this).data("product-ref"), compareitems) !== -1) {
                $(this).addClass("is-selected");
            }
            else {
                $(this).removeClass("is-selected");
            }
        });
    }
}

/**
 * Adds or deletes a product to/from the compare list and updates the counter.
 */
RetailShop.updateCompareList = function() {
    $(document).on('click', '.add-to-compare-trigger', function(event){
        event.preventDefault();
        var self = $(this);
        var ajaxurl = "";
        if(self.hasClass('is-selected')){
            ajaxurl = self.attr("data-url-delete");
        } else {
            ajaxurl = self.attr("data-url-add");
        }
        $.ajax({
            url: ajaxurl,
            success: function(data) {
                $('#compare-count').empty().append(data);
                self.toggleClass("is-selected");
            }
        });
    });
}

/**
 * Handling of dependent checkboxes
 * - disable/enable the dependent checkbox according to another checkbox
 * - don't allow checking of the dependant checkbox if the releated checkbox is unchecked
 * both checkboxes should be in the same form-group, data-dependent-checkbox contains the name attribute of the dependent checkbox
 */
RetailShop.onChangeCheckboxWithDependent = function() {
	$(document).on('change', '.checkbox input[data-dependent-checkbox]', function(event){
		if ($(this).data('dependent-checkbox')) {
			var dependentCheckbox=$(this).closest(".form-group").find("[name="+$(this).data('dependent-checkbox')+"]");
			
			if ($(this).prop("checked")) {
				dependentCheckbox.closest(".checkbox").show();
			}
			else {
				dependentCheckbox.prop("checked", false).closest(".checkbox").hide();
			}
		}
	});
	$(".checkbox input[data-dependent-checkbox]").trigger('change');
}


/**
 * handling language switch
 * 
 * - language switch active / inactive
 * - click on page outside language switch and suggested box hide these box if they display  
 *
 */
RetailShop.onClickLanguageSwitch = function(){
    // change the class of language switch link to show active and inactive
    $(document).on('click', '.language-switch-link', function(event){
        $(this).closest('.language-switch').toggleClass('language-switch-active');
    });
}
RetailShop.onClickDocumentHideLanguageSwitch = function(){
    var flag = false; // Boolean variable
    // On mouse hover at mini-cart or suggested product list box flag value is set true
    $(document).on('mouseenter', '#languageSwitch', function() {
        flag = true;
    });
    $(document).on('mouseleave', '#languageSwitch', function() {
        flag = false;
    });
    
    $(document).on('click', function() {
        if (!flag) {
            //  Check mini-cart box is visible
            if($('#languageSwitch').hasClass('in')){
                $('.language-switch-link').trigger('click');
            }
        }
    });
}


/**
 * handling for tab display of collection
 * 
 * - remove tab and tab content container for tabs without content
 * - remove whole tab set container if there are no tabs remained or activate first tab if no active tab defined
 */
RetailShop.removeEmptyTabsForCollection = function() {
    $.each($('.collection.tabset'), function() {
        var $tabset = $(this);
        //find tabs without content
        $.each($tabset.find('.tab-pane').filter( function() {
            return $(this).children().length === 0;
        }), function() {
            // remove tab and tab content
            $tabset.find('a[href="#'+ $(this).attr('id') + '"]').parent('li').remove();
            $(this).remove();
        });
        // list without tabs
        if ($tabset.find('.nav-tabs > li').length === 0) {
            $tabset.remove();
        } 
        // tab activation
        else {
            if ($tabset.find('.nav-tabs > li.active').length === 0) {
                $tabset.find('.nav-tabs > li:first-child').children('a').click();
            };
        };
    });
}

/**
 * handling for toclist display of collection
 * 
 * - remove toc entry and entry content container for entries without content
 * - remove whole toclist container if there are no toc entries remained
 */
RetailShop.removeEmptyTOCEntriesForCollection = function() {
    $.each($('.toclist'), function() {
        var $toclist = $(this);
        // find entries without content
        $.each($toclist.find('.toclist-content').filter(function() {
            return $(this).children().length === 0;
        }).parents('.toclist-entry'), function() {
            // remove toc entry end entry content
            $('a[href="#'+$(this).data('entry-id')+'"]').parent('li').remove();
            $(this).remove();
        });
        // no entries remained
        if ($toclist.find('.toclist-content').length === 0) {
            $toclist.remove();
        }
    });
}

$(document).ready(function(){
    RetailShop.init();
});