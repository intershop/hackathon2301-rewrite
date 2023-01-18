/*global jQuery:true, intershop:true */
'use strict';
!function (global, utils, $) {
    var ui = utils.namespace('intershop.propertygroups.ui'),
        container = utils.namespace('intershop.propertygroups.ui.container'),
        template = '' + 
        '<div class="propertygroup-property form-group">' +
            '<div class="propertygroup-property-helptext"></div>' +
            '<div class="propertygroup-property-suberror"></div>' +
            '<div class="propertygroup-property-subdescription"></div>' +
            '<label class="control-label col-sm-4">' +
                '<input class="propertygroup-property-checkbox" type="checkbox" value="active" />' +
                '<span class="propertygroup-property-displayname"></span>' +
            '</label>' +
            '<div class="propertygroup-property-content col-sm-6"></div>' +
            '<div class="propertygroup-property-error col-sm-offset-4 col-sm-6"></div>' +
            '<div class="propertygroup-property-description col-sm-offset-4 col-sm-6"></div>' +
        '</div>';

    /**
     * Create a new Property container.
     * @param {Object} json Configuration of the Property
     */
    var Property = function Property(json, editor, subPropertyGroup) {
        this.fromJSON(json);
        this.editor = editor;
        this.subPropertyGroup = subPropertyGroup;
    };

    /**
     * Load configuration from JSON.
     * @param {Object} originJSON configuration as JSON
     * @private
     */
    Property.prototype.fromJSON = function fromJSON(originJSON) {
        var json = $.extend(true, {}, originJSON);

        this.invalid = json.invalid || false;
        this.id = json.id || '';
        this.mandatory = json.mandatory || false;
        this.hidden = json.hidden || 'never'; // never = default value = not hidden
        this.displayName = json.displayName;
        this.description = json.description;
        this.helpText = json.helpText;
        this.errorMessages = json.errorMessages || [];
        this.subGroupCheckbox = json.subGroupCheckbox || false;
        this.subGroupCheckboxEnabled = typeof json.subGroupCheckboxEnabled !== 'undefined' ? json.subGroupCheckboxEnabled : true;
    };
    
    /**
     * Enable the property.
     */
    Property.prototype.enable = function enable() {
        if (this.subGroupCheckbox) {
            $el.find('.propertygroup-property-checkbox').prop('disabled', false);
        }
        
        if (this.subPropertyGroup) {
            this.subPropertyGroup.enable();
        }
        
        this.editor.enable();
        this.editor.$el.closest('.propertygroup-property').removeClass('propertygroup-property-disabled');
    };

    /**
     * Disable the property.
     */
    Property.prototype.disable = function disable() {
        if (this.subGroupCheckbox) {
            $el.find('.propertygroup-property-checkbox').prop('disabled', true);
        }
        
        if (this.subPropertyGroup) {
            this.subPropertyGroup.disable();
        }
        
        this.editor.disable();
        this.editor.$el.closest('.propertygroup-property').addClass('propertygroup-property-disabled');
    };
    
    /**
     * View (and don't edit) the property.
     */
    Property.prototype.view = function view() {
        if (this.subPropertyGroup) {
            this.subPropertyGroup.view();
        }
        else {
            this.editor.view();
            this.editor.$el.closest('.propertygroup-property').addClass('propertygroup-property-noneditable');
            this.editor.$el.closest('.propertygroup-property').find(".propertygroup-property-description").hide();

            // remove 'hidden' class if only to hide in edit mode
            if (this.hidden == 'edit')
            {
                this.editor.$el.closest('.propertygroup-property').removeClass('hidden');
            }
        }    
    };

    /**
     * Get the jQuery DOM element.
     * @return {jQuery} jQuery DOM elements
     */
    Property.prototype.render = function render() {
        var $el = $($.parseHTML(template)),
            $checkbox = $el.find('.propertygroup-property-checkbox'),
            $errors = $el.find('.propertygroup-property-error'),
            $subErrors = $el.find('.propertygroup-property-suberror'),
            $label = $el.find('.propertygroup-property-displayname'),
            $description = $el.find('.propertygroup-property-description'),
            $subDescription = $el.find('.propertygroup-property-subdescription'),
            $helptext = $el.find('.propertygroup-property-helptext'),
            $content = $el.find('.propertygroup-property-content');
        
        if (this.mandatory && !this.subPropertyGroup) {
            $label.text(this.displayName).append('<span class="required">*</span>');
        }
        else {
            $label.text(this.displayName);
        }

        // set attributes for hiding property
        if (this.hidden && this.hidden != 'never') {
            $el.closest('.propertygroup-property').addClass('hidden');
        }

        if (this.subGroupCheckbox) {
            var that = this;
            $checkbox.show()
                .on('change.propertygroup', function onChange(event) {
                    if ($checkbox.is(':checked')) {
                        that.subPropertyGroup.enable();
                        that.subGroupCheckboxEnabled = true;
                    }
                    else {
                        that.subPropertyGroup.disable();
                        that.subGroupCheckboxEnabled = false;
                    }
                })
                .prop('checked', this.subGroupCheckboxEnabled)
                .change();
        }
        
        if (this.invalid) {
            $el.addClass('propertygroup-property-invalid has-error');
        }
        if (this.description) {
            if (this.subPropertyGroup) {
                $subDescription.text(this.description);
            }
            else {
                $description.text(this.description);
            }
        }
        if (this.helpText) {
            $helptext.text(this.helpText);
        }
        
        if (this.subPropertyGroup) {
            this.errorMessages.forEach(function eachError(msg, i) {
                if (i > 0) $subErrors.append('<br />');
                $($.parseHTML('<span></span>'))
                    .text(msg)
                    .appendTo($subErrors);
            });
        }
        else {
            this.errorMessages.forEach(function eachError(msg, i) {
                if (i > 0) $errors.append('<br />');
                $($.parseHTML('<span></span>'))
                    .text(msg)
                    .appendTo($errors);
            });
        }
        
        if (this.editor && !this.subPropertyGroup) {
            var $editor = this.editor.render();
            if (this.invalid) {
                $editor.addClass('propertygroup-editor-invalid');
            }
            $content.append($editor);
        }
        else {
            if (!this.displayName) {
                $el.addClass('propertygroup-property-ungrouped');
            }
            else {
                $el.addClass('propertygroup-property-subgroup');
            }
            $content.append(this.subPropertyGroup.render());
        }
        
        return $el;
    };

    container.Property = container.Property || Property;
    ui.Controller.registerContainer('Property', Property);
}(window, intershop.propertygroups.utils, jQuery);
