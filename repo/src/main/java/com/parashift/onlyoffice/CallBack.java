package com.parashift.onlyoffice;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by cetra on 20/10/15.
 */
 /*
    Copyright (c) Ascensio System SIA 2019. All rights reserved.
    http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.callback.post")
public class CallBack extends AbstractWebScript {

    @Autowired
    LockService lockService;

    @Autowired
    @Qualifier("policyBehaviourFilter")
    BehaviourFilter behaviourFilter;

    @Autowired
    ContentService contentService;

    @Autowired
    ConfigManager configManager;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {

        logger.debug("Received JSON Callback");
        try {
            JSONObject callBackJSon = new JSONObject(request.getContent().getContent());

            logger.debug(callBackJSon.toString(3));

            String[] keyParts = callBackJSon.getString("key").split("_");
            NodeRef nodeRef = new NodeRef("workspace://SpacesStore/" + keyParts[0]);

            //Status codes from here: https://api.onlyoffice.com/editors/editor

            int saved = 0;
            switch(callBackJSon.getInt("status")) {
                case 0:
                    logger.error("ONLYOFFICE has reported that no doc with the specified key can be found");
                    lockService.unlock(nodeRef);
                    break;
                case 1:
                    if(lockService.getLockStatus(nodeRef).equals(LockStatus.NO_LOCK)) {
                        logger.debug("Document open for editing, locking document");
                        behaviourFilter.disableBehaviour(nodeRef);
                        lockService.lock(nodeRef, LockType.WRITE_LOCK);
                    } else {
                        logger.debug("Document already locked, another user has entered/exited");
                    }
                    break;
                case 2:
                    logger.debug("Document Updated, changing content");
                    lockService.unlock(nodeRef);
                    if (!updateNode(nodeRef, callBackJSon.getString("url")))
                    {
                        saved = 1;
                    }
                    break;
                case 3:
                    logger.error("ONLYOFFICE has reported that saving the document has failed");
                    lockService.unlock(nodeRef);
                    break;
                case 4:
                    logger.debug("No document updates, unlocking node");
                    lockService.unlock(nodeRef);
                    break;
            }

            response.getWriter().write("{\"error\":" + saved + "}");
        } catch (JSONException ex) {
            throw new WebScriptException("Unable to deserialize JSON: " + ex.getMessage());
        }
    }

    private boolean updateNode(NodeRef nodeRef, String url) {
        logger.debug("Retrieving URL:{}", url);

        try {
            checkCert();
            InputStream in = new URL( url ).openStream();
            contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true).putContent(in);
        } catch (IOException e) {
            logger.error(ExceptionUtils.getFullStackTrace(e));
            return false;
        }
        return true;
    }

    private void checkCert() {
        String cert = (String) configManager.getOrDefault("cert", "no");
        if (cert.equals("true")) {
            TrustManager[] trustAllCerts = new TrustManager[]
            {
                new X509TrustManager()
                {
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers()
                    {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType)
                    {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType)
                    {
                    }
                }
            };

            SSLContext sc;

            try
            {
                sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            }
            catch (NoSuchAlgorithmException | KeyManagementException ex)
            {
            }

            HostnameVerifier allHostsValid = new HostnameVerifier()
            {
                @Override
                public boolean verify(String hostname, SSLSession session)
                {
                return true;
                }
            };

            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        }
    }
}

