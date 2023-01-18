/*
    Javascript Class - Dialog
    
    Options:
    {
        url: 'http... | jQuery_selector',
        callback: false | function() { ... },
        formData: false
    }
    
    Methods:
    .options({...});
    .create(<url|jQuery_selector>, <callback>);
    .open();
    .init(<modalHTMLcontent>);
    
    
    Usage:
    
    Initialize a dialog with a link, a clickable <div>, a button or a form submit
    =============================================================================
    Set the "data-dialog" attribute at a link
    to open the given href response in a dialog.
    
    <a data-dialog href="http://...">Your Link</a>
    
    Set the "data-dialog" attribute at a <div>
    and provide an additional "data-dialog-href" attribute
    whose response should be opened in a dialog.
    
    <div data-dialog data-dialog-href="http://...">Your Link</div>
    
    Set the "data-dialog" attribute at a button
    to open the surrounding forms action submit response in a dialog.
    If an additional 'data-dialog-action' is provided the button will 
    submit the surrounding form with that action.
    With an additional 'data-dialog-form' that has as value a form id the button 
    will trigger a submit of that given form instead of a surrounding one.
    
    <button type="submit" data-dialog data-dialog-action="http://..." data-dialog-form="form_id">
    
    Set the "data-dialog" attribute at a form
    to open the given action submit response in a dialog.
    
    <isform data-dialog="true" action="http://..." method="post">
    When using <isform> it is important to provide attributes always with a value.
    
    
    Dialog content HTML structure description
    =========================================
    The dialog/modal HTML content that needs to be returned
    by the given reference is the same as documented by Bootstrap:
    http://getbootstrap.com/javascript/#modals
    except for the most outer <div class="modal"> hook,
    that is created by the Javascript.
    
    Example dialog HTML content structure:
    
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal" title="<istext key="dialog.close.text">" aria-label="<istext key="dialog.close.text"/>"><span aria-hidden="true">&times;</span></button>
          <h2 class="modal-title">Modal title</h2>
        </div>
        <div class="modal-body">
            ...
        </div>
        <div class="modal-footer">
            ...
        </div>
      </div>
    </div>
    
    This way one can add forms around any parts (e.g. content and footer) 
    of the dialog content, or leave out unwanted parts (e.g. footer). 
    It is also possible to control for example the dialog size through 
    the HTML of the returned content.
    
    
    Display the result of a dialog interaction within the dialog
    ============================================================
    To render the result of an interaction again in the current dialog 
    the "data-hijax" attribute is introduced.
    Adding this attribute to any form or link within the dialog content 
    will result in rendering the response content again in the current dialog.
    
    <a data-hijax href="http://...">Open link in current dialog</a>
    
    <isform data-hijax="true" action=""http://..." method="post">
    When using <isform> it is important to provide attributes always with a value.
    
    
    Display local content in a dialog (non Ajax)
    ============================================
    To display local modal content (not getting the content via Ajax 
    from the server) the following HTML structure is used.
    The relevant part is providing a jQuery selector within the href instead of an URL.
    The jQuery selector has to point to a hidden div that includes the complete
    <div class="modal-dialog">...</div> part as described earlier.
    
    <a data-dialog href="[data-local-modal-content]">
    
    <div class="hidden" data-local-modal-content>
      <div class="modal-dialog">
        <div class="modal-content">
          ...
        </div>
      </div>
    </div>
    
    
    Initializing non standard dialogs via Javascript, e.g with a callback function
    ==============================================================================
    In case a specific callback function is needed once the dialog content is loaded, 
    relying on the "data-dialog" functionality will not work. In that case a specific 
    handler needs to be used that uses the provided dialog methods.
    
    Example dialog handling via Javascript:
    
    $(document).on('submit', '[my-form-selector]', function(event){
        event.preventDefault();
        
        var Dialog = new dialog();
        Dialog.options({
            formData: form.serialize()
        });
        Dialog.create(form.attr('action'), function() {
            // some callback function code
        });
        Dialog.open();
    });
    
    
    Dialog auto close handling and redirecting
    ==========================================
    
 */

