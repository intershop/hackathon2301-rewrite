//category.js file is used for all client side operations on Category and search page.
var Category = {};
var mobileDevice = false;
// Check user agent
if((navigator.userAgent.match(/iPhone/i)) || (navigator.userAgent.match(/iPod/i)) || (navigator.userAgent.match(/iPad/i)) || (navigator.userAgent.match(/Android/i)) || (navigator.userAgent.match(/windows phone/i))) {
    mobileDevice = true;
}
/*
 * Registering all the methods on initialization.
 */
Category.init = function() {
    Category.onPageScroll();
    Category.onClickFilterChekbox();
}

/**
 * More products load on page scroll down
 */
Category.onPageScroll = function() {
    
    // Default page size is set 12 products per page and on page scroll we are getting next page products by AJAX and display
    var paginationCounter = $('#pagination-counter');
    var processScroll = false;
    var nextPage;
    // Check page count
    if(paginationCounter.data('current-page') < (paginationCounter.data('page-count') - 1)){
        processScroll = true;
        nextPage = paginationCounter.data('next-page');
    }
    $(window).on('scroll', function() {
        // To check browser scroll position when its reached last row of product list then then start AJAX for more products if available 
        if (processScroll && $(window).scrollTop() > $(document).height() - ($(window).height() + $('footer').height() + 1.5*$(".product-list li:first-child").height())) {    
            processScroll=false;
            // Display products loading icon
            $('#ProductsloaderDiv').show();
            $.ajax({
                url:        nextPage,
                dataType:   "xml",
                success:    function(data) {
                    // Hide products loading icon
                    $('#ProductsloaderDiv').hide();
                    // Append new products list  
                    $(".product-list").append($(data).find("content ul").text());
                    nextPage = $(data).find("next").text(); 
                    // To fixed left filter panel and Sort by row
//                    if(!mobileDevice){
//                        navscroll();
//                    }
                    
                    // To check more product are available
                    if(!nextPage || /^\s*$/.test(nextPage)) {
                        processScroll=false;
                    }
                    else {
                        processScroll=true;
                    }
                }
            });
        }else{
            // To fixed left filter panel and Sort by row
//            if(!mobileDevice){
//                navscroll();
//            }
        }
    });
}

/**
 * Select multiple checkbox from Left panel filter
 */
Category.onClickFilterChekbox = function(){
    $('[type="checkbox"].filter-checkbox').on('change', function(){
        window.location.href = $(this).data('document-location'); 
    });
}
/**
 * Fixed left filter panel and Sort by row on page scroll down
 */
function navscroll(){
    var workingareawidth = $('.main-container').width()+30;
    var contentpanHeight=$('ul.product-list').height(); // Height of Product list container
    var leftfilter=$('.filter-panel'); 
    var filterRow = $('.filters-row');
    var leftfilterH=$(leftfilter).height() + $('.filterRow').height(); // Height of Left Filter panel + Height of SortBy row div
    var windowH=$(window).height(); // Height of Window screen
    var contentAndLeftPanelHeightDiff=(contentpanHeight-leftfilterH); // Height difference between Product list container and Left Filter panel 
    var windowScrollheight=($(window).scrollTop() - 268); // Height difference between scroll height from top and Height upto SortBy row from top
    var scrollHeightFromSortBy=windowScrollheight-leftfilterH; // Height difference between left panel filter and scroll height SortBy row div
    
    // If window height is greater than left panel filter height
    if (windowH > leftfilterH) {
        //Page scroll upto SortBy row div
        if (windowScrollheight > 0) {
            // If page is scroll upto bottom and product is not loading
            if ((contentAndLeftPanelHeightDiff) < windowScrollheight) {
                leftfilter.css({
                    'position': 'relative',
                    'top': windowScrollheight
                });
                filterRow.css({
                    'position': 'relative',
                    'top': windowScrollheight,
                    'width': 'auto'
                });
            } else {
                // Fixed Position of Left filter panel and SortBy row div
                leftfilter.css({
                    'position': 'fixed',
                    'top': 80
                });
                filterRow.css({
                    'position': 'fixed',
                    'top': 0,
                    'width': workingareawidth
                });
            }
        } else {
            // Reset position of Left filter panel and SortBy row div when reached at top
            leftfilter.css({
                'position': 'static',
                'top': 0
            });
            filterRow.css({
                'position': 'static',
                'top': 0,
                'width': 'auto'
            });
        }
    }
    // If window height is less than left panel filter height
    else {
        // Either product is loading or Page reached up-to footer
        if (windowScrollheight > contentAndLeftPanelHeightDiff) {
            leftfilter.css({
                'position': 'relative',
                'top': contentAndLeftPanelHeightDiff
            });
            filterRow.css({
                'position': 'relative',
                'top': contentAndLeftPanelHeightDiff
            });
        } else {
            // After products loading fixed position of Left filter panel and SortBy row div
            if (scrollHeightFromSortBy > 0) {
                leftfilter.css({
                    'position': 'fixed',
                    'top': 80
                });
                filterRow.css({
                    'position': 'fixed',
                    'top': 0,
                    'width': workingareawidth
                });
            } else {
                // On page scroll down first time fixed position of Left filter panel and SortBy row div
                if (windowScrollheight > 0) {
                    leftfilter.css({
                        'position': 'fixed',
                        'top': 80
                    });
                    filterRow.css({
                        'position': 'fixed',
                        'top': 0,
                        'width': workingareawidth
                    });
                }
                // Reset position of Left filter panel and SortBy row div when reached at top
                else {
                    leftfilter.css({
                        'position': 'static',
                        'top': 0
                    });
                    filterRow.css({
                        'position': 'static',
                        'top': 0,
                        'width': 'auto'
                    });
                }
            }
        }
    }
}

$(document).ready(function() {
    Category.init(); 
});
