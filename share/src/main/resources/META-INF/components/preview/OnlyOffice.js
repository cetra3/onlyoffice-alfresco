(function() {

    var throttle = function(type, name, obj) {
        obj = obj || window;
        var running = false;
        var func = function() {
            if (running) { return; }
            running = true;
            requestAnimationFrame(function() {
                obj.dispatchEvent(new CustomEvent(name));
                running = false;
            });
        };
        obj.addEventListener(type, func);
    };

    /* init - you can init any event */
    throttle("resize", "optimizedResize");

    Alfresco.WebPreview.prototype.Plugins.OnlyOffice = function(wp, attributes)
    {
        this.wp = wp;
        this.attributes = YAHOO.lang.merge(Alfresco.util.deepCopy(this.attributes), attributes);
        return this;
    };

    Alfresco.WebPreview.prototype.Plugins.OnlyOffice.prototype =
    {
        attributes: {},

        report: function() {
            return null;
        },

        display: function() {

            var aboveThreshold = this.attributes.abovePreviewThreshold;
            if (aboveThreshold) {
                var messageAbovePreviewThreshold = Alfresco.messages.global["label.onlyoffice.abovePreviewThreshold"];
                if (!messageAbovePreviewThreshold) {
                    messageAbovePreviewThreshold = "This document cannot be viewed or edited in OnlyOffice. Please edit using Microsoft Office.";
                }
                return '<div class="message">' + messageAbovePreviewThreshold + '</div>';
            } else {
                var docEditor = new DocsAPI.DocEditor(this.wp.id + "-body", this.getAttributes());
                this.updateHeight();

                //Update the height of the document preview
                window.addEventListener("optimizedResize", this.updateHeight.bind(this));
            }

        },

        updateHeight: function() {
            var iFrames = document.getElementById(this.wp.id).getElementsByTagName("iframe");

            var height = this.wp.options.proxy == "alfresco-noauth" ? 260 : 200;

            if(iFrames[0]) {
                iFrames[0].style.height = (window.innerHeight - height) + "px";
            }

        },

        /*getDocumentType: function(ext) {
            if (".docm.dotm.docx.doc.odt.rtf.txt.html.htm.mht.pdf.djvu.fb2.epub.xps".indexOf(ext) != -1) return "text";
            if (".xlsm.xltm.xlam.xlsb.xls.xlsx.ods.csv".indexOf(ext) != -1) return "spreadsheet";
            if (".pptm.potm.ppsm.ppam.sldm.pps.ppsx.ppt.pptx.odp".indexOf(ext) != -1) return "presentation";
            return null;
        },*/

        getAttributes: function() {

            var docName = this.attributes.docTitle;
            var docType = docName.substring(docName.lastIndexOf(".") + 1).trim().toLowerCase();

            var config = {
                type: "embedded",
                width: "100%",
                height: "100%",
                documentType: getDocumentType(docType),
                document: {
                    title: docName,
                    url: this.attributes.docUrl,
                    fileType: translateDocumentType(docType),
                    key: this.attributes.key,
                    permissions: {
                        edit: true
                    }
                },
                editorConfig: {
                    mode: "view",
                    callbackUrl: this.attributes.callbackUrl,
                    user: this.attributes.user
                }
            };

            return config;

        }
    };

})();
