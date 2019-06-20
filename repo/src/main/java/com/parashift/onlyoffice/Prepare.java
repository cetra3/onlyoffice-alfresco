package com.parashift.onlyoffice;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.namespace.QName;
import org.alfresco.repo.i18n.MessageService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.connector.User;
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
    MessageService mesService;

    @Autowired
    PersonService personService;

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
            String username = AuthenticationUtil.getFullyAuthenticatedUser();
            NodeRef person = personService.getPersonOrNull(username);
            PersonInfo personInfo = null;
            if (person != null) {
                personInfo = personService.getPerson(person);
            }

            JSONObject responseJson = new JSONObject();
            JSONObject documentObject = new JSONObject();
            JSONObject editorConfigObject = new JSONObject();
            JSONObject userObject = new JSONObject();
            JSONObject permObject = new JSONObject();

            try {
                String docTitle = (String) properties.get(ContentModel.PROP_NAME);
                String docExt = docTitle.substring(docTitle.lastIndexOf(".") + 1).trim().toLowerCase();

                responseJson.put("type", "desktop");
                responseJson.put("width", "100%");
                responseJson.put("height", "100%");
                responseJson.put("documentType", getDocType(docExt));

                responseJson.put("document", documentObject);
                documentObject.put("title", docTitle);
                documentObject.put("url", contentUrl);
                documentObject.put("fileType", docExt);
                documentObject.put("key", key);
                documentObject.put("permissions", permObject);
                permObject.put("edit", true);

                responseJson.put("editorConfig", editorConfigObject);
                editorConfigObject.put("lang", mesService.getLocale().toLanguageTag());
                editorConfigObject.put("mode", "edit");
                editorConfigObject.put("callbackUrl", callbackUrl);
                editorConfigObject.put("user", userObject);
                userObject.put("id", username);

                if (personInfo == null) {
                    userObject.put("name", username);
                } else {
                    userObject.put("firstname", personInfo.getFirstName());
                    userObject.put("lastname", personInfo.getLastName());
                    userObject.put("name", personInfo.getFirstName() + " " + personInfo.getLastName());
                }

                if (jwtManager.jwtEnabled()) {
                    responseJson.put("token", jwtManager.createToken(responseJson));
                }

                responseJson.put("onlyofficeUrl", configManager.getOrDefault("url", "http://127.0.0.1/"));

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

    private String getDocType(String ext) {
        if (".doc.docx.docm.dot.dotx.dotm.odt.fodt.ott.rtf.txt.html.htm.mht.pdf.djvu.fb2.epub.xps".indexOf(ext) != -1) return "text";
        if (".xls.xlsx.xlsm.xlt.xltx.xltm.ods.fods.ots.csv".indexOf(ext) != -1) return "spreadsheet";
        if (".pps.ppsx.ppsm.ppt.pptx.pptm.pot.potx.potm.odp.fodp.otp".indexOf(ext) != -1) return "presentation";
        return null;
    }
}
