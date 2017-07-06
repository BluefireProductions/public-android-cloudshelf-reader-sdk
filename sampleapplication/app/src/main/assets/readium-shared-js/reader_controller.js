var navTime = 0;
var initialOrientation = (window.orientation == 0 || window.orientation == 180) ? 'portrait' : 'landscape';
var initialOrientationBookmark;
if (!isUsingCFILocations) {
    var isUsingCFILocations = false;
}

ReadiumSDK.ReaderController = (function () {                               
    var spineItemsByHref = {};
    var isFixedLayout = false;
    var spineItems = [];
                               
    function onContentDocumentLoaded($iframe) {
        console.log('onContentDocumentLoaded in ReaderController.');
    }
                               
    function openBook(openBookData) {
        navTime = new Date().getTime();
        console.log('navTime: openBook started.');
        isFixedLayout = (openBookData.package.rendition_layout == 'pre-paginated');
        if (isFixedLayout) {
            $('body').addClass('fixed-layout');
        }
        ReadiumSDK.reader.openBook(openBookData);
                               
        spineItems = ReadiumSDK.reader.spine().items;
        spineItems.forEach(function(item) {
            spineItemsByHref[item.href] = item;
        });
    }

    var navigationTargetLocation = undefined;
    
    function getNavigationTargetLocation() {
        return navigationTargetLocation || '';
    }


    function openPageRight() {
            console.time('pageRequest')

        ReadiumSDK.HighlightManager.setSearchResultHighlight(false);
        initialOrientation = (window.orientation == 0 || window.orientation == 180) ? 'portrait' : 'landscape';
        initialOrientationBookmark = null;
        ReadiumSDK.reader.openPageRight()
    }
    function openPageLeft() {
        console.time('pageRequest')

        ReadiumSDK.HighlightManager.setSearchResultHighlight(false);
        initialOrientation = (window.orientation == 0 || window.orientation == 180) ? 'portrait' : 'landscape';
        initialOrientationBookmark = null;
        ReadiumSDK.reader.openPageLeft()
    }
                               
    function goTo(idref, cfi, isSearchResult) {
        console.time('pageRequest')
        if (isSearchResult) {
            ReadiumSDK.HighlightManager.setSearchResultHighlight({idref: idref, CFI: cfi});
        } else {
            ReadiumSDK.HighlightManager.setSearchResultHighlight(false);                               
        }
        initialOrientation = (window.orientation == 0 || window.orientation == 180) ? 'portrait' : 'landscape';
        initialOrientationBookmark = null;
        console.log('ReaderController.goTo() called with idref: ' + idref + ' and cfi: ' + cfi);
        navTime = new Date().getTime();
        navigationTargetLocation = {
                cfi: cfi,
                idref: idref
        };
                               
        ReadiumSDK.reader.openSpineItemElementCfi(idref, cfi);
    }

    function goToFile(filename) {
        initialOrientation = (window.orientation == 0 || window.orientation == 180) ? 'portrait' : 'landscape';
        initialOrientationBookmark = null;
        ReadiumSDK.reader.openContentUrl(filename);
    }

    function goToHighlight(idref, cfi) {
       console.time('pageRequest')
       goTo(idref, cfi);
    }

    function goToSearchResult(idref, cfi) {
        console.time('pageRequest')
        goTo(idref, cfi, true);
    }
                               
    function goToPage(idref, pageIndex) {
        ReadiumSDK.reader.openSpineItemPage(idref, pageIndex, ReadiumSDK.reader);
    }

    function updateSettings(settings) {
        ReadiumSDK.reader.updateSettings(settings);
    }
                               
    function getSpineItem(href) {
        return spineItemsByHref[href];
    }
                               
    function getCurrentIframe() {
        return $('iframe').first();
    }

    function getIframeContentDocument() {
        return $('iframe').get(0).contentDocument;
    }
                    
    function getCurrentViewSelection() {
        return ReadiumSDK.ReaderController.getIframeContentDocument().getSelection();
    }

   function reportSelectedText(action) {
       var selectedText = getCurrentViewSelection().toString();
       if (window.LauncherUI) {
          window.LauncherUI.reportSelectedText(selectedText, action);
       }
   }
                               
                               
    // Public methods
    return {
        openBook: openBook,
        isFixedLayout: isFixedLayout,
        goTo: goTo,
        goToHighlight: goToHighlight,
        goToPage: goToPage,
        goToFile: goToFile,
        goToSearchResult: goToSearchResult,
        getSpineItem: getSpineItem,
        getCurrentIframe: getCurrentIframe,
        getIframeContentDocument: getIframeContentDocument,
        getCurrentViewSelection: getCurrentViewSelection,
        onContentDocumentLoaded: onContentDocumentLoaded,
        getNavigationTargetLocation: getNavigationTargetLocation,
        openPageRight: openPageRight,
        openPageLeft: openPageLeft,
        updateSettings: updateSettings,
        reportSelectedText: reportSelectedText
    }
                               
    // Private methods
    function cfiRangeSpansMultipleElements(cfi) {
        return cfi.indexOf(',') > -1 && cfi.split(',')[2].split('/').length > 2;
    }
                               
})(); // End ReaderController