var dialog = function () {
    
    this._options = {};
}
dialog.prototype = {
    
    options: function(options) {
        $.extend(true, this._options, options);
    },
    
    create: function(url, callback) {
        
        // do nothing if another dialog is already initializing
        if (Dialogs.initializing) {
            return;
        };
        // set the dialog initializing flag 'true'
        Dialogs.initializing = true;
        
        var id = Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
        var modal = $('<div class="modal fade" role="dialog" aria-labelledby="modalLabel-'+id+'" aria-hidden="true"></div>');
        
        this.options({
            id: id,
            modal: modal,
            url: url,
            callback: callback
        });
        
        modal.appendTo('body');
        modal.modal('hide');
        modal.on('hidden.bs.modal', function(e) {
            // fix to prevent modals that cannot be scrolled on the iPhone
            if (/iPhone|iPod|iPad/.test(navigator.userAgent)) {
                $('body').css('overflow','auto');
                $('body').css('position','relative');
            }
            modal.remove();
			// in case of more than one open modal: prevent scrolling the main page after closing the topmost modal
            if($('.modal').hasClass('in')) {
                $('body').addClass('modal-open');
            }    
        });
    },
    
    open: function() {
        
        // do nothing if no dialog is initializing
        if (!Dialogs.initializing) {
            return;
        };
        
        var self = this;
        
        // do nothing if no URL is provided
        if (typeof this._options.url === 'undefined') {
            return;
        }
        
        // if a real URL is provided get the modal content via Ajax
        if ((this._options.url).indexOf("http") === 0) {
            $.ajax({
                url : Dialogs.fixURLOrigin(this._options.url),
                data: this._options.formData,
                type: 'post',
                datatype: 'text/html',
                success: function(data) {
                    if (Dialogs.ajaxResponseEvaluation(data)) {
                        self.init(data);
                    }
                }
            });
        }
        // else use the the provided URL option as a selector
        else {
            this.init($(this._options.url).html());
        }

        $(':focus').trigger('blur');
    },
    
    init: function(modalContent) {
        
        var modal = this._options.modal;
        modal.html(modalContent);
        Validation.bind(modal.find('form.bv-form'));
        modal.find('.captcha').captcha();
        modal.find('textarea[data-charactercounter]').trigger('keyup');
        $('[data-toggle="popover"]', $(modal)).popover({trigger: 'hover'});
        
        if (this._options.callback && typeof this._options.callback == "function") {
            modal.on('shown.bs.modal', this._options.callback(modalContent));
        }
        
        modal.modal('show');
        // fix to prevent modals that cannot be scrolled on the iPhone
        if (/iPhone|iPod|iPad/.test(navigator.userAgent)) {
            $('body').css('overflow','hidden');
            $('body').css('position','fixed');
        }
        
        // set the dialog initializing flag back to 'false' once the dialogs initializing is finished
        Dialogs.initializing = false;
    }
}

// init a Dialogs object that handles global dialog functions
var Dialogs = {};

// flag to indicate a dialog is already initializing to prevent duplicated dialog creation
Dialogs.initializing = false;

/**
 * change the url origin to match the origin of the current location to prevent Ajax same origin policy violations
 * (this might not work well for content that requires to be loaded via https)
 **/
Dialogs.fixURLOrigin = function(url) {
    var locationSplit = location.href.split("/");
    var locationOrigin = locationSplit[0] + '//' + locationSplit[2];
    var urlSplit = url.split("/");
    var urlOrigin = urlSplit[0] + '//' + urlSplit[2];
    if (urlOrigin !== locationOrigin) {
        return url.replace(urlOrigin, locationOrigin);
    }
    return url;
}

/**
 * evaluates the Ajax response for content that requires different action then rendering it in a dialog
 **/
Dialogs.ajaxResponseEvaluation = function(data) {
    
    // a response with a DOCTYPE tag signals that the whole page needs to be replaced
    if (data.indexOf('<!DOCTYPE html>') !== -1) {
        document.open();
        document.write(data);
        document.close();
        return false;
    }
    
    // a response with a link with the id="redirect" signals that this href should be loaded
    if ($(data).filter('a#redirect').length == 1) {
        window.location.href = $(data).filter('a#redirect').attr(('href'));
        return false;
    }
    
    return true;
}

/**
 * handling the hijacksed response
 **/
