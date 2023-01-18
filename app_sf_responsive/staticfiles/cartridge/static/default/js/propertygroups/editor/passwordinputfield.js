/*global jQuery:true, intershop:true */
'use strict';
!function (global, utils, $) {
	var ui = utils.namespace('intershop.propertygroups.ui'),
		editor = utils.namespace('intershop.propertygroups.ui.editor'),
		template = '' + 
			'<div class="propertygroup-editor propertygroup-editor-passwordinput">' +
				'<input type="password" class="propertygroup-editor-value form-control" />' +
			'</div>';

	/**
	 * Create a new PasswordInputField editor.
	 * @param {Object} json configuration as JSON
	 * @param {String} name name of the editor
	 */
	var PasswordInputField = function PasswordInputField(json, name) {
		this.$el = $($.parseHTML(template));
		this.$el.children('.propertygroup-editor-value').attr('name', name);
		
		Object.defineProperty(this, 'value', {
			configurable: true,
			enumerable: true,
			get: function () {
				return this.$el.children('.propertygroup-editor-value').val();
			},
			set: function (v) {
				this.$el.children('.propertygroup-editor-value').val(v);
			}
		});
		
		this.fromJSON(json);
		this.bindEvents();
	};
	
	/**
	 * Bind events to the DOM element.
	 * @private
	 */
	PasswordInputField.prototype.bindEvents = function bindEvents() {
		var _this = this,
			oldValue;
		
		this.$el.find('.propertygroup-editor-value')
			.on('input.propertygroup', function onInput() {
				_this.invalid = false;
				_this.modified = true;
			})
			.on('focus.propertygroup', function onFocus() {
				if (_this.invalid === _this.modified) {
					// remove old value if no new value was given
					oldValue = _this.value;
					_this.value = '';
				}
			})
			.on('blur.propertygroup', function onBlur() {
				if (_this.invalid === _this.modified) {
					// restore initial value if no new value is given
					_this.value = oldValue;
				}
			});
	};
	
	/**
	 * Load configuration from JSON.
	 * @param {Object} originJSON configuration as JSON
	 * @private
	 */
	PasswordInputField.prototype.fromJSON = function fromJSON(originJSON) {
		var json = $.extend(true, {}, originJSON);
		
		this.modified = json.modified || false;
		this.invalid = json.invalid || false;
		this.value = json.value || '';
	};
	
	/**
	 * Create configuration as JSON.
	 * @param {Object} originJSON old configuration as JSON
	 * @return {Object} updated configuration as JSON
	 */
	PasswordInputField.prototype.toJSON = function toJSON(originJSON) {
        var json = $.extend(true, {}, originJSON);
        
        if (this.$el.closest('.propertygroup-property').hasClass('hidden'))
        {
            if (json.value != this.value)
            {
                this.modified = true;
                json.modified = true;
                json.value = this.value;
            }
        }
        else
        {
            json.modified = this.modified;
            if (this.modified) {
                json.value = this.value;
            }
        }
        
        return json;
	};
	
    /**
     * Enable the editor.
     */
	PasswordInputField.prototype.enable = function enable() {
		this.$el.find('.propertygroup-editor-value').prop('disabled', false);
    };

    /**
     * Disable the editor.
     */
    PasswordInputField.prototype.disable = function disable() {
		this.$el.find('.propertygroup-editor-value').prop('disabled', true);
    };
    
    /**
     * View the value of the editor.
     */
    PasswordInputField.prototype.view = function view() {
    	this.$el.html(''); // never show this field
    	this.$el.closest('.propertygroup-property').hide();
    };

    
	/**
	 * Get the jQuery DOM element.
	 * @return {jQuery} jQuery DOM elements
	 */
	PasswordInputField.prototype.render = function render() {
		return this.$el;
	};
	
	editor.PasswordInputField = editor.PasswordInputField || PasswordInputField;
	ui.Controller.registerEditor('com.intershop.ui.web.capi.property.editor.ui.PasswordInputField', PasswordInputField);
}(window, intershop.propertygroups.utils, jQuery);
