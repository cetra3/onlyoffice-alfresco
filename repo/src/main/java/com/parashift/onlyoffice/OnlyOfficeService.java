package com.parashift.onlyoffice;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.UrlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Properties;

/**
 * Created by cetra on 16/08/2016.
 */
@Component(value = "onlyOfficeService")
public class OnlyOfficeService {

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA256";

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    SysAdminParams sysAdminParams;

    @Autowired
    NodeService nodeService;

    @Resource(name = "global-properties")
    Properties globalProp;

    private byte[] token;

    private String onlyOfficeUrl;
    private String transformUrl;

    @PostConstruct
    public void init() {

        if(globalProp.containsKey("onlyoffice.token")) {
            token = ((String) globalProp.get("onlyoffice.token")).getBytes();
        } else {
            token = ("ParashiftOnlyOffice123456789" + sysAdminParams.getAlfrescoHost()).getBytes();
        }

        if(globalProp.containsKey("onlyoffice.url")) {
            onlyOfficeUrl = (String) globalProp.get("onlyoffice.url");
        } else {
            onlyOfficeUrl = "http://127.0.0.1/";
        }

        if(globalProp.containsKey("onlyoffice.transform.url")) {
            transformUrl = (String) globalProp.get("onlyoffice.transform.url");
        } else {
            transformUrl = onlyOfficeUrl;
        }
    }

    public String getKey(NodeRef nodeRef) {

        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

        return nodeRef.getId() + "_" + dateFormat.format(properties.get(ContentModel.PROP_MODIFIED));
    }


    public String getSharedContentUrl(String sharedId) {
        return UrlUtil.getAlfrescoUrl(sysAdminParams) + "/s/api/internal/shared/node/" + sharedId + "/content";
    }

    public String getContentUrl(NodeRef nodeRef) {
        return  UrlUtil.getAlfrescoUrl(sysAdminParams) + "/s/api/node/content/workspace/SpacesStore/" + nodeRef.getId() + "?alf_ticket=" + authenticationService.getCurrentTicket();
    }

    public String getCallbackUrl(NodeRef nodeRef) {

        String username = authenticationService.getCurrentUserName();

        return UrlUtil.getAlfrescoUrl(sysAdminParams) + "/s/parashift/onlyoffice/callback?nodeRef=" + nodeRef.toString() + "&user=" + username + "&usertoken=" + getToken(username);
    }

    public String getToken(String username) {

        try {
            SecretKeySpec signingKey = new SecretKeySpec(token, "HmacSHA256");

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            mac.update(username.getBytes());
            byte[] bytes = mac.doFinal();

            return bytesToHex(bytes);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {

            return "NoTokenGenerated";

        }

    }

    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public String getOnlyOfficeTransformUrl() {

        return transformUrl;

    }

    public String getOnlyOfficeUrl() {

        return onlyOfficeUrl;

    }

}
