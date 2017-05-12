/*
    Copyright (c) Ascensio System SIA 2016. All rights reserved.
    http://www.onlyoffice.com
*/
function key(k) {
    var result = k.replace(new RegExp("[^0-9-.a-zA-Z_=]", "g"), "_") + (new Date()).getTime();
    return result.substring(result.length - Math.min(result.length, 20));
};

/*var getDocumentType = function (ext) {
    if (".docx.doc.odt.rtf.txt.html.htm.mht.pdf.djvu.fb2.epub.xps".indexOf(ext) != -1) return "text";
    if (".xls.xlsx.ods.csv".indexOf(ext) != -1) return "spreadsheet";
    if (".pps.ppsx.ppt.pptx.odp".indexOf(ext) != -1) return "presentation";
    return null;
};*/

var getDocumentType = function(ext) {
    if (".dotx.docm.dotm.docx.doc.dot.odt.rtf.txt.html.htm.mht.pdf.djvu.fb2.epub.xps".indexOf(ext) != -1) return "text";
    if (".xltx.xlsm.xltm.xls.xlt.xlsx.ods.csv".indexOf(ext) != -1) return "spreadsheet";
    if (".potx.pptm.potm.ppsm.pps.ppsx.ppt.pot.pptx.odp".indexOf(ext) != -1) return "presentation";
    return null;
};

var translateDocumentType = function(docType) {
    if (".xltx.xlsm.xltm".indexOf(docType) != -1) return "xlsx";
    if (".dotx.docm.dotm".indexOf(docType) != -1) return "docx";
    if (".potx.pptm.potm.ppsm".indexOf(docType) != -1) return "pptx";
    if (".dot".indexOf(docType) != -1) return "doc";
    if (".xlt".indexOf(docType) != -1) return "xls";
    if (".pot".indexOf(docType) != -1) return "ppt";
    return docType;
};

var getEditorType = function() {
    var isIE11 = !!window.MSInputMethodContext && !!document.documentMode;
    return isIE11 ? "embedded" : "desktop";
};