<#if onlyofficeUrl??>

<@markup id="onlyoffice-dependencies" target="js" action="after" scope="global">

    <@script src="${url.context}/res/components/preview/OnlyOffice.js" group="${dependencyGroup}"/>

    <script type="text/javascript" src="${url.context}/res/components/onlyoffice/onlyoffice.js"></script>
    <script id="scriptApi" type="text/javascript" src="${onlyofficeUrl}OfficeWeb/apps/api/documents/api.js"></script>
</@markup>

</#if>
