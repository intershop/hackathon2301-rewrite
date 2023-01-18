$(document).ready(function(){
    
    $(document).on('click', '.video.autoplay', function(event){
        this.paused ? this.play() : this.pause();
    });
    
    $(document).on('click', '.video-link', function(event){
        var videoContainer = $(this).closest('.video-container');
        
        var video= videoContainer.children('video').get(0);
        video.paused ? video.play() : video.pause();
        
        videoContainer.children('.video').attr("controls","controls");
        videoContainer.children('.video-text').hide();
        
        $(video).on('click', function(event){
            this.paused ? this.play() : this.pause();
        });
    });

    // Retrieve proper embed URL through Vimeo OEmbed API
    function getVimeoEmbedUrl(url) {
        $.ajax({
          url: 'https://vimeo.com/api/oembed.json?url='+url,
          type: 'GET',
          async: false,
          success: function(response) {
            if(response.html) {
              return response.html;
            }
          }
        });
    }
    
    // Get video hoster specific embed URL
    function getVideoEmbedURL(url) {
        var ytRegExp = /^.*(youtu.be\/|v\/|u\/\w\/|embed\/|watch\?v=|\&v=)([^#\&\?]*).*/;
        
        if (/youtube/i.test(url)) {
            var match = url.match(ytRegExp);
            // if URL is embed URL, leave as is
            if (/embed/i.test(url)) {
                return url;
            // else retrieve video ID and build URL
            } else if (match && match[2].length == 11) {
                return "//www.youtube.com/embed/" + match[2];
            }
        } else if (/vimeo/i.test(url)) {
            var vimeoHTML = getVimeoEmbedUrl(url);
            if ($(vimeoHTML).attr("src")) {
                return $(vimeoHTML).attr("src");
            } else {
                return url;
            }
        }
    }

    // retrieve configured video URL for each video component, adjust URL and reload iframe with proper URL
    $(".video-container iframe").each(function() {
        var videoURL = $(this).attr("data-url");
        var autoPlay = $(this).attr("data-autoplay");
        if (videoURL) {
            if (autoPlay == "true")
                $(this).attr("src", getVideoEmbedURL(videoURL) + "?autoplay=1");
            else {
                $(this).attr("src", getVideoEmbedURL(videoURL));
            }
        }
    });

});
