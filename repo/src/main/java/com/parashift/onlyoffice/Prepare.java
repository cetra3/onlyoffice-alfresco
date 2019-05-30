package com.parashift.onlyoffice;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by cetra on 20/10/15.
 * Sends Alfresco Share the necessaries to build up what information is needed for the OnlyOffice server
 */
 /*
    Copyright (c) Ascensio System SIA 2019. All rights reserved.
    http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.prepare.get")
public class Prepare extends AbstractWebScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    NodeService nodeService;

    @Autowired
    ConfigManager configManager;

    @Autowired
    JwtManager jwtManager;

    @Autowired
    Util util;

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        if (request.getParameter("nodeRef") != null) {

            NodeRef nodeRef = new NodeRef(request.getParameter("nodeRef"));

            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

            response.setContentType("application/json; charset=utf-8");
            response.setContentEncoding("UTF-8");

            String contentUrl = util.getContentUrl(nodeRef);
            String key = util.getKey(nodeRef);
            String callbackUrl = util.getCallbackUrl(nodeRef);

            JSONObject responseJson = new JSONObject();

            try {
                responseJson.put("docUrl", contentUrl);
                responseJson.put("callbackUrl", callbackUrl);
                responseJson.put("onlyofficeUrl", configManager.getOrDefault("url", "http://127.0.0.1/"));
                responseJson.put("key", key);
                responseJson.put("docTitle", properties.get(ContentModel.PROP_NAME));

                if (jwtManager.jwtEnabled()) {
                    responseJson.put("token", jwtManager.createToken(responseJson));
                }

                logger.debug("Sending JSON prepare object");
                logger.debug(responseJson.toString(3));

                response.getWriter().write(responseJson.toString(3));
            } catch (JSONException ex) {
                throw new WebScriptException("Unable to serialize JSON: " + ex.getMessage());
            } catch (Exception ex) {
                throw new WebScriptException("Unable to create JWT token: " + ex.getMessage());
            }
        }
    }
}
