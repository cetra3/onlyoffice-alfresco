package com.parashift.onlyoffice.conversion;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by cetra on 16/08/2016.
 */

@XmlRootElement(name = "FileResult")
public class FileResult {

    @XmlElement(name = "FileUrl")
    private String fileUrl;

    @XmlElement(name = "Percent")
    private Integer percent;

    @XmlElement(name = "EndConvert")
    private boolean convertEnded;

    @XmlElement(name = "Error")
    private Integer error;

    public boolean isConvertEnded() {
        return convertEnded;
    }

    public void setConvertEnded(boolean convertEnded) {
        this.convertEnded = convertEnded;
    }

    public Integer getError() {
        return error;
    }

    public void setError(Integer error) {
        this.error = error;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Integer getPercent() {
        return percent;
    }

    public void setPercent(Integer percent) {
        this.percent = percent;
    }

    @Override
    public String toString() {
        return "FileUrl:" + fileUrl + ", Percent:" + percent + ", EndConvert:" + convertEnded + ", Error:" + error;
    }
}
