pObj = eval('(' + remote.call("/parashift/onlyoffice/prepare?nodeRef=" + url.args.nodeRef) + ')');
model.callbackUrl = pObj.callbackUrl;
model.docTitle = pObj.docTitle;
model.docUrl = pObj.docUrl;
model.key = pObj.key;
model.onlyofficeUrl = pObj.onlyofficeUrl;
model.abovePreviewThreshold = pObj.abovePreviewThreshold;
model.timeout = pObj.timeout;

if(pObj.lang) {
  model.lang = pObj.lang;
}

model.userId = user.id;
model.firstName = user.firstName;
model.lastName = user.lastName;
