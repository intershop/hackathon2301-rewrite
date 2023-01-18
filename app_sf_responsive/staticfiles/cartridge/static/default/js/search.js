/**
 * Collapsing of product search results or content search results
 **/
$(document).on( 'click', '.search-switch-toggle input[type="radio"]', function(e){
    var theRadio = $(e.target);
    var switchTargetID = theRadio.data("switch-target-id");
    $("#" + switchTargetID).show();
    $("[data-switch-target-id='" + switchTargetID + "']").prop("checked",true);
    $("input[name='" + theRadio.attr('name') + "']").not(theRadio).each(function(){
        $("#" + $(this).data("switch-target-id")).hide();
    });
});

$(document).ready(function(){
    
    /**
     * Event Listener for links within containers with search parameter data 
     * appends the search parameter to the url when such a link is clicked.
     **/
    $(document).on('click', '[data-search] a, a[data-search]', function(ev) {
        var $target = $(ev.currentTarget),
            url = $target.attr('href'),
            searchParameter = "";
        // get search parameter of the parent container
        $target.parents('[data-search]').each(function(index, element) {
            searchParameter = $(element).attr('data-search');
        });
        
        // add the search parameter to the URL before the request
        if(searchParameter !== "")
        if (/\?/.test(url)) {
            url = url + '&SearchParameter=' + searchParameter;
        } else {
            url = url + '?SearchParameter=' + searchParameter;
        }
        $target.attr('href', url);
        return true;
    });
});


