package com.parashift.onlyoffice;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/*
    Copyright (c) Ascensio System SIA 2019. All rights reserved.
    http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.config.post")
public class ConfigCallback extends AbstractWebScript {

    @Autowired
    ConfigManager configManager;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Pattern urlRegex = Pattern.compile("http(s)?://.*");

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {

        logger.debug("Received new configuration");
        try {
            JSONObject data = new JSONObject(request.getContent().getContent());

            logger.debug(data.toString(3));

            String docUrl = data.getString("url");
            Matcher m = urlRegex.matcher(docUrl);

            if (m.matches()) {
                if (!docUrl.endsWith("/")) {
                    docUrl = docUrl + "/";
                }
                configManager.set("url", docUrl);
            } else {
                response.getWriter().write("{\"success\": false, \"message\": \"Unable to parse hostname\"}");
                return;
            }

            configManager.set("cert", data.getString("cert"));
            configManager.set("jwtsecret", data.getString("jwtsecret"));

            response.getWriter().write("{\"success\": true}");
        } catch (JSONException ex) {
            String msg = "Unable to deserialize JSON: " + ex.getMessage();
            logger.debug(msg);
            response.getWriter().write("{\"success\": false, \"message\": \"" + msg + "\"}");
        }
    }
}

