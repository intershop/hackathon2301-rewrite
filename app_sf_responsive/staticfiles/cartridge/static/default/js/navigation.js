/**
 * functionality to check whether URL fragment information is present (e.g. after a redirect after login)
 * and if so try to select an according element with a fitting 'data-url-fragment-navigation' value and click it
 **/
$(function() {
	if (location.hash.slice(1)) {
		var element = $('[data-url-fragment-navigation="' + location.hash.slice(1) + '"]')[0];
		if (element) {
			element.click();
		}
	}
});
