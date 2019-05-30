package com.parashift.onlyoffice;

import java.io.IOException;

import org.alfresco.service.cmr.repository.ContentReader;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.Pair;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

/*
    Copyright (c) Ascensio System SIA 2019. All rights reserved.
    http://www.onlyoffice.com
*/
@Component(value = "webscript.onlyoffice.converter.get")
public class ConverterCallback extends AbstractWebScript {

    @Autowired
    Converter converterService;

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        String key = request.getParameter("key");

        try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
            if (key != null) {
                Pair<String, ContentReader> pair = converterService.GetConversion(key);

                ContentReader reader = pair.getSecond();
                String mime = pair.getFirst();

                if (reader != null) {
                    response.setHeader("Content-Disposition", "attachment; filename=" + key);
                    response.setContentType(mime);
                    response.setContentEncoding(reader.getEncoding());
                    response.setHeader("Content-Length", Long.toString(reader.getSize()));
                    reader.getContent(response.getOutputStream());
                    return;
                }
            }
        }
        response.setStatus(404);
    }
}

