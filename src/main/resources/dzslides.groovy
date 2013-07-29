isLastSlide = {
    js '''
        var totalSlides = Dz.slides.length;
        var cSlide = Dz.idx;
        cSlide==totalSlides && Dz.step==Dz.slides[cSlide - 1].$$('.incremental > *').length
    '''
}

nextSlide = {
    js 'Dz.forward();'
}

