(function() {

    /**
      * YUI Library aliases
      */
    var Dom = YAHOO.util.Dom;

    /**
     * Alfresco Slingshot aliases
     */
    var $html = Alfresco.util.encodeHTML,
        $isValueSet = Alfresco.util.isValueSet,
        $combine = Alfresco.util.combinePaths,
        $siteURL = Alfresco.util.siteURL,
        $userProfile = Alfresco.util.userProfileLink;

    if (Alfresco.DocumentList) {

        YAHOO.Bubbling.fire("registerRenderer", {
            propertyName: "ooLockBanner",
            renderer: function showMetadataDescription(record, label) {

                var properties = record.jsNode.properties,
                   bannerUser = properties.lockOwner,
                   bannerUserLink = Alfresco.DocumentList.generateUserLink(this, bannerUser),
                   editLink = "<a href='onlyoffice-edit?nodeRef=" + record.jsNode.nodeRef.nodeRef + "' target='_blank'>here</a>";
                   nodeTypePrefix = "onlyoffice.banner.",
                   html = "";

                if (bannerUser.userName === Alfresco.constants.USERNAME)
                {
                    html = this.msg(nodeTypePrefix + "lock-owner");
                }
                else
                {
                    if (!record.jsNode.hasPermission("Write")) {
                        html =  this.msg(nodeTypePrefix + "locked", bannerUserLink);
                    } else {
                        html = this.msg(nodeTypePrefix + "locked.editable", bannerUserLink, editLink);
                    }
                }
                return html;
            }
        });
    }

})();
