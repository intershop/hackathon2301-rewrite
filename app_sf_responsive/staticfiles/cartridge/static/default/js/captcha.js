var KOR;
(function($) {

    if(typeof(KOR) == 'undefined'){
        /**
         * class for the jsu library. does nothing right now.
         * Used as a namespace. Used as a namespace. Don't Bother creating an instance of KOR.
         * @namespace KOR
         * @static
         */
        KOR = function(){return};
    };

	KOR.Captcha = {
		render: function(element, library){
			this['render' + (library || this.library)](element)
		}
	}
	$.extend(KOR.Captcha, {
		library: 'ReCaptcha',
		
		renderReCaptcha: function(element){
			var id = element.attr('id');
			if (!id){
				id = ["captcha-", parseInt(Math.random()*1000000), new Date().getTime()].join('');
				element.attr('id', id);
			}
			Recaptcha.create(this.reCaptchaKey, id, this.reCaptchaOptions || {});
		}
	});

	$.fn.captcha = function(){
		$(this).each(function(){
			KOR.Captcha.render($(this));
		});
	}
})(jQuery);
