setup = {
    js '''Flowtime.showProgress(false)
    Flowtime.gotoEnd();
    var endPage=Flowtime.getPageIndex();
    var endSection=Flowtime.getSectionIndex();
    Flowtime.gotoHome();
    var complete = false
    '''
}

nextSlide = {
    js 'Flowtime.next();'
}

isLastSlide = {
    js 'endPage == Flowtime.getPageIndex() && endSection==Flowtime.getSectionIndex()'
}
