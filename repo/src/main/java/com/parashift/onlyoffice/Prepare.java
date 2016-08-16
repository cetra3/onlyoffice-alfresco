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
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Properties;

/**
 * Created by cetra on 20/10/15.
 * Sends Alfresco Share the necessaries to build up what information is needed for the OnlyOffice server
 */
@Component(value = "webscript.onlyoffice.prepare.get")
public class Prepare extends AbstractWebScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    OnlyOfficeService onlyOfficeService;

    @Autowired
    NodeService nodeService;

    @Resource(name = "global-properties")
    Properties globalProp;

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        if (request.getParameter("nodeRef") != null) {

            NodeRef nodeRef = new NodeRef(request.getParameter("nodeRef"));

            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

            response.setContentType("application/json; charset=utf-8");
            response.setContentEncoding("UTF-8");

            String contentUrl = onlyOfficeService.getContentUrl(nodeRef);
            String key =  onlyOfficeService.getKey(nodeRef);
            String callbackUrl = onlyOfficeService.getCallbackUrl(nodeRef);

            JSONObject responseJson = new JSONObject();
            responseJson.put("docUrl", contentUrl);
            responseJson.put("callbackUrl", callbackUrl);
            responseJson.put("onlyofficeUrl", onlyOfficeService.getOnlyOfficeUrl());
            responseJson.put("key", key);
            responseJson.put("docTitle", properties.get(ContentModel.PROP_NAME));

            if(globalProp.containsKey("onlyoffice.lang")) {
                responseJson.put("lang", globalProp.get("onlyoffice.lang"));
            }

            logger.debug("Sending JSON prepare object");
            logger.debug(responseJson.toString(3));

            try(Writer responseWriter = response.getWriter()) {
                responseJson.write(responseWriter);
            }

        }
    }
}
