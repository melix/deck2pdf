setup = {
    js 'var api = impress();'
    js '''var $$ = function ( selector, context ) {
        context = context || document;
        return context.querySelectorAll(selector);
    };'''
    js '''var byId = function ( id ) {
        return document.getElementById(id);
    };'''
}

nextSlide = {
    js('api.next()')
}

totalSlides = {
    js (/$$(".step", byId('impress')).length/)
}

// longer pause because of transitions
pause = 2000