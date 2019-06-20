package com.parashift.onlyoffice;

import org.alfresco.service.cmr.attributes.AttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Properties;

/*
    Copyright (c) Ascensio System SIA 2019. All rights reserved.
    http://www.onlyoffice.com
*/
@Service
public class ConfigManager {

    @Autowired
    AttributeService attributeService;

    @Autowired
    @Qualifier("global-properties")
    Properties globalProp;

    public void set(String key, String value) {
        attributeService.setAttribute(value == null || value.isEmpty() ? null : value, formKey(key));
    }

    public Object get(String key) {
        String formedKey = formKey(key);
        Object value = attributeService.getAttribute(formedKey);

        if (value == null) {
            value = globalProp.get(formedKey);
        }

        return value;
    }

    public Object getOrDefault(String key, Object defaultValue) {
        String formedKey = formKey(key);
        Object value = attributeService.getAttribute(formedKey);

        if (value == null) {
            value = globalProp.getOrDefault(formedKey, defaultValue);
        }

        return value;
    }

    private String formKey(String key) {
        return "onlyoffice." + key;
    }
}