Dialogs.hijaxResponseHandler = function(data, target) {
    if (Dialogs.ajaxResponseEvaluation(data)) {
        target.html(data);
        Validation.bind(target.find('form.bv-form'));
        target.find('.captcha').captcha();
        target.find('textarea[data-charactercounter]').keyup();
        $('[data-toggle="popover"]', $(target)).popover({trigger: 'hover'});
    }
}

/**
 * Hijacks form submits inside a dialog in order to open the response within the dialog itself
 **/
Dialogs.formHijaxHandler = function(event) {
    var $form = $(event.target).closest('form');
    var $target = $form.closest('.modal');
    
    if ($form.length) {
        event.preventDefault();
        
        $.ajax({
            type: $form.attr('method'),
            url : Dialogs.fixURLOrigin($form.attr('action')),
            data: $form.serialize(),
            success: function(data) {
                Dialogs.hijaxResponseHandler(data, $target);
            }
        });
    }
}

/**
 * Hijacks links inside a dialog in order to open the response within the dialog itself
 **/
Dialogs.linkHijaxHandler = function(event){
    event.preventDefault();
    var $link = $(event.target);
    var $target = $link.closest('.modal');
    
    $.ajax({
        url : Dialogs.fixURLOrigin($link.attr('href')),
        success: function(data) {
            Dialogs.hijaxResponseHandler(data, $target);
        }
    });
}


$(function() {

    /**
     * Open links etc. with the 'data-dialog' attribute in a dialog/modal
     * if it is not a link element an additional 'data-dialog-href' needs to be provided
     */
    $(document).on('click', 'a[data-dialog], div[data-dialog]', function(event) {
        event.preventDefault();
        var url;
        
        if ($(this).attr('href')) {
            url = $(this).attr('href');
        } else if ($(this).attr('data-dialog-href')) {
            url = $(this).attr('data-dialog-href');
        }
        
        var Dialog = new dialog();
        Dialog.create(url);
        Dialog.open();
    });

    /**
     * Display the result of a form submitted with a button that has a 'data-dialog' attribute in a dialog/modal
     * an additional 'data-dialog-action' at this button will replace the forms default action if provided
     */
    $(document).on('click', 'button[data-dialog]', function(event) {
        event.preventDefault();
        
        var $form = $(this).closest('form');
        if ($(this).attr('data-dialog-form')) {
            $form = $('#' + $(this).attr('data-dialog-form'));
        }
        var url = $form.attr('action');
        if ($(this).attr('data-dialog-action')) {
            url = $(this).attr('data-dialog-action');
        }
        var formData = $form.serialize() + "&" + event.currentTarget.name;
        
        var Dialog = new dialog();
        Dialog.options({
            formData: formData
        });
        Dialog.create(url);
        Dialog.open();
    });

    /**
     * Display the result of a submitted form with the 'data-dialog' attribute in a dialog/modal
     */
    $(document).on('submit', 'form[data-dialog]', function(event) {
        event.preventDefault();
        
        var Dialog = new dialog();
        Dialog.options({
            formData: $(this).serialize()
        });
        Dialog.create($(this).attr('action'));
        Dialog.open();
    });
    


    /**
     * Hijacks links inside a dialog in order to open the content within the dialog itself
     **/
    $(document).on('click', '.modal a[data-hijax]', Dialogs.linkHijaxHandler);

    /**
     * Hijacks form submits inside a dialog in order to open the response within the dialog itself
     **/
    $(document).on('submit', '.modal form:not(.bv-form)[data-hijax]', Dialogs.formHijaxHandler);
    // prevent submitting forms with bootstrapValidator twice - http://formvalidation.io/examples/form-submit-twice/
    $(document).on('submit', '.modal form.bv-form[data-hijax]', function(e){e.preventDefault();});
    $(document).on('success.form.bv', '.modal form.bv-form[data-hijax]', Dialogs.formHijaxHandler);

    /**
     * Updates a hidden input field with the name of the button that submitted the form via Ajax.
     * This is required so the server knows what action is being requested from the form.
     **/
    $(document).on('click', 'form[data-dialog] [type=submit], form[data-dialog] [type=cancel], form[data-dialog] [name=cancel], .modal form[data-hijax] [type=submit], .modal form[data-hijax] [type=cancel], .modal form[data-hijax] [name=cancel]', function(event) {
        $('[data-form-action]', $(event.currentTarget).closest('form')).attr('name', event.currentTarget.name);
    });
});
