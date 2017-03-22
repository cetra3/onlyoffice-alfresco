package com.parashift.onlyoffice;

import org.alfresco.service.cmr.repository.ContentReader;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.Collections;
import java.util.Map;

/**
 * Created by cetra on 20/10/15.
 */
@Component(value = "webscript.onlyoffice.transform.get")
public class TransformGet extends AbstractWebScript {

    @Autowired
    OnlyOfficeService onlyOfficeService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Map<String, ContentReader> readerMap;

    @PostConstruct
    public void init() {
        readerMap = Collections.synchronizedMap(new LruCache<String, ContentReader>(1000));
    }

    public void submitReader(String nodeKey, ContentReader contentReader) {
        logger.debug("Received submit for nodeKey:{}. Reader:{}", nodeKey, contentReader);
        readerMap.put(nodeKey, contentReader);
    }

    public void deleteReader(String nodeKey) {
        logger.debug("Received delete for nodeKey:{}", nodeKey);
        readerMap.remove(nodeKey);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {

        String nodeKey = request.getParameter("nodeKey");

        String token = request.getParameter("token");

        logger.debug("Received Transform Callback. nodeKey:{}, token:{}", nodeKey, token);

        if(token != null && onlyOfficeService.getToken(nodeKey).contentEquals(token)) {

            if(readerMap.containsKey(nodeKey)) {

                logger.debug("Found Reader, sending content");

                ContentReader contentReader = readerMap.get(nodeKey);

                response.setContentType(contentReader.getMimetype());

                try(OutputStream outputStream = response.getOutputStream()) {
                    contentReader.getContent(outputStream);
                }

            } else {

                logger.warn("Could not find reader for nodeKey:{}", nodeKey);

            }

        } else {

            logger.warn("Invalid Token for Transform of Node:{}", nodeKey);

            try(Writer responseWriter = response.getWriter()) {
                JSONObject responseJson = new JSONObject();
                responseJson.put("error", 1);
                responseJson.put("errormessage", "Invalid Token for nodeKey:" + nodeKey);
                responseJson.write(responseWriter);
            }

        }

   }

}

