// Cart & Checkout
var Checkout = {};
Checkout.init = function() {
	// Register your checkout functions here	
	Checkout.onChangeCartWarrantySelection();
	Checkout.addEmailFriend();
	Checkout.shippingAddress();
	Checkout.shippingSelectionAutoSubmit();
	Checkout.shippingClearInputs();
	Checkout.switchCheckoutView();
	Checkout.onToggleCartItems();
	Checkout.selectBillingAddress();
	Checkout.selectShippingAddress();
	Checkout.submitDeleteAddress();
	Checkout.onCancelAddressForm();
	Checkout.giftingHandling();
	Checkout.submitSelectedStore();
	Checkout.onSubmitPromoCode();
	Checkout.onSubmitGiftCode();
}

// Submit Cart if the warranty selection has changed
Checkout.onChangeCartWarrantySelection = function() {
	$(document).on('change', 'select.js-warrantySelection', function(ev){	
		var target = $(ev.currentTarget);
		if (target.attr('data-form-action-key') != null && target.attr('data-form-action-key') == 'update' ){
			$("button[name='update']").click();
		}
	});
}

//Allows you to add extra email addresses when emailing your shopping cart
Checkout.addEmailFriend = function() {
	$(document).on('click', '.add-email-friend', function(ev){
		if($("div[data-inputIndex=1]").is(':hidden')) {
			$("div[data-inputIndex=1]").slideDown();
		}
		else if($("div[data-inputIndex=2]").is(':hidden')) {
			$("div[data-inputIndex=2]").slideDown();
		} 
		else if($("div[data-inputIndex=3]").is(':hidden')) {
			$("div[data-inputIndex=3]").slideDown();
		} 
		else if($("div[data-inputIndex=4]").is(':hidden')) {
			$("div[data-inputIndex=4]").slideDown();
			$(".add-email-friend").hide();
		}
	});
}

//displays alternate shipping address form
Checkout.shippingAddress = function() {
	$(document).on('change', 'input[name=ShippingAddressOption]', function(){
		if($(this).hasClass('alternate-address')){
		  // expand alternate address form
		  $('.checkout-address-alt input').each(function(index) {
		      var arg = $(this).attr('data-validate-disable');
	          if( typeof arg != 'undefined')
	          {
	           $(this).attr('data-validate',arg);
			   $(this).removeAttr('data-validate-disable');
	          }
	      });
	      $('.checkout-address-alt select').each(function(index) {
		      var arg = $(this).attr('data-validate-disable');
	          if( typeof arg != 'undefined')
	          {
	           $(this).attr('data-validate',arg);
			   $(this).removeAttr('data-validate-disable');
	          }        
	      });

		  $('.checkout-address-alt').slideDown()
		}else{
		  if($('.checkout-address-alt').css('display')!='none'){
			 // collapse alternate address form
			 $('.checkout-address-alt input').each(function(index) {
		      var arg = $(this).attr('data-validate');
	          if( typeof arg != 'undefined')
	          {
	           $(this).attr('data-validate-disable',arg);
			   $(this).removeAttr('data-validate');
	          }
	         });
	         $('.checkout-address-alt select').each(function(index) {
		      var arg = $(this).attr('data-validate');
	          if( typeof arg != 'undefined')
	          {
	           $(this).attr('data-validate-disable',arg);
			   $(this).removeAttr('data-validate');
	          }
	         });

	         $('.checkout-address-alt').slideUp()
	         $(this.form).data('bootstrapValidator').disableSubmitButtons(false);
		  }
		}
		if($(this).hasClass('alternate-store-address')){
            $('.checkout-storeaddress-alt').slideDown();
            $('.checkout-storeaddress-alt input').attr('data-validate', 'required');
            // $('.kor-open-store-dialog').click();
            // return true; 
        }else{
            if($('.checkout-storeaddress-alt').css('display')!='none'){
               $('.checkout-storeaddress-alt input').removeAttr('data-validate');
               $('.checkout-storeaddress-alt').slideUp();
            }
        }
	});
}

