package com.parashift.onlyoffice;

import org.alfresco.service.namespace.QName;

/**
 * Created by cetra on 9/09/2016.
 */
public interface OnlyOfficeModel {

    // ad:model model namespaces
    static final String OO_1_0_URI = "http://www.parashift.com.au/model/doc/onlyoffice/1.0";

    // oo:currentlyEditing
    static final QName ASPECT_OO_CURRENTLY_EDITING = QName.createQName(OO_1_0_URI, "currentlyEditing");
}

