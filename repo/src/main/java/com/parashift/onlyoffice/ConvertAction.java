package com.parashift.onlyoffice;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/*
    Copyright (c) Ascensio System SIA 2019. All rights reserved.
    http://www.onlyoffice.com
*/

public class ConvertAction extends ActionExecuterAbstractBase {

    // private Logger logger = LoggerFactory.getLogger(this.getClass());
    private org.apache.log4j.Logger logger;

    @Autowired
    Converter converterService;

    @Autowired
    NodeService nodeService;

    @Autowired
    ContentService contentService;

    @Autowired
    MimetypeService mimetypeService;

    @Autowired
    VersionService versionService;

    @Autowired
    CheckOutCheckInService checkOutCheckInService;

    @Autowired
    Util util;

    public ConvertAction() {
        logger = org.apache.log4j.Logger.getLogger(this.getClass());
        
        PatternLayout layout = new PatternLayout("%d{ISO8601} [%t] %-5p %c %x - %m%n");

        RollingFileAppender fileAppender;
        try {
            fileAppender = new RollingFileAppender(layout, "onlyoffice.log");
            logger.addAppender(fileAppender);
            logger.setLevel(Level.ALL);
        } catch (IOException e) {
        }
    }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        if (nodeService.exists(actionedUponNodeRef)) {
            ContentReader reader = contentService.getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);
            String nodeName = (String) nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_NAME);
            String mime = reader.getMimetype();

            String targetMimeParam = converterService.GetModernMimetype(mime);
            String newName = nodeName.substring(0, nodeName.lastIndexOf('.') + 1)
                    + mimetypeService.getExtension(targetMimeParam);
    
            logger.debug("Converting '" + nodeName + "' -> '" + newName + "'");
    
            ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(actionedUponNodeRef);
            if (parentAssoc == null || parentAssoc.getParentRef() == null) {
                logger.debug("Couln't find parent folder");
                return;
            }
            NodeRef parentRef = parentAssoc.getParentRef();
    
            NodeRef node = nodeService.getChildByName(parentRef, ContentModel.ASSOC_CONTAINS, newName);
            NodeRef chkout = null;
            
            Boolean shouldDelete = false;

            if (node == null) {
                logger.debug("Creating new node");
                shouldDelete = true;

                Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
                props.put(ContentModel.PROP_NAME, newName);
    
                node = this.nodeService.createNode(parentRef, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, newName), ContentModel.TYPE_CONTENT, props)
                    .getChildRef();
            } else {
                versionService.ensureVersioningEnabled(node, null);
                logger.debug("Checking out node");
                chkout = checkOutCheckInService.checkout(node);
            }

            ContentWriter writer = this.contentService.getWriter(chkout != null ? chkout : node, ContentModel.PROP_CONTENT, true);
            writer.setMimetype(targetMimeParam);
    
            try {
                logger.debug("Invoking .transform()");
                converterService.transform(reader, writer, new TransformationOptions(actionedUponNodeRef, null, node, null));

                if (chkout != null) {
                    logger.debug("Checking in node");
                    Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
                    versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);	
                    checkOutCheckInService.checkin(chkout, versionProperties);
                }
            } catch (Exception ex) {
                logger.debug("Couldn't transform: " + ex.getMessage());
                if (shouldDelete) {
                    logger.debug("Deleting created node");
                    nodeService.deleteNode(node);
                }
            } finally {
                if (checkOutCheckInService.isCheckedOut(node)) {
                    logger.debug("Finally: cancelCheckout()");
                    checkOutCheckInService.cancelCheckout(node);
                }
            }
        }
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) { }
}

