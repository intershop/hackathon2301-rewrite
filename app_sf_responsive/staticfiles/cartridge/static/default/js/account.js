/**
 * global Account variable is holding all my account specific js methods/functionality
 */
var Account = {};

Account.init = function() {
    Account.onOpenAddressForm();
    Account.onCancelAddresForm();
    Account.onSaveAddress();
    Account.onSortHistoryItems();
	Account.onDisplayAlertFade();
	Account.onSelectPreferredAddress();
}

/**
 * handle click on create new address/edit address buttons in my account > saved addresses section
 */
Account.onOpenAddressForm = function() {
    $(document).on('click', 'a.update-address, a.my-account-add-address', function(event) {
        event.preventDefault();
        var url;
        var editAddressContainer;
        if ($(this).is("[data-url]")) { // Edit button on Suggest Address Form
            url = $(this).attr("data-url");
            editAddressContainer =  $(this).closest('.my-account-address-form-container');
        } else { // Edit Address and New Address links on Saved Addresses page
            url = $(this).attr('href');
            editAddressContainer = $(this).closest('div.myAccount-addressBox').next('.my-account-address-form-container');
        }

        // Hide address form if visible
        $('.my-account-address-form-container').slideUp();

        // AJAX call to prepare the address form content
        $.ajax({
            url : url,
            success : function(data) {
              if (Account.isUserStillLoggedIn(data)) {
                var content;
                if ($(data).find('.shift-content').length > 0) { // Add new address case
                    content = $(data).find('.shift-content').parent().html();
                } else {  // Edit address case
                    content = $(data);
                }

                $(editAddressContainer).html(content).slideDown({
                    complete : function(){
                        Validation.bind($(this).find("form")); // Add bootstrap validation to the returned form
                    }
                });
              }
            }
        });

        // Hide the Add New Address button
        $('.my-account-add-address').hide();
        $(this).closest('.myAccount-addressBook .myAccount-addressBox').hide();
        // In case multiple forms are opened in succession without submitting, restore previously hidden address boxes
        $('.myAccount-addressBook .myAccount-addressBox').not($(this).closest('.myAccount-addressBook .myAccount-addressBox')).show();
    });
}

/**
 * handle user canceling of edit/add address
 */
Account.onCancelAddresForm = function() {
    $(document).on('click', '.my-account-address-form-container a.cancel', function(event){
        event.preventDefault();
        var form = $(this).closest("form");
        Validation.unbind(form);
        $(this).closest(".my-account-address-form-container").slideUp();

        // Show the Add New Address button
        $('.my-account-add-address').show();
        $(this).closest('.myAccount-editBox').prev('.myAccount-addressBox').show();
    });
}

/**
 * handle save address
 */
Account.onSaveAddress = function() {
	// using a bootstrap validator event thrown when a form is validated successfully
	$(document).on('success.form.bv', '.my-account-address-form-container form', function(event) {
		event.preventDefault();
		var form = $(this);
		var submitBtn = $(form).find('button[type="submit"][name]');
		var submitBtnName = $(submitBtn).attr("name");
		var formData = $(form).serialize() + "&" + $(submitBtn).attr("name") + "=" + $(submitBtn).attr("name");
		var url = $(form).attr("action");
		// Make AJAX call to store the address and process the response
		$.ajax({
			type: 'post',
			url: url,
			data: formData,
			success: function(data) {
				if (Account.isUserStillLoggedIn(data)) {
					// Check if address suggest list is returned as response
					var isAddressSuggestion = $(data).find("#suggested-address-form").length > 0;
					var hasErrors = $(data).find("div.alert-danger").length > 0;
					var container;
					if (isAddressSuggestion) { // Replace the create/edit address form content
						if (submitBtnName == "update") {
							container = $(form).closest(".my-account-address-form-container");
						} else { // create new address
							container = $(form).closest(".shift-content");
						}
					} else if (hasErrors) {
						container = $(form).closest(".my-account-address-form-container");
					} else { // success - replace the whole address list

						//Otherwise update Address List
						container = $(".my-account-address-list.shift-content");

						// Show the Add New Address button
						$('.my-account-add-address').show();
					}

					// Inject the response into the defined container
					$(container).html(data);

					if (!hasErrors) {
						Validation.unbind(form);
					} else { // Add bootstrap validation to the returned form
						Validation.bind($(container).find("form"));
					}

					$(form).closest(".my-account-address-form-container").slideUp();

					$('html, body').animate({scrollTop: ($('h1').offset().top)}, 'slow');
				}
				$('.selectpicker').selectpicker('refresh');
			}	
		});
	});
}

Account.onSelectPreferredAddress = function() {
	$(document).on('change', 'select.useAsPreferredAddress', function(ev) {

		var target = $(ev.currentTarget)
		var form = target.closest('form')

		if ( target.attr('data-form-action-key') != null ){
			form.attr('action',form.attr('action')+'?'+ target.attr('data-form-action-key')).submit();
		}
	});
}

/**
 * Generic listener: On change of selectbox submit form
 **/
Account.onSortHistoryItems = function() {
    $(document).on('change', '[data-submit-form-handler=change]', function(ev){
        $(ev.currentTarget).closest('form').submit()
    });
}

Account.onDisplayAlertFade = function() {
   setTimeout(function() {$('.alert-fade').fadeOut();}, 8000);
}

/**
 * check if user logged in after ajax request 
 **/
Account.isUserStillLoggedIn = function(data) {
    if ($(data).filter('a#redirect').length == 1) {
        window.location.href = $(data).filter('a#redirect').attr(('href'));
        return false;
    }
    return true;
}

/*
 * Initialize all Account functions
 */
$(document).ready(function(){
    Account.init();
});
