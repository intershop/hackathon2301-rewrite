
/*
 * Defines a class to handle controllers for the template engine handlebars
 * @constructor
 * @param name {string} Defines the name of the handlebars module which is to be rendered
 */
ishHbsController = function(name) {

    // Defines the id of the controller
    this.id = name;

	// Defines the html dom of the related controller
	this.source = $("#"+this.id);

	// Defines the compiled handlebars template
	this.compiledTemplateSource = null;

	// Defines the model of the component
	this.modelData = {};
	this.filteredModelData = null;

	// Defines the generated html structure of the component after handlebars compile
    this.html = {};

    // Defines a dom element will can be replaced with the generated handlebars template
    this.template = null;

    // Defines the customized source
    this.controllerSource = function(){};

    // Debugging
    this.debugging = false;

}

/*
 * Defenition of method of the handlebars controller class
 * @methods
 */
ishHbsController.prototype = {

    /*
     * Initialization of the controller and precompile the handlebars template
     * @method init
     */
    init: function() {

	    // Compile template
		this.compiledTemplateSource = Handlebars.compile(this.source.html());

        if (this.debugging) {
            console.info('Initialized "'+this.id+'Controller".');
        }

	},

	/*
     * Debugging
     * @method debug
     * @chainable
     */
    debug: function() {

        // Sets the property 'debugging' on true for this execution
        this.debugging = true;

        return this;

    },

	/*
     * Update the model data and triggers the update method of the controller
     * @method model
     * @param {object} data Defines a JSON object with the data
     * @chainable
     */
    model: function(data) {

        // Set model data
        if (typeof data !== 'undefined') {
            this.modelData = data;

            return this;
        }

        return this.modelData;

    },

	/*
	 * Update the precompiled handlebars template with the model
	 * @method update
	 * update the dom structure
	 */
	update: function() {

	    // Generated html dom from handlebars

	    if (this.filteredModelData !== null) {
	        var currentModelData = this.filteredModelData;
	    }else{
	        var currentModelData = this.modelData;
	    }

	    this.html = this.compiledTemplateSource(currentModelData);

	    // Replace html comments
	    this.html = this.html.replace(/<!--[\s\S]*?-->/g, "");

	    if (!this.template) {
	        this.template = $(this.html);
	    }else{
	        this.template.remove();
            this.template = $(this.html);
	    }

        $(this.template).insertAfter(this.source);

        if (this.debugging) {
            console.info('Template updated for "'+this.id+'Controller".');
        }

        return new RSVP.Promise(function(resolve, reject) {
            resolve(currentModelData);
        });

	},

    /*
     * Customized user controller code for the current Handlebars controller
     * @method controller
     * @param {object} source Customized user controller code for this Handlebars controller
     * @chainable
     */
    controller: function(source) {

        if (typeof source !== 'undefined') {

            if (this.debugging) {
                console.info('Customized hbs controller set for "', this.id+'Controller".');
            }
            
            this.controllerSource = source;
            this.controllerSource.apply(this, arguments);
        }

        return this;

    },

    /*
     * Customized user methods for the current Handlebars controller instance
     * @method extend
     * @param {object} methods Customized user methods with all defined methods
     * @chainable
     */
    extend: function(methods) {

        if (typeof methods !== 'undefined' && typeof methods === 'object') {

            for (var method in methods) {
                if(typeof this[method] === 'undefined' && methods.hasOwnProperty(method)) {
                    $.extend( true, this, methods[method] );
                }
            }

        }

        return this;

    },

    /*
     * Method to filter the current model data
     * @method filterElements
     * @param {function} handler Filter handler function to manipulate the current model data
     * @param {object} model Updated the original model data before the filter will be applied
     * @chainable
     */
    filterElements: function(handler, model) {

        var self = this;

        // Set model data if defined
        if (typeof model !== 'undefined') {
            this.modelData = model;
        }

        // Invoke the filter handler function
        if (typeof handler !== 'undefined' && typeof handler === 'function') 
        {
            
            if (typeof this.modelData.elements !== 'undefined') {
                
                var filteredList = this.modelData.elements.filter(handler);
                this.filteredModelData = $.extend(true, this.filteredModelData, this.modelData);
                this.filteredModelData.elements = filteredList;
                
            }else{
                
                console.info('No elements list available in current model.');
                
            }
            
        }else{
            this.filteredModelData = null;
        }

        return this;

    },

    /*
     * Get the currently filtered element list
     * @method filteredModel
     * @param {object} data Optional - Defines a JSON object with the data
     * @chainable
     */
    filteredModel: function(model) {

        if (typeof model !== 'undefined') {
            this.filteredModelData = model;

            return this;
        }

        return this.filteredModelData;

    }

};
