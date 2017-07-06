// Search with:
// ReadiumSDK.SearchController.findIn(query);

ReadiumSDK.SearchController = function () {

    this.searchResults = [];

    function findIn(query, href, idref) {
        var queryRegExp = new RegExp(query, 'i');
        var searchResults = [];
        $.ajax({
            type: 'GET',
            async: false,
            url: href,
            dataType: 'xml',
            success: function (xml) {
                var body = $(xml).find('body').get(0);
                var textNodes = getTextNodesIn(body);
                textNodes.forEach(function (textNode, i) {
                    var searchIndex = textNode.nodeValue.search(queryRegExp);
                    if (searchIndex > -1) {
                        var prefixIndex = Math.max(searchIndex - 140, 0);
                        var suffixIndex = searchIndex + query.length;

                        // Reformat CFIs returned from the readium-cfi-js library to contain a range, e.g.
                        // "/4/6[mo-nav-020]/4[mo-3-27]/1:34"
                        //                becomes
                        // "/4/6[mo-nav-020]/4[mo-3-27],/1:34,/1:39"


                        var cfi = EPUBcfi.Generator.generateCharacterOffsetCFIComponent(textNode, searchIndex);
                        var cfiFragment = cfi.split('/').pop();              // e.g. "1:34"
                        cfi = cfi.substr(0, cfi.lastIndexOf('/'));           // e.g. "/4/6[mo-nav-020]/4[mo-3-27]"
                        var cfiFragmentEnd = cfiFragment.split(':')[0] + ':' + (parseInt(cfiFragment.split(':')[1]) + query.length);
                        cfi += ',/' + cfiFragment + ',/' + cfiFragmentEnd;    // e.g. "/4/6[mo-nav-020]/4[mo-3-27],/1:34,/1:34"


                        searchResults.push({
                            text: textNode.nodeValue.substr(searchIndex, query.length),
                            prefix: textNode.nodeValue.substr(prefixIndex, searchIndex - prefixIndex),
                            suffix: textNode.nodeValue.substr(suffixIndex, 140),
                            cfi: cfi,
                            idref: idref
                        });
                    }
                });
                } // end success()
        }); // End $.ajax request
        this.searchResults = $.merge(this.searchResults, searchResults);
        window.LauncherUI.reportSearchResults(JSON.stringify(searchResults));
        
    } // End function findIn()


    function getTextNodesIn(node, includeWhitespaceNodes) {
        var textNodes = [], nonWhitespaceMatcher = /\S/;

        function getTextNodes(node) {
            if (node.nodeType !== undefined && node.nodeType == Node.TEXT_NODE) {
                if (node.parentNode.nodeName !== 'script' && (includeWhitespaceNodes || nonWhitespaceMatcher.test(node.nodeValue))) {
                    textNodes.push(node);
                }
            } else {
                for (var i = 0, len = node.childNodes.length; i < len; ++i) {
                    getTextNodes(node.childNodes[i]);
                }
            }
        }

        getTextNodes(node);
        return textNodes;
    }

    // Expose the following functions and properties
    return {
        findIn: findIn,
        searchResults: this.searchResults
    }
}();