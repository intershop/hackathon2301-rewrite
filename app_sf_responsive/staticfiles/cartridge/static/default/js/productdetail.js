var ProductDetail = {};


ProductDetail.init = function () {
    ProductDetail.productImageCarousel();
    ProductDetail.onClickRatingStar();
    ProductDetail.productVariationHandler();
    ProductDetail.productVariationSwatchHandler();
};

// Carousel for Product Detail Main Images
ProductDetail.productImageCarousel = function () {
    var imgset = 1;
    var productimages = $(".product-img-inner > picture");
    for (var j = 0; j < productimages.length; j += imgset) {
        productimages.slice(j, j + imgset).wrapAll("<div class='item'></div>");
    }
    var productthumbs = $(".product-img-thumbs img");
    for (var i = 0; i < productthumbs.length; i += imgset) {
        var productthumb = productthumbs.slice(i, i + imgset);
        // add a product-thumb-set frame only if not added yet
        if (!productthumb.parent().hasClass('product-thumb-set')) {
            var carousel = productthumb.closest('.row.product-details').find('.carousel.slide');
            productthumb.wrapAll("<div class='product-thumb-set' data-target='#" + carousel.attr('id') + "'></div>");

            // handle when the carousel is about to slide from one item to another to set the active image thumb
            carousel.on('slide.bs.carousel', function (event) {
                var from = $(event.target).find('.carousel-inner > .item.active').index();
                var to = $(event.relatedTarget).index();
                var carouselID = carousel.attr('id');

                $(".product-thumb-set[data-target='#" + carouselID + "'][data-slide-to='" + from + "']").removeClass('active');
                $(".product-thumb-set[data-target='#" + carouselID + "'][data-slide-to='" + to + "']").addClass('active');
            });
        }
    }

    // add data-slide-to attribute
    $.each($('.row.product-details'), function () {
        $.each($(this).find('.product-thumb-set'), function (index, value) {
            $(this).attr('data-slide-to', index);
        });
    });
    // make first thumb active if none is currently active
    $.each($('.product-img-thumbs'), function (index, value) {
        if ($(this).find('.product-thumb-set.active').length == 0) {
            $(this).find('.product-thumb-set:first-child').addClass('active');
        }
    });
    // handle clicking on thumbs
    $('.product-thumb-set').on('click', function () {
        $(this).parent().find('.product-thumb-set').removeClass('active');
        $(this).addClass('active');
    });
    // make first set active
    $.each($('.carousel-inner.product-img-inner'), function () {
        if ($(this).find('.item.active').length == 0) {
            $(this).find('.item:first-child').addClass('active');
        }
    });
};

/**
 * Update star field value and remove error message if display regarding star field
 */
ProductDetail.onClickRatingStar = function () {
    $(document).on('click', '.rating-input a', function () {
        // Set star value in hidden field
        $('input[name=' + $(this).data('field-name') + ']').val($(this).data('value'));
        // Set active class
        $('.rating-input').attr('class', 'rating-input ' + $(this).data('parent-class'));
        var formGroup = $(this).closest('.form-group');
        // Remove error class from container
        $(formGroup).removeClass('has-error');
        $(formGroup).find('.rating-star-required').remove();
        // Submit button enabled
        if (!($(this).closest('form').find('.has-error').length > 0)) {
            $(this).closest('form').find('button[type=submit]').prop('disabled', false);
        }
    });
};

/**
 *  Get the content of a changed product variation
 */
ProductDetail.productVariationSwatchHandler = function () {
    $(document).on("click", ".imageVariationListItem a", function () {
        var $container = $(this).closest('[data-dynamic-block]');
        var currentClicked = $(this);
        var currentAttributeType = $(this).attr('data-variation-attribute');
        var allAttributes = ProductDetail.extractAttributes($(this).closest('form'), currentClicked);
        var currentProductSKU = $container.attr('data-dynamic-block-product-sku');
        //Search priority

        var productSearchAttributes = {};
        productSearchAttributes['prioritySearchAttribute'] = currentAttributeType;
        productSearchAttributes['searchAttributes'] = allAttributes;

        ProductDetail.searchVariation(currentProductSKU, productSearchAttributes, $container);
    });
};

ProductDetail.productVariationHandler = function () {
    $(document).on("change", '.variation-attribute', function () {
        var $container = $(this).closest('[data-dynamic-block]');
        var currentChanged = $(this);
        var currentAttributeType = $(this).attr('data-variation-attribute');
        var allAttributes = ProductDetail.extractAttributes($(this).closest('form'), currentChanged);
        var currentProductSKU = $container.attr('data-dynamic-block-product-sku');
        //Search priority
        var productSearchAttributes = {};
        productSearchAttributes['prioritySearchAttribute'] = currentAttributeType;
        productSearchAttributes['searchAttributes'] = allAttributes;

        ProductDetail.searchVariation(currentProductSKU, productSearchAttributes, $container);
    });
};

