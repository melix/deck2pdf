
ready = { start ->
    action = start // store start action into the binding for use from javascript
}

setup = {
    js '''
        var slideView;
        var slideCount = 1;
        curl(['wire!slides-spec']).then(function (wire) {
            slideView = wire.view;
            var start = function(error) {
                // fetch "action" from binding, then call it
                exportProfile.getVariable('action').run(); // start export
            };

            // count the number of slides (unfortunately, no API for this)
            var counter = function(slidedef) {
                if (slidedef) {
                    slideCount++;
                    wire.model.get(slideCount).then(counter, start);
                }
            }
            wire.model.get(slideCount).then(counter, start);
        });
    '''
}

totalSlides = {
    js 'slideCount'
}

nextSlide = {
    js 'slideView.next()'
}