// apply selected store to current address checkout page
Checkout.submitSelectedStore = function () {
	$(document).on('click', '.selectStore', function(ev){

		var divContainer = $('.submit_Store');
		var selectedStoreVal = $( "input:radio[name=StoreAddressOption]:checked" ).val();
		if (selectedStoreVal == null) {
			divContainer.find(".alert").show();  // show validation errors
			return;
		}

		// add the address block to a target container and add the selected address id as hidden input field
		var addressBlock = divContainer.find("[data-store-id='" + selectedStoreVal + "']");
		$("#checkoutAddressSelectedStore").html(addressBlock);
		$("#checkoutAddressSelectedStore").css('display', 'block');
		$("#checkoutAddressSelectedStore").append($('<input/>', {type: 'hidden', name: 'StoreAddressID', value: selectedStoreVal}));

		$("#checkoutShippingAddressOptionStore").removeClass('has-error');
		$("#checkoutShippingAddressStoreSelectionError").hide();

		// on address book page submit the store address immediately
		$(".shippingAddressSelect").submit(); 

		// on multiple shipping address page add the store address to the select boxes and show a confirmation dialog
		if ( $('select.js-select-shippingaddress').length) {
			$('select.js-select-shippingaddress [data-selectpicker-store]').append( new Option(addressBlock.data('store-name') + ' | ' + addressBlock.data('store-address'), selectedStoreVal));
			$('.bootstrap-select, .selectpicker').removeClass('hide');
			$('.selectpicker').selectpicker('refresh');
			$(".storesSelectConfirmation").click();
			return;
		}

		//close dialog
		$(".close").click();
	});
}

// Shipping selection auto form submit
Checkout.shippingSelectionAutoSubmit = function() {
    $(document).on('change', '.shipping-methods input[type=radio]', function(ev){
    	var form = $(ev.target).closest('form').first();
    	form.submit();
    });
}

// clear shipping instruction if checkbox is unselected
Checkout.shippingClearInputs = function() {
    $(document).on('change', '.shipping-instruction .checkbox input', function(ev){
        if (! $(this).prop('checked')) {
           $textarea = $($(this).data('target')).find('textarea');
           if ($textarea) {
               $textarea.val("");
           }
       }
    });
}

//Switch compact and full checkout view
Checkout.switchCheckoutView = function(){
    $('.compact-view span').on('click', function(){
        if($(this).hasClass('active')){
            $('.cart-compact-view').fadeOut('fast');
            $('.cart-full-view').fadeIn('fast');
            $(this).removeClass('active');
        }else{
            $('.cart-compact-view').fadeIn('fast');
            $('.cart-full-view').fadeOut('fast');
            $(this).addClass('active');
        }
    });
}

/**
 * checkout cart summary -> more items / hide items behavior
 **/
Checkout.onToggleCartItems = function() {
	$(document).on('click','.cart-summary a[data-target="#furtheritemsincart"]', function(ev){
		$('.cart-summary a[data-toggle="collapse"]').each(function(){
			$(this).toggleClass('hidden');			
		});
	});
}

/**
 * checkout address book page -> use selected address as billing address 
 */
Checkout.selectBillingAddress = function() {
    $(document).on('change', 'select.useAsBillingAddress', function(ev) {
        var target = $(ev.currentTarget)
        var form = target.closest('form')
        if ( target.attr('data-form-action-key') != null && target.attr('data-form-action-key') == 'changeAddress' ){
           form.attr('action',form.attr('action')+'?'+$(this).attr('data-form-action-key')).submit();
      }
   });
}

/**
 * checkout address book page / multiple address page-> use selected address as shipping address 
 */
Checkout.selectShippingAddress = function() {
	$(document).on('change', 'select.useAsShippingAddress', function(ev) {
		var target = $(ev.currentTarget);
		var form = target.closest('form');
		if ( target.attr('data-form-action-key') != null && target.attr('data-form-action-key') == 'changeAddress' ){
			form.attr('action',form.attr('action')+'?'+$(this).attr('data-form-action-key')).submit();
		}
	});

	$('.multiple-address-container').on('change', 'select.js-select-multipleshippingaddress', function (ev) {
		var target = $(ev.currentTarget);
		var form = target.closest('form');
		var formData = form.serialize() +'&setShippingAddress=true';
		
		$.ajax({
			type: 'POST',
			url: form.attr('action'),
			data: formData,
			success: function(data){
				if (data.indexOf("<!DOCTYPE") === -1) {
					$(ev.delegateTarget).html(data);
					$('.selectpicker').selectpicker('refresh');
					return;
				}
				else {		/* in case of error, e.g. session timeout */
					window.open(); 
					document.write(data);
				}
			}
		}); 
	});
	
	$('.multiple-address-container').on('change', 'select.js-select-singleshippingaddress', function (ev) {
		this.form.submit();
	});
}

