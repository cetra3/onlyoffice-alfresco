<!--
    Copyright (c) Ascensio System SIA 2019. All rights reserved.
    http://www.onlyoffice.com
-->
<html>
<head>
    <meta http-equiv='Content-Type' content='text/html; charset=utf-8'>

    <title>ONLYOFFICE™</title>

    <link href="${url.context}/res/components/onlyoffice/onlyoffice.css" type="text/css" rel="stylesheet">

    <!--Change the address on installed ONLYOFFICE™ Online Editors-->
    <script id="scriptApi" type="text/javascript" src="${onlyofficeUrl}OfficeWeb/apps/api/documents/api.js"></script>

    <script type="text/javascript" src="${url.context}/res/components/onlyoffice/onlyoffice.js"></script>
</head>

<body>
    <div>
        <div id="placeholder"></div>
    </div>
    <script>
    var config = JSON.parse('${config}');
    new DocsAPI.DocEditor("placeholder", config);
    </script>
</body>
</html>

