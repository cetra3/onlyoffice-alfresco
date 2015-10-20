package com.parashift.onlyoffice;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;

/**
 * Created by cetra on 20/10/15.
 */
@Component(value = "webscript.onlyoffice.callback.post")
public class CallBack extends AbstractWebScript {

    @Autowired
    LockService lockService;

    @Autowired
    ContentService contentService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {

        logger.debug("Received JSON Callback");
        JSONObject callBackJSon = new JSONObject(request.getContent().getContent());

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

