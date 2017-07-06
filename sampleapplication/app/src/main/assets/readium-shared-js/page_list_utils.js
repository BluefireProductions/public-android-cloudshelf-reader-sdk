ReadiumSDK.PageListUtils = (function () {

	var pageList = {};

   function getCFIs(spineHref, pageIds) {
        var cfis = [];
        console.log('spineHref: %O', spineHref)
        console.log('pageIds: %O', pageIds)

        var isFirstGeneratedCfi = true;
        $.ajax({
            type: 'GET',
            async: false,
            url: spineHref,
            dataType: 'xml',
            success: function (xml) {
                $(pageIds).each(function (i, pageId) {
                	if (pageId == '/4/2') {
                		cfis.push('/4/2');
                	} else {
	                    var elements = $(xml).find('#' + pageId);
	                    if (elements.length > 0) {
                            // If this is the first generated CFI, always return /4/2
                            if (isFirstGeneratedCfi) {
                                cfis.push('/4/2');
                                isFirstGeneratedCfi = false;
                            } else {
    	                        cfis.push(EPUBcfi.Generator.generateElementCFIComponent(elements[0]));
                            }
	                    }
	                    else {
	                        cfis.push('');
	                	}
                    }
                });
            }
        });

        console.log('generated CFIs: %O', cfis)
        window.LauncherUI.reportPageListCFIs(JSON.stringify(cfis));
    }

    function getSyntheticCFIs(spineHref) {
        var cfis = [];
        console.log('spineHref: %O', spineHref)


        $.ajax({
            type: 'GET',
            async: false,
            url: spineHref,
            dataType: 'xml',
            success: function (xml) {
                var body = xml.getElementsByTagName('body')[0];
                var textNodes = getTextNodesIn(body);
                cfis.push('/4/2');
                var characterCount = 0;
                $.each(textNodes, function (i, textNode) {
                    characterCount += textNode.length;
                    if (characterCount >= 3500) {
                        cfis.push(EPUBcfi.Generator.generateElementCFIComponent(textNode.parentElement));
                        characterCount = 0;
                    }
                });
            }
        });

        if (cfis.length > 1) {
          cfis.pop(); // Remove last CFI because it may be too close to the end of the spine item
	      if (cfis.length > 1) {
	        cfis.pop(); // If still over 1 remaining, remove another one
	      }

        }

        window.LauncherUI.reportPageListCFIs(JSON.stringify(cfis));
    }


    return {
        getCFIs: getCFIs,
        getSyntheticCFIs: getSyntheticCFIs
    }

    // Helper function to prevent cyclic references when stringifying objects
    function simpleKeys(original) {
        return Object.keys(original).reduce(function (obj, key) {
            obj[key] = typeof original[key] === 'object' ? '{ ... }' : original[key];
            return obj;
        }, {});
    }

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

    function isElementInViewport (el) {
        var rect = el.getBoundingClientRect();
        return (
                rect.top >= 0 &&
                rect.left >= 0 &&
                rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
                rect.right <= (window.innerWidth || document.documentElement.clientWidth)
                );
    }

    function isUrlExistent(url, callback) {
        var http = new XMLHttpRequest();
        http.open('HEAD', url, false);
        http.onreadystatechange = function() {
            if (this.readyState == this.DONE) {
                callback(this.status != 404);
            }
        };
        http.send();
    }

    function sanitizeCFI(cfi) {
        if (cfi && cfi.contentCFI) {
            cfi = cfi.contentCFI;
        } else {
            cfi = '/4/2'; // Default if no CFI found
        }
        if (cfi.indexOf(',') > -1) { // If range CFI, remove range data
            cfi = cfi.split(',')[0];
        }
        return cfi;
    }

    function parseContentCfi(cont) {
        return cont.replace(/\[(.*?)\]/, "").split(/[\/,:]/).map(function(n) { return parseInt(n); }).filter(Boolean);
    };

    function contentCfiComparator(cont1, cont2) {
        cont1 = this.parseContentCfi(cont1);
        cont2 = this.parseContentCfi(cont2);

        //compare cont arrays looking for differences
        for (var i=0; i<cont1.length; i++) {
            if (cont1[i] > cont2[i]) {
                return 1;
            }
            else if (cont1[i] < cont2[i]) {
                return -1;
            }
        }

        //no differences found, so confirm that cont2 did not have values we didn't check
        if (cont1.length < cont2.length) {
            return -1;
        }

        //cont arrays are identical
        return 0;
    };


})();