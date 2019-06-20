package com.parashift.onlyoffice;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/*
    Copyright (c) Ascensio System SIA 2019. All rights reserved.
    http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.config.post")
public class ConfigCallback extends AbstractWebScript {

    @Autowired
    ConfigManager configManager;

    @Autowired
    JwtManager jwtManager;

    @Autowired
    @Qualifier("global-properties")
    Properties globalProp;

    @Autowired
    Converter converter;

    @Autowired
    Util util;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {

        logger.debug("Received new configuration");
        try {
            JSONObject data = new JSONObject(request.getContent().getContent());

            logger.debug(data.toString(3));

            String docUrl = data.getString("url").trim();
            String jwtSecret = data.getString("jwtsecret").trim();

            if (!docUrl.endsWith("/")) {
                docUrl = docUrl + "/";
            }

            configManager.set("url", docUrl);
            configManager.set("cert", data.getString("cert"));
            configManager.set("jwtsecret", jwtSecret);

            String alfrescoProto = (String) globalProp.getOrDefault("alfresco.protocol", "http");

            if (alfrescoProto == "https" && docUrl.toLowerCase().startsWith("http://")) {
                response.getWriter().write("{\"success\": false, \"message\": \"mixedcontent\"}");
                return;
            }

            logger.debug("Checking docserv url");
            if (!CheckDocServUrl(docUrl)) {
                response.getWriter().write("{\"success\": false, \"message\": \"docservunreachable\"}");
                return;
            }

            try {
                logger.debug("Checking docserv commandservice");
                if (!CheckDocServCommandService(docUrl)) {
                    response.getWriter().write("{\"success\": false, \"message\": \"docservcommand\"}");
                    return;
                }

                logger.debug("Checking docserv convert");
                if (!CheckDocServConvert(docUrl)) {
                    response.getWriter().write("{\"success\": false, \"message\": \"docservconvert\"}");
                    return;
                }
            } catch (SecurityException e) {
                response.getWriter().write("{\"success\": false, \"message\": \"jwterror\"}");
                return;
            }

            response.getWriter().write("{\"success\": true}");
        } catch (JSONException ex) {
            String msg = "Unable to deserialize JSON: " + ex.getMessage();
            logger.debug(msg);
            response.getWriter().write("{\"success\": false, \"message\": \"jsonparse\"}");
        }
    }

    private Boolean CheckDocServUrl(String url) {
        try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url + "healthcheck");
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                String content = IOUtils.toString(response.getEntity().getContent(), "utf-8").trim();
                if (content.equalsIgnoreCase("true")) return true;
            }
        } catch (Exception e) {
            logger.debug("/healthcheck error: " + e.getMessage());
        }

        return false;
    }

    private Boolean CheckDocServCommandService(String url) throws SecurityException {
        Integer errorCode = -1;
        try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
            JSONObject body = new JSONObject();
            body.put("c", "version");

            HttpPost request = new HttpPost(url + "coauthoring/CommandService.ashx");

            if (jwtManager.jwtEnabled()) {
                String token = jwtManager.createToken(body);
                body.put("token", token);
                request.setHeader((String) configManager.getOrDefault("jwtheader", "Authorization"), "Bearer " + token);
            }

            StringEntity requestEntity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);
            request.setEntity(requestEntity);
            request.setHeader("Accept", "application/json");

            logger.debug("Sending POST to Docserver: " + body.toString());
            try(CloseableHttpResponse response = httpClient.execute(request)) {
                int status = response.getStatusLine().getStatusCode();

                if(status != HttpStatus.SC_OK) {
                    return false;
                } else {
                    String content = IOUtils.toString(response.getEntity().getContent(), "utf-8");
                    logger.debug("/CommandService content: " + content);
                    JSONObject callBackJSon = null;
                    callBackJSon = new JSONObject(content);

                    if (callBackJSon.isNull("error")) {
                        return false;
                    }

                    errorCode = callBackJSon.getInt("error");
                }
            }
        } catch (Exception e) {
            logger.debug("/CommandService error: " + e.getMessage());
            return false;
        }

        if (errorCode == 6) {
            throw new SecurityException();
        } else if (errorCode != 0) {
            return false;
        } else {
            return true;
        }
    }

    private Boolean CheckDocServConvert(String url) throws SecurityException {
        String key = new SimpleDateFormat("MMddyyyyHHmmss").format(new Date());

        try {
            String newFileUrl = converter.convert(key, "txt", "docx", util.getTestConversionUrl());
            logger.debug("/ConvertService url: " + newFileUrl);

            if (newFileUrl == null || newFileUrl.isEmpty()) {
                return false;
            }
        } catch (Exception e) {
            if (e instanceof SecurityException) {
                throw (SecurityException)e;
            }
            logger.debug("/ConvertService error: " + e.getMessage());
            return false;
        }

        return true;
    }
}