/**
 * checkout address book page -> delete address - check for concurrent modifications
 **/
Checkout.submitDeleteAddress = function() {
    
    $(document).on('click', '.submitDeleteAddress', function(ev) {
        
        ev.preventDefault();
        var target = $(ev.currentTarget);
        var targetName = target.attr('name');
    
        var form = target.closest('form');
        var params = form.serialize() +'&'+targetName+'='+targetName;
        var container = form.closest(".modal-dialog");
        
        $.post(form.attr('action'), params, function(data) {
            var modified = $('.js-dialog-concurrent-modification', data);
            if(modified.length < 1){
                location = form.attr('action')+'?newaddress=addressDeleted';
            }else{
                container.html(data);
            }
        }); 
    });
}

/**
 * handle user canceling of edit/add address
 */
Checkout.onCancelAddressForm = function() {
	$(document).on('click', '.checkout-address-book .cancel', function(event){

		// Show the Add New Address button
		$(this).closest('.address-form-container').prev('.address-box').removeClass('hide');
	});
}

/**
 * Gifting option site
 **/
Checkout.giftingHandling = function() {

	$('form[name=GiftingForm] .giftRadioYes').on('click', function(ev){
		$(this).closest('.pliGiftingWrapper').find('.giftOptions').slideDown();
	});

	$('form[name=GiftingForm] .giftRadioNo').on('click', function(ev){
		$(this).closest('.pliGiftingWrapper').find('.giftOptions').slideUp();
		$(this).closest('.pliGiftingWrapper').find('.giftOptions input').val("");
		
		// Clear nearest text area.
		var $textarea=$(this).closest('.pliGiftingWrapper').find('.giftOptions textarea');
		$textarea.val("");
		// Recalculate the attached to the texarea data-charactercounter
		$textarea.keyup();
		// enable continue button
		$('button[name=continue]').prop("disabled", false);
	});
	
	// show gift message if at least one product has gift options
	if ($("[id^='gift_checkbox']").length) {
		$('.js-gifting-helptext').removeClass('hide');
	}
}

/**
 * submit promo code on checkout payment page - refresh order summary per ajax
 */
Checkout.onSubmitPromoCode = function() {
	//using a bootstrap validator event thrown when a form is validated successfully
	$(document).on('submit', '.order-summary-paymentpage form', function(event){
		event.preventDefault();
		var form = $(this);
		var submitBtn = $(form).find('button[type="submit"]');
		var formData = $(form).serialize() + "&" + $(submitBtn).attr("name") + "=" + $(submitBtn).attr("value");
		var url = $(form).attr("action");
		$.ajax({
			type: 'post',
			url: url,
			data: formData,
			success: function(data){
				//update order summary
				$(".order-summary-paymentpage").html(data);
				$(".order-summary-paymentpage").trigger('afterPromoCode');   		/* do some other action after submit */
			}
		});
	});
}

/**
 * submit gift certificate /card number on checkout payment page - refresh order summary and gift card per ajax
*/
Checkout.onSubmitGiftCode = function() {
	// this event should happen after propertyEditor submit event
	$(document).on('submit', '.checkout-payment-giftcard form', function(event){
		event.preventDefault();
		var form = $(this);
		var submitBtn = $(form).find('button[type="submit"]');
		$(submitBtn).attr("disabled", "disabled");
		var formData = $(form).serialize() + "&" + $(submitBtn).attr("name") + "=" + $(submitBtn).attr("value");
		var url = $(form).attr("action");
		$.ajax({
			type: 'POST',
			url: url,
			data: formData,
			success: function(data){
				var $response=$(data);
				//update order summary
				$(".order-summary-paymentpage").html($response.filter(".order-summary-paymentpage").html());
				//update gift card section
				$(".checkout-payment-giftcard").html($response.filter(".checkout-payment-giftcard").html());
				$('[data-toggle="popover"]', $(".checkout-payment-giftcard")).popover({trigger: 'hover'});
				$(".checkout-payment-giftcard").trigger('afterSubmitGiftCode');  		/* do some other action after submit */
				setTimeout(function() {$(".checkout-payment-giftcard").find('.captcha').captcha();}, 500);
			},
			error: function(data){
			    $(submitBtn).prop("disabled", false);
			}
		});
	});
}

//initialize all checkout functions
$(document).ready(function() {
	Checkout.init();
});
