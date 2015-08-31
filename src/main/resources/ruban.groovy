setup = {
    js 'ruban.disableTransitions();'
}

isLastSlide = {
    if (Boolean.valueOf(options.skipSteps)) {
        js 'ruban.isLastSlide()'
    }
    else {
        js 'ruban.isLastSlide() && ruban.isLastStep()'
    }
}

nextSlide = {
    if (Boolean.valueOf(options.skipSteps)) {
        js '''
            ruban.nextSlide();
            if (ruban.hasSteps()) {
                while (!ruban.isLastStep()) {
                    ruban.next();
                }
            }
        '''
    }
    else {
        js 'ruban.next();'
    }
}