package com.parashift.onlyoffice;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.UrlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Map;

 /*
    Copyright (c) Ascensio System SIA 2019. All rights reserved.
    http://www.onlyoffice.com
*/

@Service
public class Util {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    SysAdminParams sysAdminParams;

    @Autowired
    NodeService nodeService;

    public String getKey(NodeRef nodeRef) {
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        return nodeRef.getId() + "_" + dateFormat.format(properties.get(ContentModel.PROP_MODIFIED));
    }

    public String getContentUrl(NodeRef nodeRef) {
        return  UrlUtil.getAlfrescoUrl(sysAdminParams) + "/s/api/node/content/workspace/SpacesStore/" + nodeRef.getId() + "?alf_ticket=" + authenticationService.getCurrentTicket();
    }

    public String getCallbackUrl(NodeRef nodeRef) {
        return UrlUtil.getAlfrescoUrl(sysAdminParams) + "/s/parashift/onlyoffice/callback?nodeRef=" + nodeRef.toString() + "&alf_ticket=" + authenticationService.getCurrentTicket();
    }

    public String getConversionUrl(String key) {
        return UrlUtil.getAlfrescoUrl(sysAdminParams) + "/s/parashift/onlyoffice/converter?key=" + key + "&alf_ticket=" + authenticationService.getCurrentTicket();
    }
}
