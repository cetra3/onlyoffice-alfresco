var mimeTypes = [
    "application/vnd.ms-excel",
    "application/vnd.ms-powerpoint",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/vnd.ms-word.document.macroenabled.12",
    "application/vnd.ms-word.template.macroenabled.12",
    "application/vnd.ms-excel.sheet.macroenabled.12",
    "application/vnd.ms-excel.template.macroenabled.12",
    "application/vnd.ms-powerpoint.presentation.macroenabled.12",
    "application/vnd.ms-powerpoint.template.macroenabled.12",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
    "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
    "application/vnd.ms-powerpoint.slideshow.macroenabled.12",
    "application/vnd.openxmlformats-officedocument.presentationml.template",
    "application/vnd.ms-powerpoint.addin.macroenabled.12",
    "application/vnd.openxmlformats-officedocument.presentationml.slide",
    "application/vnd.ms-powerpoint.slide.macroenabled.12",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
    "application/vnd.ms-excel.addin.macroenabled.12",
    "application/vnd.ms-excel.sheet.binary.macroenabled.12"
]

if (model.widgets)
{
    for (var i = 0; i < model.widgets.length; i++)
    {
        var widget = model.widgets[i];
        if (widget.id == "WebPreview")
        {

            pObj = eval('(' + remote.call("/parashift/onlyoffice/prepare?nodeRef=" + url.args.nodeRef) + ')');

            if(pObj.status == "OK" && mimeTypes.indexOf(pObj.mimeType) != -1) {

                model.onlyofficeUrl = pObj.onlyofficeUrl;

                pObj.user = {
                    userId: user.id,
                    firstName: user.firstName,
                    lastName: user.lastName
                }

                widget.options.pluginConditions = jsonUtils.toJSONString([{
                    attributes: {
                        mimeType: pObj.mimeType
                    },
                    plugins: [{
                        name: "OnlyOffice",
                        attributes: pObj
                    }]
                }]);
            }

        }
    }
}
