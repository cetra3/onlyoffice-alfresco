<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

/**
 * Gets icon resource URL for specified {node} parameter.
 *
 * The URL is composed by {url.context} + "/res/" +
 *  - icon resource string path from {style} configuration that corresponds with matching filter from
 *     share-documentlibrary-config.xml [CommonComponentStyle][component-style], {browse.folder} component
 *  - "components/images/filetypes/generic-folder-48.png" if there are no matching filters.
 *
 * @param node
 * @returns icon resource URL for specified {node} parameter.
 */
function getFolderIcon(node)
{
   var conf = AlfrescoUtil.getCommonConfigStyle();
   var folderCommonStyleConfig = {};
   if (conf)
   {
      folderCommonStyleConfig = JSON.parse(conf).browse.folder;
   }
   var defaultIcon = "components/images/filetypes/generic-folder-48.png";
   var iconStr = AlfrescoUtil.getResourceIcon(node, folderCommonStyleConfig, defaultIcon, "48x48");
   return url.context + "/res/" + iconStr;
}
function main()
{
   AlfrescoUtil.param("nodeRef");
   AlfrescoUtil.param("site", null);
   AlfrescoUtil.param("rootPage", "documentlibrary");
   AlfrescoUtil.param("rootLabelId", "path.documents");
   AlfrescoUtil.param("showOnlyLocation", "false");
   AlfrescoUtil.param("showFavourite", "true");
   AlfrescoUtil.param("showLikes", "true");
   AlfrescoUtil.param("showComments", "true");
   AlfrescoUtil.param("showQuickShare", "true");
   AlfrescoUtil.param("showDownload", "true");
   AlfrescoUtil.param("showPath", "true");
   AlfrescoUtil.param("libraryRoot", null);
   AlfrescoUtil.param("pagecontext", null);
   AlfrescoUtil.param("showItemModifier", "true");
   var nodeDetails = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site, null, model.libraryRoot);
   if (nodeDetails)
   {
      var supressSocial = AlfrescoUtil.isComponentSuppressed(nodeDetails.item.node, AlfrescoUtil.getSupressSocialfolderDetailsConfig());
      var folderIcon = null;
      if (nodeDetails.item.node.isContainer)
      {
         folderIcon = getFolderIcon(nodeDetails.item.node)
      }
      model.item = nodeDetails.item;
      model.node = nodeDetails.item.node;
      model.isContainer = nodeDetails.item.node.isContainer;
      model.folderIcon = folderIcon;
      model.paths = AlfrescoUtil.getPaths(nodeDetails, model.rootPage, model.rootLabelId);
      model.showQuickShare = (!model.isContainer && model.showQuickShare && (config.scoped["Social"]["quickshare"].getChildValue("url") != null) && quickShareStatus.enabled).toString();
      model.isWorkingCopy = (model.item && model.item.workingCopy && model.item.workingCopy.isWorkingCopy) ? true : false;
      model.showFavourite = (model.isWorkingCopy || supressSocial ? false : model.showFavourite).toString();
      model.showLikes = (model.isWorkingCopy || supressSocial ? false : model.showLikes).toString();
      model.showComments = (model.isWorkingCopy || supressSocial ? false : ((nodeDetails.item.node.permissions.user["CreateChildren"] || false) && model.showComments)).toString();
      model.showDownload = (!model.isContainer && model.showDownload).toString();
      model.showOnlyLocation = model.showOnlyLocation.toString();
      var count = nodeDetails.item.node.properties["fm:commentCount"];
      model.commentCount = (count != undefined ? count : null);
      model.lock = (nodeDetails.item.node.properties["cm:lockType"] != undefined ? nodeDetails.item.node.properties["cm:lockType"] : null);

      var suppressConfig = AlfrescoUtil.getSupressConfig();
      var supressDateFolderDetailsConfig = {};
      if (suppressConfig)
      {
         supressDateFolderDetailsConfig = JSON.parse(suppressConfig).date.details.folder;
      }
      var supressDate = AlfrescoUtil.isComponentSuppressed(nodeDetails.item.node, supressDateFolderDetailsConfig);

      model.showItemModifier = (!supressDate).toString();

      // Widget instantiation metadata...
      var likes = {};
      if (model.item.likes != null)
      {
         likes.isLiked = model.item.likes.isLiked || false;
         likes.totalLikes = model.item.likes.totalLikes || 0;
      }

      // check whether it is editing in only office
      model.editInOnlyOffice = false;
      for (i = 0; i < model.item.node.aspects.length; i++) {
         if (model.item.node.aspects[i] == "oo:currentlyEditing") {
             model.editInOnlyOffice = true;
         }
      }

      model.hasWritePermission = model.item.node.permissions.user["Write"];
      model.editInOnlyOfficeLink = model.hasWritePermission ? "<a href='onlyoffice-edit?nodeRef=" + model.nodeRef + "' target='_blank'>here</a>" : "";

      var nodeHeader = {
         id : "NodeHeader",
         name : "Alfresco.component.NodeHeader",
         options : {
            nodeRef : model.nodeRef,
            siteId : model.site,
            actualSiteId: model.item.location.site != null ? model.item.location.site.name : null,
            rootPage : model.rootPage,
            rootLabelId : model.rootLabelId,
            showOnlyLocation: (model.showOnlyLocation == "true"),
            showQuickShare: (model.showQuickShare == "true"),
            showFavourite : (model.showFavourite == "true"),
            showLikes : (model.showLikes == "true"),
            showComments : (model.showComments == "true"),
            showDownload : (model.showDownload == "true"),
            showPath : (model.showPath == "true"),
            displayName : (model.item.displayName != null) ? model.item.displayName : model.item.fileName,
            likes : likes,
            isFavourite : (model.item.isFavourite || false),
            isContainer : model.isContainer,
            sharedId: model.item.node.properties["qshare:sharedId"] || null,
            sharedBy: model.item.node.properties["qshare:sharedBy"] || null,
            pagecontext: model.pagecontext,
            libraryRoot: model.libraryRoot,
            lock: model.lock,
            folderIcon: model.folderIcon,
            showItemModifier: (model.showItemModifier == "true"),
            hasWritePermission: model.hasWritePermission,
            editInOnlyOffice: model.editInOnlyOffice,
            editInOnlyOfficeLink: model.editInOnlyOfficeLink
         }
      };

      if(nodeDetails.item.workingCopy != null && nodeDetails.item.workingCopy.isWorkingCopy)
      {
         nodeHeader.options.showFavourite = false;
         nodeHeader.options.showLikes = false;
         model.showQuickShare = "false";
         model.showComments = "false";
      }

      model.widgets = [nodeHeader];
   }
}

main();