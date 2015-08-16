<html>
<head>
    <meta http-equiv='Content-Type' content='text/html; charset=utf-8'>

    <title>ONLYOFFICE™</title>

    <link href="${url.context}/res/components/onlyoffice/onlyoffice.css" type="text/css" rel="stylesheet">

    <!--Change the address on installed ONLYOFFICE™ Online Editors-->
    <script id="scriptApi" type="text/javascript" src="http://192.168.0.102/OfficeWeb/apps/api/documents/api.js"></script>

    <script type="text/javascript" src="${url.context}/res/components/onlyoffice/sha256.js"></script>
    <script type="text/javascript" src="${url.context}/res/components/onlyoffice/onlyoffice.js"></script>
</head>

<body>
    <div>
        <div id="placeholder"></div>
    </div>
    <script>

    var docUrl = "http://onlyo.co/1x5REbq?demo.docx";
    var docType = docUrl.substring(docUrl.lastIndexOf(".") + 1).trim().toLowerCase();
    var documentType = getDocumentType(docType);

    new DocsAPI.DocEditor("placeholder",
        {
            type: "desktop",
            width: "100%",
            height: "100%",
            documentType: documentType,
            document: {
                title: docUrl,
                url: docUrl,
                fileType: docType,
                key: "${nodeRef}",
                permissions: {
                    edit: true
                }
            },
            editorConfig: {
                mode: "edit",
                canAutosave: false,
                callbackUrl: "http://192.168.1.12:8080/test.php"
            },
            events: {
                'onSave': function() {
                  console.log("Save button pressed!");
                },
                'onDocumentStateChange': function()
                {
                  console.log("State Changed");
                }
            }
        });
    </script>
</body>
</html>

