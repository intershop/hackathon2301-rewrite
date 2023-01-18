/*global jQuery:true, intershop:true */
'use strict';
!function (global, utils, $) {
	var ui = utils.namespace('intershop.propertygroups.ui'),
		editor = utils.namespace('intershop.propertygroups.ui.editor'),
		template = '' + 
			'<div class="propertygroup-editor propertygroup-editor-selectbox">' +
				'<select class="propertygroup-editor-value"></select>' +
			'</div>',
		optionTemplate = '<option></option>';

	/**
	 * Create a new SelectBox editor.
	 * @param {Object} json configuration as JSON
	 * @param {String} name name of the editor
	 */
	var SelectBox = function SelectBox(json, name) {
		this.$el = $($.parseHTML(template));
		this.$el.children('.propertygroup-editor-value').attr('name', name);
		
		Object.defineProperty(this, 'value', {
			configurable: true,
			enumerable: true,
			get: function () {
				return this.$el.children('.propertygroup-editor-value').val();
			},
			set: function (v) {
				var $value = this.$el.children('.propertygroup-editor-value');
				$value.children('option')
					.prop('selected', false)
					.filter('[value="' + v + '"]')
						.prop('selected', true);
			}
		});
		
		this.fromJSON(json);
		this.bindEvents();
	};
	
	/**
	 * Bind events to the DOM element.
	 * @private
	 */
	SelectBox.prototype.bindEvents = function bindEvents() {
		var _this = this;
		this.$el.find('.propertygroup-editor-value')
			.on('change.propertygroup', function onChange() {
				_this.modified = true;
			});
	};
	
	/**
	 * Load configuration from JSON.
	 * @param {Object} originJSON configuration as JSON
	 * @private
	 */
	SelectBox.prototype.fromJSON = function fromJSON(originJSON) {
		var json = $.extend(true, {}, originJSON);
		
		this.modified = json.modified || false;
		
		this.options = json.options || [];
		var $select = this.$el.find('.propertygroup-editor-value');
		this.options.forEach(function (option) {
			$($.parseHTML(optionTemplate))
				.val(option.value)
				.text(option.text)
				.addClass(option.cssClass)
				.appendTo($select);
		});
		this.value = json.value || '';
	};
	
	/**
	 * Create configuration as JSON.
	 * @param {Object} originJSON old configuration as JSON
	 * @return {Object} updated configuration as JSON
	 */
	SelectBox.prototype.toJSON = function toJSON(originJSON) {
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
	SelectBox.prototype.enable = function enable() {
		this.$el.find('.propertygroup-editor-value').prop('disabled', false);
	};

	/**
	 * Disable the editor.
	 */
	SelectBox.prototype.disable = function disable() {
		this.$el.find('.propertygroup-editor-value').prop('disabled', true);
	};
	
    /**
     * View the value of the editor.
     */
	SelectBox.prototype.view = function view() {
        this.$el.html(this.$el.find('option:selected').text());    
        if (this.$el.html() == ''){
            this.$el.html('&nbsp;');
        }
    };

	
	/**
	 * Get the jQuery DOM element.
	 * @return {jQuery} jQuery DOM elements
	 */
	SelectBox.prototype.render = function render() {
		return this.$el;
	};
	
	editor.SelectBox = editor.SelectBox || SelectBox;
	ui.Controller.registerEditor('com.intershop.ui.web.capi.property.editor.ui.SelectBox', SelectBox);
}(window, intershop.propertygroups.utils, jQuery);
