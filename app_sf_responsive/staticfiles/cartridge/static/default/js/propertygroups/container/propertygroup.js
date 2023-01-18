/*global jQuery:true, intershop:true */
'use strict';
!function (global, utils, $) {
	var ui = utils.namespace('intershop.propertygroups.ui'),
		container = utils.namespace('intershop.propertygroups.ui.container'),
		template = '' + 
		'<div class="propertygroup">' +
			'<div class="propertygroup-error"></div>' +
			'<div class="propertygroup-description"></div>' +
			'<div class="propertygroup-content"></div>' +
		'</div>';

	/**
	 * Create a new PropertyGroup container.
	 * @param {Object} json Configuration of the PropertyGroup
	 */
	var PropertyGroup = function PropertyGroup(json) {
		this.fromJSON(json);
	};

	/**
	 * Load configuration from JSON.
	 * @param {Object} originJSON configuration as JSON
	 * @private
	 */
	PropertyGroup.prototype.fromJSON = function fromJSON(originJSON) {
		var json = $.extend(true, {}, originJSON);
		
		this.invalid = json.invalid || false;
        this.id = json.propertyGroupID || '';
        if (json.ownerID)
        {
            this.id = json.ownerID + '_' + json.propertyGroupID;
        }
		this.displayName = json.displayName || this.id;
		this.description = json.description;
		this.properties = [];
		this.errorMessages = json.errorMessages || [];
	};
	
	/**
	 * Add a property.
	 * @param {Object} property
	 */
	PropertyGroup.prototype.add = function add(property) {
		this.properties.push(property);
	};
	
	/**
	 * Get a property by its ID.
	 * @param {String} ID of the property
	 * @return {Object} property
	 */
	PropertyGroup.prototype.get = function get(id) {
		var found = this.properties.filter(function filterProperty(property) {
			return property.id === id;
		});
		return found.length === 1 ? found[0] : undefined;
	};
	
	/**
	 * Enable the propertygroup and its properties.
	 */
	PropertyGroup.prototype.enable = function enable() {
		this.properties.forEach(function eachProperty(property) {
			property.enable();
		});
	};
	
	/**
	 * Disable the propertygroup and its properties.
	 */
	PropertyGroup.prototype.disable = function disable() {
		this.properties.forEach(function eachProperty(property) {
			property.disable();
		});
	};
	

    /**
     * View (instead of editing) the properties of the property group.
     */
    PropertyGroup.prototype.view = function view() {
        this.properties.forEach(function eachProperty(property) {
            property.view();
        });
    };
	
	/**
	 * Get the jQuery DOM element.
	 * @return {jQuery} jQuery DOM elements
	 */
	PropertyGroup.prototype.render = function render() {
		var $el = $($.parseHTML(template)),
			$error = $el.find('.propertygroup-error'),
			$description = $el.find('.propertygroup-description'),
			$content = $el.find('.propertygroup-content');
		
		if (this.invalid) {
			if (this.errorMessages.length > 0) {
				$error.text(this.errorMessages.join('<br />')).show();            
			}
			else {
				$error.text(PropertyGroup.validationError).show();
			}
		}
		
		if (this.description) {
			$description.html(this.description);
		}
		
		this.properties.forEach(function eachProperty(property) {
			property.render().appendTo($content);
		});
		
		return $el;
	};
	
	container.PropertyGroup = container.PropertyGroup || PropertyGroup;
	ui.Controller.registerContainer('PropertyGroup', PropertyGroup);
}(window, intershop.propertygroups.utils, jQuery);
