package com.parashift.onlyoffice;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.UrlUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Properties;

/**
 * Created by cetra on 16/08/15.
 */
@Component
@WebScript
public class prepare {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    SysAdminParams sysAdminParams;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    NodeService nodeService;

    @Autowired
    ContentService contentService;

    @Autowired
    LockService lockService;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    @Resource(name = "global-properties")
    Properties globalProp;


    @Uri(method = HttpMethod.GET, value = "/parashift/onlyoffice/prepare", defaultFormat = "html")
    public void handlePrepare(final HttpServletRequest request, final WebScriptResponse response) throws IOException {
        if (request.getParameter("nodeRef") != null) {

            NodeRef nodeRef = new NodeRef(request.getParameter("nodeRef"));

            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);


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
            //logger.debug(responseJson.toString(3));

            response.getWriter().write(responseJson.toString(3));

        }
    }

    @Uri(method = HttpMethod.POST, value = "/parashift/onlyoffice/callback", defaultFormat = "html")
    public void handleCallback(final HttpServletRequest request, final WebScriptResponse response) throws IOException {
        JSONObject callBackJSon = new JSONObject(IOUtils.toString(request.getReader()));

        logger.debug("Received JSON Callback");
        logger.debug(callBackJSon.toString(3));

        String[] keyParts = callBackJSon.getString("key").split("_");
        NodeRef nodeRef = new NodeRef("workspace://SpacesStore/" + keyParts[0]);

        //Status codes from here: https://api.onlyoffice.com/editors/editor

        switch(callBackJSon.getInt("status")) {
            case 0:
                logger.error("Onlyoffice has reported that no doc with the specified key can be found");
                lockService.unlock(nodeRef);
                break;
            case 1:
                logger.debug("Document open for editing, locking document");
                lockService.lock(nodeRef, LockType.WRITE_LOCK);
                break;
            case 2:
                logger.debug("Document Updated, changing content");
                updateNode(nodeRef, callBackJSon.getString("url"));
                lockService.unlock(nodeRef);
                break;
            case 3:
                logger.error("Onlyoffice has reported that saving the document has failed");
                lockService.unlock(nodeRef);
                break;
            case 4:
                logger.debug("No document updates, unlocking node");
                lockService.unlock(nodeRef);
                break;
        }

    }

    private void updateNode(NodeRef nodeRef, String url) {
        logger.debug("Retrieving URL:{}", url);

        try {
            InputStream in = new URL( url ).openStream();
            contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true).putContent(in);
        } catch (IOException e) {
           logger.error(ExceptionUtils.getFullStackTrace(e));
        }
    }

}
