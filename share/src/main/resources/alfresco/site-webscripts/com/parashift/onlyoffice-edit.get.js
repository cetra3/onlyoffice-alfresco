
pObj = eval('(' + remote.call("/parashift/onlyoffice/prepare?nodeRef=" + url.args.nodeRef) + ')'); 
model.callbackUrl = pObj.callbackUrl;
model.docTitle = pObj.docTitle;
model.docUrl = pObj.docUrl;
model.key = pObj.key;
model.onlyofficeUrl = pObj.onlyofficeUrl;

model.userId = user.id;
model.fullName = user.fullName;

