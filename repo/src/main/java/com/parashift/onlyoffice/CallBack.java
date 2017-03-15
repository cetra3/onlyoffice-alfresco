package com.parashift.onlyoffice;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.Writer;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;

/**
 * Created by cetra on 20/10/15.
 */
@Component(value = "webscript.onlyoffice.callback.post")
public class CallBack extends AbstractWebScript {

    @Autowired
    LockService lockService;

    @Autowired
    NodeService nodeService;

    @Resource(name = "policyBehaviourFilter")
    BehaviourFilter behaviourFilter;

    @Autowired
    ContentService contentService;

    @Autowired
    OnlyOfficeService onlyOfficeService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    @SuppressWarnings("deprecation")
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {

        logger.debug("Received JSON Callback");
        String user = request.getParameter("user");
        String userToken = request.getParameter("usertoken");

        logger.debug("User:{}, User Token:{}", user, userToken);

        JSONObject callBackJSon = new JSONObject(request.getContent().getContent());

        logger.debug(callBackJSon.toString(3));

        if(user != null && userToken != null && onlyOfficeService.getToken(user).contentEquals(userToken)) {

            AuthenticationUtil.runAs(() -> {
                String[] keyParts = callBackJSon.getString("key").split("_");
                NodeRef nodeRef = new NodeRef("workspace://SpacesStore/" + keyParts[0]);

                //Status codes from here: https://api.onlyoffice.com/editors/editor

                switch(callBackJSon.getInt("status")) {
                    case 0:
                        logger.error("ONLYOFFICE has reported that no doc with the specified key can be found");
                        lockService.unlock(nodeRef);
                        nodeService.removeAspect(nodeRef, OnlyOfficeModel.ASPECT_OO_CURRENTLY_EDITING);
                        break;
                    case 1:
                        if(!nodeService.hasAspect(nodeRef, OnlyOfficeModel.ASPECT_OO_CURRENTLY_EDITING)) {
                            logger.debug("Document open for editing, locking document");

                            behaviourFilter.disableBehaviour(nodeRef);

                            nodeService.addAspect(nodeRef, OnlyOfficeModel.ASPECT_OO_CURRENTLY_EDITING, new HashMap<QName, Serializable>());
                            lockService.lock(nodeRef, LockType.WRITE_LOCK);
                        } else {
                            logger.debug("Document already locked, another user has entered/exited");
                        }
                        break;
                    case 2:
                        logger.debug("Document Updated, changing content");
                        lockService.unlock(nodeRef);
                        nodeService.removeAspect(nodeRef, OnlyOfficeModel.ASPECT_OO_CURRENTLY_EDITING);
                        updateNode(nodeRef, callBackJSon.getString("url"));
                        break;
                    case 3:
                        logger.error("ONLYOFFICE has reported that saving the document has failed");
                        lockService.unlock(nodeRef);
                        nodeService.removeAspect(nodeRef, OnlyOfficeModel.ASPECT_OO_CURRENTLY_EDITING);
                        break;
                    case 4:
                        logger.debug("No document updates, unlocking node");
                        lockService.unlock(nodeRef);
                        nodeService.removeAspect(nodeRef, OnlyOfficeModel.ASPECT_OO_CURRENTLY_EDITING);
                        break;
                }

                //Respond as per doco
                try(Writer responseWriter = response.getWriter()) {
                    JSONObject responseJson = new JSONObject();
                    responseJson.put("error", 0);
                    responseJson.write(responseWriter);
                }

                return null;
            }, user);

        } else {

            logger.warn("Invalid User Token for User:{}", user);

            try(Writer responseWriter = response.getWriter()) {
                JSONObject responseJson = new JSONObject();
                responseJson.put("error", 1);
                responseJson.put("errormessage", "Invalid User Token for User:" + user);
                responseJson.write(responseWriter);
            }

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

