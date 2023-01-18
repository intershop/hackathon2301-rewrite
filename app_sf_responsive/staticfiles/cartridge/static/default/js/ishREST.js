
/*
 * Wrapper class to using a jQuery REST plugin
 * @constructor
 */
ISHRestClient = function() {
    
    // Base url parts for all REST call
    this.hostUri = null;
    this.restPrefix = null;
    
    // Basic authorization for the REST call
    this.basicAuthorization = null;
    
	// Ressource path for the REST call
	this.restResource = null;
	
	// Url part of the current customer like 'customers/AgroNet/users/fbirdo@test.intershop.de' for B2B or 'customers/patricia' for B2C
	this.customerUrlPart = null;
	
    // Defines the default options for the REST call (jQuery $.ajax() request)
    this.options = {
    	url: null,
    	dataType: 'json',
    	contentType: "application/json",
    	headers: {
	    	Accept: 'application/json'
    	}
    }
    
    // If available, some needed values from RESTConfiguration Class will be set.
    if (typeof RESTConfiguration !== 'undefined') {
        
        // Set the hostUri (e.g.: 'https://localhost:443/INTERSHOP/rest/WFS')
        this.hostUri = RESTConfiguration.getRESTClientHost();
        
        // Set the hostrestPrefix (e.g.: 'inSPIRED-inTRONICS_Business-Site/-')
        this.restPrefix = RESTConfiguration.getRESTClientPrefix();
        
        // Set the header 'authentication-token'
        if (typeof RESTConfiguration.AuthenticationToken !== 'undefined') {
           this.options.headers['authentication-token'] = RESTConfiguration.AuthenticationToken;
        }
        
        // Set the URL part with customer and user is available
        this.customerUrlPart = RESTConfiguration.getCustomerUrlPart();
        
    }
    
	// Defines an object with all registered ajax error callback functions when $.ajax is finished
	// Example:
	// 	{
	// 		statusCode: 401,
	//		function(request, textStatus, errorThrown) {
	//			
	// 		}
	// 	}
    this.registeredErrorCallbackFunctions = [];

}

/*
 * Definition of method of the wrapper class
 * @methods
 */
