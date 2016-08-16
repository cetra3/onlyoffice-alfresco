package com.parashift.onlyoffice.conversion;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by cetra on 16/08/2016.
 */

@XmlRootElement(name = "FileResult")
public class FileResult {

    @XmlElement(name = "FileUrl")
    public String fileUrl;

    @XmlElement(name = "Percent")
    public Integer percent;

    @XmlElement(name = "EndConvert")
    public boolean convertEnded;

    @XmlElement(name = "Error")
    public Integer error;

    @Override
    public String toString() {
        return "FileUrl:" + fileUrl + ", Percent:" + percent + ", EndConvert:" + convertEnded + ", Error:" + error;
    }
}
