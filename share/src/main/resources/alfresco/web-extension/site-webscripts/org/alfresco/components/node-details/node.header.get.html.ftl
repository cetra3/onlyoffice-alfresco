<@standalone>
   <@markup id="css" >
      <#-- CSS Dependencies -->
      <@link href="${url.context}/res/components/node-details/node-header.css" group="node-header"/>
   </@>

   <@markup id="js">
      <#-- JavaScript Dependencies -->
      <@script src="${url.context}/res/components/node-details/node-header.js" group="node-header"/>
   </@>

   <@markup id="widgets">
      <#if item??>
      <@createWidgets group="node-header"/>
      </#if>
   </@>

   <@markup id="html">
      <@uniqueIdDiv>
         <#if item??>
            <#include "../../include/alfresco-macros.lib.ftl" />
            <#assign id = args.htmlid?html>
            <#if !isContainer>
               <#assign fileExtIndex = item.fileName?last_index_of(".")>
               <#if fileExtIndex gt -1>
                  <#assign fileExt = item.fileName?substring(fileExtIndex + 1)?lower_case>
               <#elseif node.mimetype?? && mimetypes.getExtension(node.mimetype)??><#-- Mimetype may be null if it is not known in the repository -->
                  <#assign fileExt = mimetypes.getExtension(node.mimetype)>
               <#else>
                  <#assign fileExt = "generic">
               </#if>
            </#if>
            <#assign displayName = (item.displayName!item.fileName)?html>
            <#assign modifyLabel = "label.modified-by-user-on-date">
            <#assign itemType = isContainer?string("folder", "document")>
            <div class="node-header">
               <!-- Message banner -->
               <#if showOnlyLocation == "false">
                  <#if item.workingCopy??>
                     <#if item.workingCopy.isWorkingCopy??>
                        <#assign lockUser = node.properties["cm:workingCopyOwner"]>
                        <#assign modifyLabel = "label.editing-started-on-date-by-user">
                     <#else>
                        <#assign lockUser = node.properties["cm:lockOwner"]>
                        <#assign modifyLabel = "label.modified-by-user-on-date">
                     </#if>
                     <#if lockUser??>
                        <div class="status-banner theme-bg-color-2 theme-border-4">
                        <#assign lockedByLink = userProfileLink(lockUser.userName, lockUser.displayName, 'class="theme-color-1"') >
                        <#if lockUser.userName == user.name>
                           <#assign status><#if node.isLocked>lock-owner<#else>editing</#if></#assign>
                           <span class="${status}">${msg("banner." + status)}</span>
                        <#else>
                           <span class="locked">${msg("banner.locked", lockedByLink)}</span>
                        </#if>
                        </div>
                     </#if>
                  <#elseif (node.isLocked && ( ((node.properties["cm:lockType"]!"") == "WRITE_LOCK") || ((node.properties["cm:lockType"]!"") == "READ_ONLY_LOCK") || ((node.properties["cm:lockType"]!"") == "NODE_LOCK") ))>
                     <#assign lockUser = node.properties["cm:lockOwner"]>
                     <#if lockUser??>
                        <div class="status-banner theme-bg-color-2 theme-border-4">
                        <#assign lockedByLink = userProfileLink(lockUser.userName, lockUser.displayName, 'class="theme-color-1"') >
                        <#if lockUser.userName == user.name>
                           <span class="lock-owner">${msg("banner.lock-owner")}test</span>
                        <#else>
                           <span class="locked">${msg("banner.locked", lockedByLink)}test</span>
                        </#if>
                        </div>
                     </#if>
                  </#if>
               </#if>
               <div class="node-info">
               <#if showPath == "true">
                  <!-- Path-->
                  <div class="node-path">
                     <@renderPaths paths />
                  </div>
               </#if>
               <#if showOnlyLocation == "false">
                  <!-- Icon -->
                  <#if isContainer>
                     <img src="${folderIcon?html}" title="${displayName}" class="node-thumbnail" width="48" />
                  <#else>
                     <img src="${url.context}/res/components/images/filetypes/${fileExt}-file-48.png"
                          onerror="this.src='${url.context}/res/components/images/filetypes/generic-file-48.png'"
                          title="${displayName}" class="node-thumbnail" width="48" />
                  </#if>
                  <!-- Title and Version -->
                  <h1 class="thin dark">
                     ${displayName}<#if !isContainer && !isWorkingCopy><span id="document-version" class="document-version">${item.version}</span></#if>
                  </h1>
                  <!-- Modified & Social -->
                  <div class="node-social">
                  <#if showItemModifier == "true">
                     <span class="item-modifier">
                              <#assign modifyUser = node.properties["cm:modifier"]>
                              <#assign modifyDate = node.properties["cm:modified"]>
                              <#assign modifierLink = userProfileLink(modifyUser.userName, modifyUser.displayName, 'class="theme-color-1"') >
                              ${msg(modifyLabel, modifierLink, "<span id='${id}-modifyDate'>${modifyDate.iso8601}</span>")}
                     </span>
                  </#if>
                  <#if showFavourite == "true">
                     <span id="${id}-favourite" class="item item-separator"></span>
                  </#if>
                  <#if showLikes == "true">
                     <span id="${id}-like" class="item item-separator"></span>
                  </#if>
                  <#if showComments == "true">
                     <span class="item item-separator item-social">
                        <a href="#" name="@commentNode" rel="${item.nodeRef?html}" class="theme-color-1 comment<#if commentCount??> hasComments</#if> ${id}" title="${msg("comment.${itemType}.tip")}" tabindex="0">${msg("comment.${itemType}.label")}</a><#if commentCount??><span class="comment-count">${commentCount}</span></#if>
                     </span>
                  </#if>
                  <#if showQuickShare == "true">
                     <span id="${id}-quickshare" class="item item-separator"></span>
                  </#if>
                  </div>
               </div>
               <div class="node-action">
                  <#if showDownload == "true" && node.contentURL??>
                  <!-- Download Button -->
                  <span class="yui-button yui-link-button onDownloadDocumentClick">
                     <span class="first-child">
                        <a href="${url.context}/proxy/alfresco${node.contentURL?html}?a=true" tabindex="0">${msg("button.download")}</a>
                     </span>
                  </span>
                  </#if>
               </div>
               </#if>
               <div class="clear"></div>
            </div>
         <#else>
            <div class="node-header">
               <div class="status-banner theme-bg-color-2 theme-border-4">
               ${msg("banner.not-found")}
               </div>
            </div>
         </#if>
      </@>
   </@>
</@>
