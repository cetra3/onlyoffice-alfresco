/*
    Copyright (c) Ascensio System SIA 2019. All rights reserved.
    http://www.onlyoffice.com
*/

pObj = eval('(' + remote.call("/parashift/onlyoffice/prepare?nodeRef=" + url.args.nodeRef) + ')'); 
model.callbackUrl = pObj.callbackUrl;
model.docTitle = pObj.docTitle;
model.docUrl = pObj.docUrl;
model.key = pObj.key;
model.onlyofficeUrl = pObj.onlyofficeUrl;
model.token = pObj.token || "";

model.userId = user.id;
model.firstName = user.firstName;
model.lastName = user.lastName;
model.lang = pObj.lang;
