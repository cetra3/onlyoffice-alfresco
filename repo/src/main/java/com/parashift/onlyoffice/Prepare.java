package com.parashift.onlyoffice;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.UrlUtil;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Properties;

/**
 * Created by cetra on 20/10/15.
 * Sends Alfresco Share the necessaries to build up what information is needed for the OnlyOffice server
 */
 /*
    Copyright (c) Ascensio System SIA 2017. All rights reserved.
    http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.prepare.get")
public class Prepare extends AbstractWebScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    @Autowired
    NodeService nodeService;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    SysAdminParams sysAdminParams;

    @Resource(name = "global-properties")
    Properties globalProp;

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        if (request.getParameter("nodeRef") != null) {

            NodeRef nodeRef = new NodeRef(request.getParameter("nodeRef"));

            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

            response.setContentType("application/json; charset=utf-8");
            response.setContentEncoding("UTF-8");

            String contentUrl = UrlUtil.getAlfrescoUrl(sysAdminParams) + "/s/api/node/content/workspace/SpacesStore/" + nodeRef.getId() + "?alf_ticket=" + authenticationService.getCurrentTicket();
            String key = nodeRef.getId() + "_" + dateFormat.format(properties.get(ContentModel.PROP_MODIFIED));
            String callbackUrl = UrlUtil.getAlfrescoUrl(sysAdminParams) + "/s/parashift/onlyoffice/callback?nodeRef=" + nodeRef.toString() + "&alf_ticket=" + authenticationService.getCurrentTicket();

            JSONObject responseJson = new JSONObject();
            responseJson.put("docUrl", contentUrl);
            responseJson.put("callbackUrl", callbackUrl);
            responseJson.put("onlyofficeUrl", globalProp.getOrDefault("onlyoffice.url", "http://127.0.0.1/"));
            responseJson.put("key", key);
            responseJson.put("docTitle", properties.get(ContentModel.PROP_NAME));

            logger.debug("Sending JSON prepare object");
            logger.debug(responseJson.toString(3));

            response.getWriter().write(responseJson.toString(3));

        }
    }
}
