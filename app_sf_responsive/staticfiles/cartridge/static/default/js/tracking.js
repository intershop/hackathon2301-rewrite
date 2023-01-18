$(document).ready(function(){
    
    /**
     * Event Listener for links within containers with tracking information or with own tracking information.
     * Builds a tracking parameter and appends it to the url when such a link is clicked.
     **/
    $(document).on('click', '[data-tracking] a, a[data-tracking]', function(ev) {
        var $target = $(ev.currentTarget),
            url = $target.attr('href'),
            trackingData = [];
        // collect tracking data of the clicked link
        if ($target.attr('data-tracking')) {
            trackingData.push($target.attr('data-tracking'));
        }
        // collect tracking data of the parent containers
        $target.parents('[data-tracking]').each(function(index, element) {
            trackingData.push($(element).attr('data-tracking'));
        });
        // join the collected tracking data by "|" and replace empty space with "+"
        trackingData = trackingData.join('|').replace(/ /g, '+');
        
        var trackingDataEncoded = encodeURI(trackingData);
        // add the tracking data to the URL before the request
        if (/\?/.test(url)) {
            url = url + '&tracking=' + trackingDataEncoded;
        } else {
            url = url + '?tracking=' + trackingDataEncoded;
        }
        $target.attr('href', url);
        return true;
    });
});
