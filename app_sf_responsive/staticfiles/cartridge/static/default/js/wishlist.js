// wishlist.js to handle wishlist functionality
var Wishlist = {} ;

Wishlist.init = function()
{
    Wishlist.NewWishListInputValidationHandler();
}

/**
* This method handles the validation of the NewWishList input and its according radio button
*/
Wishlist.NewWishListInputValidationHandler = function() {
    $(document).on('change', '[name="AddToWishlistForm_TargetWishlistID"], [name="MoveWishlistItemForm_TargetWishlistID"]', function() {
        var newWishlistNameInput = $('#NewWishlistName');
        var bootstrapValidator = $(this.form).data('bootstrapValidator');
        if($(this).attr('id') == 'NewWishlistRadioButton') {
            newWishlistNameInput.prop('disabled', false);
            bootstrapValidator.enableFieldValidators(newWishlistNameInput.attr('name'), true);
        }
        else {
            bootstrapValidator.enableFieldValidators(newWishlistNameInput.attr('name'), false);
            newWishlistNameInput.attr("disabled","");
        }
    });
}

/**
*  Updates the counter of the preferred wish list in the page header after adding a product to the wish list 
*/
Wishlist.updateWishlistStatus = function(count) {
	$("#preferred-wishlist-counter").html(count);
}


$(document).ready(function(){
    Wishlist.init();
});