ISHRestClient.prototype = {

    /*
     * Setter and Getter for REST call host url
     * @method host
     * @return {object} If not defined a parameter the method will return the instance object of the REST client. By calling the method with a parameter the method will return the current base url.
     * @param {string} hostUri String with part of the REST url
     * @example
     *      client.host('/INTERSHOP/rest/WFS');
     * @chainable      
     */
    host: function(hostUri) {
        
        if (typeof hostUri !== 'undefined' && typeof hostUri === 'string') {
            this.hostUri = hostUri;
            return this;
        }

        return this.hostUri;
    },

    /*
     * Setter and Getter for REST call prefix
     * @method prefix
     * @return {object} If not defined a parameter the method will return the instance object of the REST client. By calling the method with a parameter the method will return the current base url.
     * @param {string} restPrefix String with part of the REST url
     * @example
     *      client.prefix('inSPIRED-inTRONICS_Business-Site/-');
     * @chainable
     */
    prefix: function(restPrefix) {
        
        if (typeof restPrefix !== 'undefined' && typeof restPrefix === 'string') {
            this.restPrefix = restPrefix;
            return this;
        }

        return this.restPrefix;
    },

	/*
	 * Setter and Getter for REST call header data
	 * @method header
	 * @return {object} If not defined a parameter the method will return the instance object of the REST client. By calling the method with a parameter the method will return the current header data (json) object.
	 * @param {object} headerData Object with all header data in a key value map.
	 * @example
	 *     client.header({
               "Accept": "application/json",
               ...
           });
	 */
	header: function(headerData) {
		
		if (typeof headerData !== 'undefined' && typeof headerData === 'object') {
	    	this.options.headers = $.extend(true, this.options.headers, headerData);
	    	return this;
	    }

	    return this.options;
	},

	/*
	 * Getter and Setter for REST call request data
	 * @method data
	 * @return {object} If not defined a parameter the method will return the instance object of the REST client. By calling the method with a parameter the method will return the current request data (json) object.
	 * @param {object} data JSON object with all properties
     * @example
     *     client.data({
               "param1": "...",
               "param2": "...",
               ...
           });
	 */
	data: function(data) {
		
		if (typeof data !== 'undefined' && typeof data === 'object') {
		    this.options.data = $.extend(true, this.options.data, data);
		    return this;
		}

	    return this.data;
	},

	/*
	 * Get the full REST request uri with base uri and the complete ressource path
	 * @method uri
	 * @return {string} Returns the uri as a string.
	 * @example
	 *     client.uri();
	 */
	uri: function() {

		if (typeof this.restPrefix !== 'undefined' && typeof this.restResource !== 'undefined') {
		    
		    // Replace double existing string like the restPrefix in response link uri's 
		    // Exists in resource type 'ResourceCollection'
		    this.restResource = this.restResource.replace(this.restPrefix+'/', '');
		    
		    var uri = this.host() + '/' + this.restPrefix;
		    
		    // Is available the URL part for customer and user will be set automatically
		    // So the developer only have to set the REST resource for some calls
		    if (typeof this.customerUrlPart === 'string') {
		        var uri = uri + '/' + this.customerUrlPart + this.restResource;
		    }else{
		        var uri = uri + this.restResource;
		    }
            
		    // Remove double slashes in URL
		    uri = uri.replace('//', '/');
            
		    return uri;
		    
		}
		
	    return this;
	},

	/*
	 * Set the basic authorization (e.g. 'Basic xxxxxxxxx=') for the REST call
	 * @method auth
	 * @param {string} auth String with the basic authorization.
	 * @return {object} Returns the current inctance of the REST client.
	 * @example
	 *     client.auth('Basic xxxxxxxxx=');
	 */
	auth: function(auth) {

		if (typeof auth !== 'undefined') {
			this.basicAuthorization = auth;
			this.options.headers.Authorization = auth;
			return this;
		}

	    return this.basicAuthorization;
	},

	/*
	 * Set the authentication-token like this structure 'demo@PLAIN:lcmI551YIjY=|VDcwS0RnQVZJcXNBQUFGV0Y0R3owQWM0QDE0NjkxODA0NTQ2NDU='
	 * @method token
	 * @return {object} Returns the current inctance of the REST client.
     * @example
     *     client.token('demo@PLAIN:lcmI551YIjY=|VDcwS0RnQVZJcXNBQUFGV0Y0R3owQWM0QDE0NjkxODA0NTQ2NDU=');
	 */
	token: function(token) {

		if (typeof token !== 'undefined' && token !== '') {
			this.options.headers['authentication-token'] = token;
			delete this.options.headers.Authorization;
			return this;
		}
		
	    return this.options.headers['authentication-token'];
	},

    /*
     * Register a handler function for a specific ajax error code.
     * @method error
     * @return {object} Return the instance object of the REST client.
     * @example
     *      client.error('401', function(jqXHR, textStatus, errorThrown) {
     *          // ... customized code
     *      });
     */
    error: function(statusCode, handler) {
        
        if (typeof statusCode !== 'undefined' && typeof handler !== 'undefined' && typeof handler === 'function') {
            this.registeredErrorCallbackFunctions.push({
                statusCode: statusCode,
                handler: handler
            });

            return this;
        }

        return this.registeredErrorCallbackFunctions;

    },

    /*
     * Set the url part for the current customer
     * @method customer
     * @return {object} Return the instance object of the REST client.
     * @example
     *      client.customer('customers/AgroNet/users/fbirdo@test.intershop.de');
     */
    customer: function(urlPart) {
        
        if (typeof urlPart !== 'undefined') {
            
            if (!urlPart) {
                
                var urlPart = null;
                
            }else{
                
                // If set then remove the slash at the end of  the urlPart
                if (urlPart.substr(urlPart.length-1,1) === '/') {
                    var urlPart = urlPart.substr(0,urlPart.length-1);
                }
                
            }
            
            this.customerUrlPart = urlPart; 
            
            return this;
        }
        
        return encodeURI(this.customerUrlPart);

    },

	/*
	 * Calls a request with method 'POST' with all set options and headers
	 * @method create
	 * @param {string} resource String with the called REST resource.
	 * @param {string} requestData JSON with all request data that be sent.
	 * @return {object} Returns the response as a jQuery.ajax() promise object.
	 * @example
     *      client.create('/recurringorders', {
                "param1": "...",
                ...
            }).then(function(data) {
                // ... custom code with response data
            });
	 */
	create: function(resource, requestData) {
		
		// Set the method for the REST call
		this.options.method = 'POST';
		
	    // Set the REST resource path
	    if (typeof resource === 'string') {
	        this.restResource = resource;
	    }

	    // Defines the data object for the REST call
	    if (typeof requestData !== 'undefined' && typeof requestData === 'object') {
	        this.data(requestData);
	    }
	    
		// Triggers the REST call
		return this.__call();

	},

	/*
	 * Calls a request with method 'GET' with all set options and headers
	 * @method get
     * @param {string} resource String with the called REST resource.
     * @return {object} Returns the response as a jQuery.ajax() promise object.
     * @example
     *      client.get('/recurringorders').then(function(data) {
                // ... custom code with response data
            });
	 */
	get: function(resource) {
		
	    if (typeof resource === 'string') {
	        
	        // Set the REST resource path
	        this.restResource = resource;
	    
    		// Set the method for the REST call
    		this.options.method = 'GET';
    
    		// Triggers the REST call
    		return this.__call();
    		
	    }
	    
	    return this;

	},

	/*
	 * Calls a request with method 'PUT' with all set options and headers
	 * @method update
     * @param {string} resource String with the called REST resource.
     * @param {string} requestData JSON with all request data that be sent.
     * @return {object} Returns the response as a jQuery.ajax() promise object.
     * @example
     *      client.update('/recurringorders', {
                "param1": "...",
                ...
            }).then(function(data) {
                // ... custom code with response data
            });
	 */
	update: function(resource, requestData) {

		// Set the method for the REST call
		this.options.method = 'PUT';

        // Set the REST resource path
        if (typeof resource === 'string') {
            this.restResource = resource;
        }

        // Defines the data object for the REST call
        if (typeof requestData !== 'undefined' && typeof requestData === 'object') {
            this.data(requestData);
        }
        
		// Triggers the REST call
		return this.__call();
		
	},

	/*
	 * Calls a request with method 'DELETE' with all set options and headers
	 * @method delete
     * @param {string} resource String with the called REST resource.
     * @return {object} Returns the response as a jQuery.ajax() promise object.
     * @example
     *      client.del('/recurringorders/123456789').then(function(data) {
                // ... custom code with response data
            });
	 */
	del: function(resource) {
		
		// Set the method for the REST call
		this.options.method = 'DELETE';

        // Set the REST resource path
        if (typeof resource === 'string') {
            this.restResource = resource;
        }

        // Defines the data object for the REST call
        if (typeof requestData !== 'undefined' && typeof requestData === 'object') {
            this.data(requestData);
        }
        
		// Triggers the REST call
		return this.__call();

	},

    /*
     * Resolve all links in the response and calls seperate get requests for each links.
     * The result is a collection with the response of each link requests.
     * @example 
     *      client.getElements('/recurringorders').then(function(data) {
                // ... custom code with all response data of all each link requests
            });
     * @example Example fora typical response link list
     *      {
              "elements": [
                {
                  "type": "Link",
                  "uri": "inSPIRED-inTRONICS_Business-Site/-/customers/AgroNet/users/fbirdo@test.intershop.de/quoterequests/OmwKDgAhF5IAAAFTgy8bsDHK",
                  "title": "OmwKDgAhF5IAAAFTgy8bsDHK"
                },
                {
                  "type": "Link",
                  "uri": "inSPIRED-inTRONICS_Business-Site/-/customers/AgroNet/users/fbirdo@test.intershop.de/quoterequests/brcKDgAhwXEAAAFTti0bsDHO",
                  "title": "brcKDgAhwXEAAAFTti0bsDHO"
                }
              ],
              "type": "ResourceCollection",
              "name": "quoteRequests"
            }
     * @method getElements
     * @param resource
     * @return {object} Returns the response as a jQuery.ajax() promise object.
     */
    getElements: function(resource) {
        
        var that = this;
        
        return new RSVP.Promise(function(resolve, reject) {
            
            that.get(resource).success(function(json) {
                
                var promises = [];
                
                json.elements.forEach(function(value) {
                    var promise = that.get(value.uri);
                    promises.push(promise);
                });
                
                var innerResult = RSVP.all(promises).then(function(elements) {
                    resolve({elements: elements});
                }, function(jqXHR) {
                    jqXHR.then = null;
                    reject(jqXHR);
                });
                
                return innerResult;
                
            });
            
        });
        
    },

    /*
     * Get an optimized collection of all given response attributes. 
     * @method getReducedAttrCollection
     * @param {array} attributes The list of all attributes of a response.
     * @return {array} Returns a optimized collection of all given response attributes.
     * @example
     *      client.del('/recurringorders/123456789').then(function(data) {
                // ... custom code with response data
            });
     */
    getReducedAttrCollection: function(attributes) {
        
        var list = {
            elements: []
        };
        
        if (typeof attributes !== 'undefined') {
            
            for (var e=0; e < attributes.length; e++) {
                var elm = attributes[e];
                var title = attributes[e].title;
                
                list.elements[e] = {};
                list.elements[e]['id'] = title;
                
                for (var a=0; a < elm.attributes.length; a++) {
                    var attr = elm.attributes[a];
                    
                    list.elements[e][attr.name] = attr.value;
                    
                }
                
            }
            
        }
        
        return list;
        
    },

	/*
	 * Triggers the calling of the REST call by jQuery.ajax()
	 * @method __call
	 * @return {object} Returns the promise object of the jQuery ajax mehtod.
	 */
	__call: function() {

		var client = this;

		// Set the request url for the ajax request
		this.options.url = this.uri();

		// Updated the current authentication-token in REST client
		this.options.success = function(data, textStatus, request){

			if(typeof request.getResponseHeader('authentication-token') !== 'undefined') {
				client.token(request.getResponseHeader('authentication-token'));
			}

		};
		
		this.options.error = function (jqXHR, textStatus, errorThrown) {

			$(client.registeredErrorCallbackFunctions).each(function(index, callback){
				
				if (jqXHR.status >= 400 && callback.statusCode === jqXHR.status) {
					callback.handler(jqXHR, textStatus, errorThrown);
				}

			});

		};
		
		// Convert JSON request data to an JSON string
		if (typeof this.options.data !== 'undefined' && typeof this.options.data === 'object') {
		    this.options.data = JSON.stringify(this.options.data);
		}
		
		// Call the jQuery ajax request
		var request = $.ajax(this.options);
		
		return request;
	}

};