//input com.intershop.sellside.rest.common.capi.resourceobject.ProductSearchAttributesRO as json and master product sku as string
ProductDetail.searchVariation = function(masterProductSKU, productSearchAttributes, container) {
    productSearchAttributes['masterProductSKU'] = masterProductSKU;
    $.ajax({
        dataType: 'json',
        type: 'post',
        contentType: 'application/json',
        data: JSON.stringify(productSearchAttributes),
        processData: false,
        url: REST_WEB_ADAPTER +'/' + SERVER_GROUP + '/'+ CURRENT_DOMAIN +'/-;loc=' + LOCALE + '/products/'+masterProductSKU+'/variations/search',
        success: function (productVariation) {
             ProductDetail.getProductVariationContent(productVariation['sku'], container.attr("data-dynamic-block-call-parameters"), container);
        },
        error: function (jqXHR, exception) {
            var msg = '';
            if (jqXHR.status === 0) {
                msg = 'Not connect.\n Verify Network.';
            } else if (jqXHR.status == 404) {
                msg = 'Requested page not found. [404]';
            } else if (jqXHR.status == 500) {
                msg = 'Internal Server Error [500].';
            } else if (exception === 'parsererror') {
                msg = 'Requested JSON parse failed.';
            } else if (exception === 'timeout') {
                msg = 'Time out error.';
            } else if (exception === 'abort') {
                msg = 'Ajax request aborted.';
            } else {
                msg = 'Uncaught Error.\n' + jqXHR.responseText;
            }
            console.error(msg);
        },
    });
};

ProductDetail.extractAttributes = function(variationAttributesForm, current) {
    var variationAttributes = $(variationAttributesForm).find('.variation-attribute');
    var variationAttributeValues = {};

    $.each(variationAttributes, function (i, variationAttribute) 
    {
        if ($(variationAttribute).is('ul')) 
        {
            if(($(variationAttribute).attr('data-attribute-uuid') && current.closest('.variation-attribute').attr('data-attribute-uuid')) && 
                    $(variationAttribute).attr('data-attribute-uuid') == current.closest('.variation-attribute').attr('data-attribute-uuid'))
            {
                if(!variationAttributeValues[current.attr('data-variation-attribute')] || current.closest('.variation-attribute').attr('data-attribute-uuid'))
                {
                variationAttributeValues[current.attr('data-variation-attribute')] = current.attr('data-variation-product-attribute');
                }                
            }
            else
            {
                if(!variationAttributeValues[$($(this).find('li > a[class="selected"]')).attr('data-variation-attribute')] || current.closest('.variation-attribute').attr('data-attribute-uuid'))
                {
                 variationAttributeValues[$($(this).find('li > a[class="selected"]')).attr('data-variation-attribute')] = $($(this).find('li > a[class="selected"]')).attr('data-variation-product-attribute');
            }
        }
        }
        if ($(variationAttribute).is('select'))
        {
            if(!variationAttributeValues[$(variationAttribute).attr('data-variation-attribute')] || current.closest('.variation-attribute').attr('data-attribute-uuid'))
            {
            variationAttributeValues[$(variationAttribute).attr('data-variation-attribute')] = $(variationAttribute).val();
        }
            
        }
    });
    return variationAttributeValues;
};

ProductDetail.getProductVariationContent = function (sku, callParameters, container) {
    // send an Ajax request for a given sku to get the different product content and the new content in the given container
    if (sku !== undefined) {
        $.ajax({
            type: 'get',
            datatype: 'text/html',
            url: RetailShop.URLs.getProductComponents,
            data: '&SKU=' + encodeURIComponent(sku) + '&' + callParameters,
            success: function (data) {
                // replace the containers HTML with the content of the selected product variation
                container.replaceWith(data);
                // initialize the product image carousel if available within the container
                if (container.find('#prodimgcarousel').length > 0) {
                    ProductDetail.productImageCarousel();
                }
                // rebind bootstrapValidator at the relevant forms within the container
                Validation.bind($('form.bv-form'));
                // initialize all not initialized slick carousels - assumably the ones in the replaced product variation content
                $('.slick-carousel').not('.slick-initialized').slick();
            },
            error: function (jqXHR, exception) {
                var msg = '';
                if (jqXHR.status === 0) {
                    msg = 'Not connect.\n Verify Network.';
                } else if (jqXHR.status == 404) {
                    msg = 'Requested page not found. [404]';
                } else if (jqXHR.status == 500) {
                    msg = 'Internal Server Error [500].';
                } else if (exception === 'parsererror') {
                    msg = 'Requested JSON parse failed.';
                } else if (exception === 'timeout') {
                    msg = 'Time out error.';
                } else if (exception === 'abort') {
                    msg = 'Ajax request aborted.';
                } else {
                    msg = 'Uncaught Error.\n' + jqXHR.responseText;
                }
                console.error(msg);
            }
        });
    }
};

//initialize all product detail functions
$(document).ready(function () {
    ProductDetail.init();
});