// Initialize with:
// ReadiumSDK.HighlightManager.loadHighlights(savedHighlights);

ReadiumSDK.HighlightManager = function () {
    var loadedHighlights = [];
    var loadedHighlightsByIdref = {};
    var searchResultHighlightToShow;
    var currentPageIdref = '';
    var currentPageLowBoundaryCfi = '';
    var currentPageHighBoundaryCfi = '';
    var percentageThroughSpineItem;
    var nextScreenPercentageThroughSpineItem;
    var highlightTimeout = -1;
    var highlightsManager; // the ReadiumSDK highlights plugin highlightsManager singleton
    var initialHighlightsLoaded = false;

    function onSettingsApplied() {
        redrawHighlights();
    }
    
    function onPaginationChanged(paginationInfo) {
        currentPageLowBoundaryCfi = paginationInfo.firstVisibleCfi;
        currentPageHighBoundaryCfi = paginationInfo.lastVisibleCfi;
        currentPageIdref = paginationInfo.openPages[0].idref;
        percentageThroughSpineItem = paginationInfo.percentageThroughSpineItem;
        nextScreenPercentageThroughSpineItem = paginationInfo.nextScreenPercentageThroughSpineItem;
        redrawHighlights();
    }

    // Called by Android app on reader initialization with previously-saved highlights for a given book.
    function loadHighlights(savedHighlights) {
        console.log('loadHighlights() called with %O', savedHighlights);
        if (!ReadiumSDK.reader.plugins.highlights) {
          console.error('Highlights plugin not initialized.');
          return;
        }
        if (!highlightsManager) { // First time loading highlights
            highlightsManager = ReadiumSDK.reader.plugins.highlights.getHighlightsManager();
        }
        loadedHighlights = savedHighlights; // Overwriten currently loaded highlights with savedHighlights
        loadedHighlightsByIdref = {}; // Clear the highlightsByIdref hashmap
        loadedHighlights.forEach(function(highlight) {
            if (!loadedHighlightsByIdref[highlight.idref]) {
                loadedHighlightsByIdref[highlight.idref] = [];
            }
            loadedHighlightsByIdref[highlight.idref].push(highlight);
        });
        redrawHighlights();
    }

    function redrawHighlights() {
        if (!highlightsManager) { // First time calling highlight method
            highlightsManager = ReadiumSDK.reader.plugins.highlights.getHighlightsManager();
        }
        var displayingSearchResultHighlight = highlightsManager.getHighlight('searchResultHighlight');
        if (displayingSearchResultHighlight) {
            highlightsManager.removeHighlight('searchResultHighlight');
        }

        if (loadedHighlightsByIdref[currentPageIdref]) {
            loadedHighlightsByIdref[currentPageIdref].forEach(function(highlight) {
//                var isHighlightWithinRange = highlightsManager.cfiIsBetweenTwoCfis(highlight.CFI, currentPageLowBoundaryCfi, currentPageHighBoundaryCfi);

                var isHighlightWithinRange = false;

                if (highlight.percentageThroughSpineItem == undefined ||
                    highlight.percentageThroughSpineItem >= percentageThroughSpineItem && highlight.percentageThroughSpineItem < nextScreenPercentageThroughSpineItem) {
                    isHighlightWithinRange = true;
                }

                if (isHighlightWithinRange) {
                    console.log('%cHighlight found on current page. Drawing...', 'font-weight: bold; color: green')
                                                                                   
                    // If highlight already drawn, remove it
                    var alreadyExistingHighlight = highlightsManager.getHighlight(highlight.id);
                    if (alreadyExistingHighlight) {
                        highlightsManager.removeHighlight(alreadyExistingHighlight.id);
                        $('iframe').each(function (i, iframe) {
                            $(iframe).contents().find('html > div.highlight[data-id="' + highlight.id + '"]').remove();
                        });
                    }

                    // Add the highlight using the ReadiumSDK highlights plugin highlightsManager singleton
                    highlightsManager.addHighlight(highlight.idref, highlight.CFI, highlight.id, 'highlight');

                    return; // Finished drawing this highlight
                } // End check if highlight is within range
            });
            console.log('Highlights shown: ' + loadedHighlightsByIdref[currentPageIdref].length);
        }
        
        // If search result highlight queued up to be shown, show it
        if (searchResultHighlightToShow && searchResultHighlightToShow.idref === currentPageIdref) {
            ReadiumSDK.reader.plugins.highlights.addHighlight(searchResultHighlightToShow.idref, searchResultHighlightToShow.CFI, 'searchResultHighlight', 'highlight');
            searchResultHighlightToShow = null;
        }
    }

    // Called by Android app when user selects 'Highlight' option in context menu.
    function addSelectionHighlight(id) {
        if (!highlightsManager) { // First time calling highlight method
            highlightsManager = ReadiumSDK.reader.plugins.highlights.getHighlightsManager();
        }
        var currentViewSelection = ReadiumSDK.ReaderController.getCurrentViewSelection();
        if (currentViewSelection.toString().length == 0) {
            console.error('Selection not highlighted. Unable to detect selected text.');
            return;
        }
        
        var highlight = highlightsManager.addSelectionHighlight(id, 'highlight');
        if (highlight !== undefined) {
            highlight.id = id;
            highlight.text = currentViewSelection.toString();
            highlight.CFI = highlight.contentCFI;
            delete highlight.contentCFI;
            loadedHighlights.push(highlight);
            if (!loadedHighlightsByIdref[highlight.idref]) {
                loadedHighlightsByIdref[highlight.idref] = [];
            }
            loadedHighlightsByIdref[highlight.idref].push(highlight);

            window.LauncherUI.saveHighlight(JSON.stringify(simpleKeys(highlight)));
        } else {
            console.error('Selection not highlighted. Highlight undefined.');
        }
    }
    
    // Called by Android app when user clicks 'Remove from the 'Highlights' section in the sidebar of the Android app.
    function removeHighlight(id) {
        highlightsManager.removeHighlight(id);
        loadedHighlights = loadedHighlights.filter(function (highlight) {
            return highlight.id !== id;
        });
        for (var idref in loadedHighlightsByIdref) {
            loadedHighlightsByIdref[idref] = loadedHighlightsByIdref[idref].filter(function(filteredHighlight) {
                return filteredHighlight.id !== id;
            });
        }
    }
    
    function setSearchResultHighlight(highlight) {
        if (!highlight) {
            ReadiumSDK.reader.plugins.highlights.removeHighlight('searchResultHighlight');
        }
        searchResultHighlightToShow = highlight;
    }
    
    function bookmarkCurrentPage() {
        var firstVisibleCfi = ReadiumSDK.reader.getFirstVisibleCfi(); // e.g. '/4/2/2[chapter_4152]/4/8,/1:453,/1:454'
        var cfiSplitByComma = firstVisibleCfi.contentCFI.split(',');
        firstVisibleCfi.contentCFI = cfiSplitByComma[0] + ',' + cfiSplitByComma[1] + ',' + cfiSplitByComma[1]; // e.g. '/4/2/2[chapter_4152]/4/8,/1:453,/1:453'
        return JSON.stringify(firstVisibleCfi);
    }

    // Helper function to prevent cyclic references when stringifying objects
    function simpleKeys(original) {
        return Object.keys(original).reduce(function (obj, key) {
            obj[key] = typeof original[key] === 'object' ? '{ ... }' : original[key];
            return obj;
        }, {});
    }

    // Public methods and properties
    return {
        // Called by Android app
        loadHighlights: loadHighlights,
        addSelectionHighlight: addSelectionHighlight,
        removeHighlight: removeHighlight,
        bookmarkCurrentPage: bookmarkCurrentPage,

        // Called by ReaderController to specify search result to highlight
        setSearchResultHighlight: setSearchResultHighlight,
        
        // Trigger these methods from HostAppFeedback
        onSettingsApplied: onSettingsApplied,
        onPaginationChanged: onPaginationChanged
    }
                                                              
}();