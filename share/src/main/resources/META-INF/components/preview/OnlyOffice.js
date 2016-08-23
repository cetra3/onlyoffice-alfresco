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
            console.log(this.wp);
            console.log(this.attributes);

            var docEditor = new DocsAPI.DocEditor(this.wp.id + "-body", this.getAttributes());

            this.updateHeight();

            // handle event
            window.addEventListener("optimizedResize", this.updateHeight.bind(this));

        },

        updateHeight: function() {
            console.log("Resize hit!");
            var iFrames = document.getElementById(this.wp.id).getElementsByTagName("iframe");

            if(iFrames[0]) {
                iFrames[0].style.height = (window.innerHeight - 200) + "px";
            }

        },

        getDocumentType: function(ext) {
            if (".docx.doc.odt.rtf.txt.html.htm.mht.pdf.djvu.fb2.epub.xps".indexOf(ext) != -1) return "text";
            if (".xls.xlsx.ods.csv".indexOf(ext) != -1) return "spreadsheet";
            if (".pps.ppsx.ppt.pptx.odp".indexOf(ext) != -1) return "presentation";
            return null;
        },

        getAttributes: function() {

            var docName = this.attributes.docTitle;
            var docType = docName.substring(docName.lastIndexOf(".") + 1).trim().toLowerCase();

            var config = {
                type: "desktop",
                width: "100%",
                height: "100%",
                documentType: getDocumentType(docType),
                document: {
                    title: docName,
                    url: this.attributes.docUrl,
                    fileType: docType,
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
