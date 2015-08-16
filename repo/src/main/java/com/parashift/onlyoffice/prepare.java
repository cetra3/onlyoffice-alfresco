package com.parashift.onlyoffice;

import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.util.UrlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptResponse;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by cetra on 16/08/15.
 */
@WebScript
public class prepare {

    @Autowired
    SysAdminParams sysAdminParams;

    @Autowired
    AuthenticationService authenticationService;

    @Uri(method = HttpMethod.GET, value = "/parashift/onlyoffice/prepare", defaultFormat = "html")
    public void handlePrepare(final HttpServletRequest request, final WebScriptResponse response) throws IOException {
        if (request.getParameter("nodeRef") != null) {

            NodeRef nodeRef = new NodeRef(request.getParameter("nodeRef"));

            String contentUrl = UrlUtil.getAlfrescoUrl(sysAdminParams) + "api/node/content" + nodeRef.getStoreRef() + "/" + nodeRef.getId() + "?alf_ticket=" + authenticationService.getCurrentTicket();

            response.getWriter().write(contentUrl);

        }
    }

}
