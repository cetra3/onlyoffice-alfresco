/*
    Copyright (c) Ascensio System SIA 2019. All rights reserved.
    http://www.onlyoffice.com
*/

pObj = eval('(' + remote.call("/parashift/onlyoffice/prepare?nodeRef=" + url.args.nodeRef) + ')');
model.onlyofficeUrl = pObj.onlyofficeUrl;
model.docTitle = pObj.document.title;
delete (pObj.onlyofficeUrl);
model.config = JSON.stringify(pObj);
