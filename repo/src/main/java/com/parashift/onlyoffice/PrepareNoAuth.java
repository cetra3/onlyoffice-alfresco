package com.parashift.onlyoffice;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
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
import java.util.Map;
import java.util.Properties;

/**
 * Created by cetra on 20/10/15.
 * Sends Alfresco Share the necessaries to build up what information is needed for the OnlyOffice server
 */
@Component(value = "webscript.onlyoffice.prepare-noauth.get")
public class PrepareNoAuth extends AbstractWebScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    OnlyOfficeService onlyOfficeService;

    @Autowired
    QuickShareService quickShareService;

    @Autowired
    NodeService nodeService;

    @Resource(name = "global-properties")
    Properties globalProp;

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        if (request.getParameter("sharedId") != null) {

            String sharedId = request.getParameter("sharedId");

            Pair<String, NodeRef> sharedPair = quickShareService.getTenantNodeRefFromSharedId(sharedId);

            NodeRef nodeRef = sharedPair.getSecond();

            JSONObject responseJson = new JSONObject();

            response.setContentType("application/json; charset=utf-8");
            response.setContentEncoding("UTF-8");

            if(nodeService.exists(nodeRef)) {
                Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

                if(properties.containsKey(ContentModel.PROP_CONTENT)) {

                    String contentUrl = onlyOfficeService.getSharedContentUrl(sharedId);
                    String key =  onlyOfficeService.getKey(nodeRef);

                    ContentData contentData = (ContentData) properties.get(ContentModel.PROP_CONTENT);

                    responseJson.put("docUrl", contentUrl);
                    responseJson.put("onlyofficeUrl", onlyOfficeService.getOnlyOfficeUrl());
                    responseJson.put("key", key);
                    responseJson.put("docTitle", properties.get(ContentModel.PROP_NAME));
                    responseJson.put("mimeType", contentData.getMimetype());

                    if(globalProp.containsKey("onlyoffice.lang")) {
                        responseJson.put("lang", globalProp.get("onlyoffice.lang"));
                    }

                    responseJson.put("status", "OK");
                } else {
                    responseJson.put("status", "Not A File");
                }

            } else {
                responseJson.put("status", "Node Not Found");
            }

            logger.debug("Sending JSON prepare object");
            logger.debug(responseJson.toString(3));

            try(Writer responseWriter = response.getWriter()) {
                responseJson.write(responseWriter);
            }

        }
    }
}
