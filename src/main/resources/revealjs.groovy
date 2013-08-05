isLastSlide = {
    js 'Reveal.isLastSlide();'
}

nextSlide = {
    js 'Reveal.next();'
}

setup = {
    // disable controls for better rendering
    js 'Reveal.configure({controls: false, progress: false});'

    if (Boolean.valueOf(options.skipFragments)) {
        js 'Reveal.configure({fragments: false});'
    }
}