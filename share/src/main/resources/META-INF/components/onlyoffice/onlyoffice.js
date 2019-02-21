/*
    Copyright (c) Ascensio System SIA 2019. All rights reserved.
    http://www.onlyoffice.com
*/
function key(k) {
    var result = k.replace(new RegExp("[^0-9-.a-zA-Z_=]", "g"), "_") + (new Date()).getTime();
    return result.substring(result.length - Math.min(result.length, 20));
};

var getDocumentType = function (ext) {
    if (".doc.docx.docm.dot.dotx.dotm.odt.fodt.ott.rtf.txt.html.htm.mht.pdf.djvu.fb2.epub.xps".indexOf(ext) != -1) return "text";
    if (".xls.xlsx.xlsm.xlt.xltx.xltm.ods.fods.ots.csv".indexOf(ext) != -1) return "spreadsheet";
    if (".pps.ppsx.ppsm.ppt.pptx.pptm.pot.potx.potm.odp.fodp.otp".indexOf(ext) != -1) return "presentation";
    return null;
};
