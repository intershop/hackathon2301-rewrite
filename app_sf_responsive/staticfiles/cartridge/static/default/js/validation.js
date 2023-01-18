// validation.js is used for client side validation operations
var Validation = {};

/*
 * Registering all the methods on initialization.
 */
Validation.init = function() {
    //add default validation to all forms with the 'bv-form' class
    Validation.bind($('form.bv-form'));
    Validation.syncValidationForIdenticalFields();
}

/**
 * bind the given forms to the bootstrap validator.
 * Note: Use this after dynamically adding/removing fields in a form (for example with ajax request)
 * @param forms - a jQuery Object holding form elements
 */
Validation.bind = function(forms){
    $(forms).bootstrapValidator({
        feedbackIcons: {
            valid: 'glyphicon glyphicon-ok',
            invalid: 'glyphicon glyphicon-remove',
            validating: 'glyphicon glyphicon-refresh'
        }
    });
}

/**
 * unbind the given forms from the bootstrap validator.
 * Note: Use this before dynamically adding/removing fields in a form (for example with ajax request)
 * @param forms - a jQuery Object holding form elements
 */
Validation.unbind = function(forms){
    $(forms).bootstrapValidator("destroy");
}

/**
 * revalidates the already filled confirm field (confirmField) if the associated field (baseField) is changed 
 **/
Validation.syncValidationForIdenticalFields = function() {
    $('input[data-bv-identical="true"]').each(function() {
        var $confirmField = $(this);
        var $form = $confirmField.closest('form');
        var $baseField = $form.find('#'+$confirmField.data('bv-identical-field'));
        
        $baseField.on('keyup', function() {
            if( $confirmField.val().length != 0 ) {
                $form.bootstrapValidator('revalidateField', $confirmField);
            };
        });
    });
}

$(document).ready(function(){
    Validation.init();
});
