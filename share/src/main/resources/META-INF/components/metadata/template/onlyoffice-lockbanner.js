(function() {

    /**
      * YUI Library aliases
      */
    var Dom = YAHOO.util.Dom;

    /**
     * Alfresco Slingshot aliases
     */
    var $html = Alfresco.util.encodeHTML,
        $isValueSet = Alfresco.util.isValueSet;
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

   /**
    * Onlyoffice constructor.
    *
    * @param {String} htmlId The HTML id of the parent element
    * @return {Alfresco.component.onlyoffice} The new Path instance
    * @constructor
    */
   Alfresco.component.Onlyoffice = function(htmlId)
   {
      Alfresco.component.Onlyoffice.superclass.constructor.call(this, "Alfresco.component.Onlyoffice", htmlId);

      Alfresco.util.ComponentManager.reregister(this);

      this.options = YAHOO.lang.merge(this.options, Alfresco.component.Onlyoffice.superclass.options);

      return this;
   };

//   YAHOO.extend(Alfresco.component.Onlyoffice, Alfresco.component.Path,
//   {
//
//           /**
//        * Event handler called when the "documentDetailsAvailable" event is received
//        *
//        * @method: onDocumentDetailsAvailable
//        */
//      onDocumentDetailsAvailable: function Onlyoffice_onDocumentDetailsAvailable(layer, args)
//      {
//         var docData = args[1].documentDetails,
//            pathHtml,
//            rootLink = this.options.rootPage,
//            pathUrl = "",
//            folders = [],
//            href;
//
//         var path = docData.location.path;
//
//         // Document Library root node
//         if (path.length < 2)
//         {
//            pathHtml = '<span class="path-link"><a href="' + $siteURL(rootLink + "?file=" + encodeURIComponent(docData.fileName)) + '">' + this.msg(this.options.rootLabelId) + '</a></span>';
//         }
//         else
//         {
//            pathHtml = '<span class="path-link"><a href="' + $siteURL(rootLink) + '">' + this.msg(this.options.rootLabelId) + '</a></span>';
//            folders = path.substring(1, path.length).split("/");
//
//            if (folders.length > 0)
//            {
//               pathHtml += '<span class="separator"> &gt; </span>';
//            }
//
//            for (var x = 0, y = folders.length; x < y; x++)
//            {
//               pathUrl += "/" + folders[x];
//               href = rootLink + (y - x < 2 ? "?file=" + encodeURIComponent(docData.fileName) + "&path=" : "?path=") + encodeURIComponent(pathUrl);
//               pathHtml += '<span class="path-link folder"><a href="' + $siteURL(href) + '">' + $html(folders[x]) + '</a></span>';
//
//               if (y - x > 1)
//               {
//                  pathHtml += '<span class="separator"> &gt; </span>';
//               }
//            }
//         }
//
//         Dom.setStyle(this.id + "-defaultPath", "display", "none");
//         Dom.get(this.id + "-path").innerHTML = pathHtml;
//
//         Dom.addClass(this.id + "-status", "hidden");
//
//         if (docData.custom && (docData.custom.isWorkingCopy || docData.custom.hasWorkingCopy))
//         {
//            var bannerMsg, bannerStatus;
//
//            if (docData.lockedByUser && docData.lockedByUser !== "")
//            {
//               var lockedByLink = $userProfile(docData.lockedByUser, docData.lockedBy, 'class="theme-color-1"');
//
//               /* Working Copy handling */
//               if (docData.lockedByUser === Alfresco.constants.USERNAME)
//               {
//                  // Locked / Working Copy handling
//                  bannerStatus = docData.actionSet === "lockOwner" ? "lock-owner" : "editing";
//                  bannerMsg = this.msg("banner." + bannerStatus);
//               }
//               else
//               {
//                  bannerStatus = "locked";
//                  bannerMsg = this.msg("banner.locked", lockedByLink);
//               }
//            }
//
//            if (bannerMsg)
//            {
//               Dom.get(this.id + "-status").innerHTML = '<span class="' + $html(bannerStatus) + '">' + bannerMsg + 'overwrite</span>';
//               Dom.removeClass(this.id + "-status", "hidden");
//            }
//
//            YAHOO.Bubbling.fire("recalculatePreviewLayout");
//         }
//      }
//   });
})();
