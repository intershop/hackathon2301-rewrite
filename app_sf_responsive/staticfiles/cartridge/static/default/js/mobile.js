//mobile.js - include functions needed to modify layout or interactions for mobile / small screen devices.

var Mobile = {};
Mobile.init = function() {
	// Register your mobile functions here
	Mobile.filterNavigation();
	Mobile.accountNavigation();
}

// handle the 'keepFilterOpen' cookie (1/2880 = 30 seconds expiration time)
Mobile.setKeepFilterOpenCookie = function() {
	Cookies.set('keepFilterOpen', $(this).closest('ul').attr('id'), {expires: 1/2880});
}
Mobile.removeKeepFilterOpenCookie = function() {
	Cookies.remove('keepFilterOpen');
}

Mobile.filterNavigation = function() {
	if (Modernizr.mq('(max-width: 767px)')) {
		// collapse all filters by default in mobile view
		$('.filter-panel').addClass('collapse');
		$('.filter-group h3').addClass('collapsed');
		$('.filter-group h3').find('span').removeClass('glyphicon-chevron-down').addClass('glyphicon-chevron-right');
		$(".filter-list").removeClass('in');
		
		// open the filter marked to be kept open in a cookie
		if (Cookies.get('keepFilterOpen')) {
			$('.filter-panel').addClass('in');
			$('.mobile-filter-toggle').removeClass('collapsed');
			$('#'+ Cookies.get('keepFilterOpen')).addClass('in').siblings('h3').find('span').removeClass('glyphicon-chevron-right').addClass('glyphicon-chevron-down');
		}
		
		// bind set and remove cookie functionality to filter navigation links
		$('.search-product-list, .category-panel').on('click', 'a', Mobile.removeKeepFilterOpenCookie);
		$('.search-product-list, .category-panel').on('click', 'a.keepFilterOpen', Mobile.setKeepFilterOpenCookie);
		
	} else {
		// reset filter collapsing and cookie handling for default view
		$('.filter-panel').removeClass('collapse');
		$('.filter-group h3').removeClass('collapsed');
		$('.filter-group h3').find('span').removeClass('glyphicon-chevron-right').addClass('glyphicon-chevron-down');
		$(".filter-list").addClass('in');
		$('.search-product-list, .category-panel').off('click', 'a', Mobile.removeKeepFilterOpenCookie);
		$('.search-product-list, .category-panel').off('click', 'a.keepFilterOpen', Mobile.setKeepFilterOpenCookie);
	}
}

Mobile.accountNavigation = function() {
	if (Modernizr.mq('(max-width: 991px)')) {
	    if ($('.account-nav-box select').length == 0) {
    		$('.account-navigation').each(function() {
    		    var select=$(document.createElement('select')).insertBefore($(this).hide());
    		    $('>li a, >li p', this).each(function() {
    		        if ($(this).hasClass('account-nav-heading')) {
    		            $(document.createElement('optgroup')).appendTo(select).attr('label',$(this).text());
    		        } else {
    		            if($(this).closest('li').hasClass('active')){
    		                $(document.createElement('option')).attr('selected', 'selected').appendTo(select).val(this.href).html($(this).html());
    		            }else{
    		                $(document.createElement('option')).appendTo(select).val(this.href).html($(this).html());
    		            }
    		            $("select small").parent().hide();
    		        }
    		    });
    		    select.addClass('form-control');
    		    select.change(function(){
    		        window.location.href = this.value;
    		    })
    		});
	    }
	} else {
		$(".account-nav-box").find("select").remove();
		$('.account-nav-box .account-navigation').show();
	}
}

//initialize all mobile functions
$(document).ready(function() {
	Mobile.init();
	Mobile.windowWidth = $(window).width();
});

// register the functionality that needs to be re-evaluated when the 
// screen size changes (e.g. switch from portrait to landscape)
$(window).on('resize', function(e){
	if ($(window).width() != Mobile.windowWidth) {
		Mobile.windowWidth = $(window).width();
		Mobile.accountNavigation();
		Mobile.filterNavigation();
	}
});
