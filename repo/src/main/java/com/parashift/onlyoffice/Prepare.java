package com.parashift.onlyoffice;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.QName;
import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.util.Map;
import java.util.Properties;

/**
 * Created by cetra on 20/10/15.
 * Sends Alfresco Share the necessaries to build up what information is needed for the OnlyOffice server
 */
@Component(value = "webscript.onlyoffice.prepare.get")
public class Prepare extends AbstractWebScript {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    OnlyOfficeService onlyOfficeService;

    @Autowired
    NodeService nodeService;

    @Autowired
    ContentService contentService;

    @Resource(name = "global-properties")
    Properties globalProp;

    @Override
    public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
        if (request.getParameter("nodeRef") != null) {

            NodeRef nodeRef = new NodeRef(request.getParameter("nodeRef"));

            JSONObject responseJson = new JSONObject();

            response.setContentType("application/json; charset=utf-8");
            response.setContentEncoding("UTF-8");

            if(nodeService.exists(nodeRef)) {
                Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

                if(properties.containsKey(ContentModel.PROP_CONTENT)) {

                    String contentUrl = onlyOfficeService.getContentUrl(nodeRef);
                    String key =  onlyOfficeService.getKey(nodeRef);
                    String callbackUrl = onlyOfficeService.getCallbackUrl(nodeRef);

                    ContentData contentData = (ContentData) properties.get(ContentModel.PROP_CONTENT);

                    responseJson.put("docUrl", contentUrl);
                    responseJson.put("callbackUrl", callbackUrl);
                    responseJson.put("onlyofficeUrl", onlyOfficeService.getOnlyOfficeUrl());
                    responseJson.put("key", key);
                    responseJson.put("docTitle", properties.get(ContentModel.PROP_NAME));
                    responseJson.put("mimeType", contentData.getMimetype());
                    responseJson.put("abovePreviewThreshold", checkAbovePreviewThreshold(nodeRef, contentData));

                    if(globalProp.containsKey("onlyoffice.lang")) {
                        responseJson.put("lang", globalProp.get("onlyoffice.lang"));
                    }

                    responseJson.put("status", "OK");
                } else {
                    responseJson.put("status", "Not A File");
                }

            } else {
                responseJson.put("status", "Node Not Found");
            }

            logger.debug("Sending JSON prepare object");
            logger.debug(responseJson.toString(3));

            try(Writer responseWriter = response.getWriter()) {
                responseJson.write(responseWriter);
            }

        }
    }

    private boolean checkAbovePreviewThreshold(NodeRef nodeRef, ContentData contentData) {
        boolean result = false;
        Long documentMaxSize = Long.parseLong((String) globalProp.getOrDefault("onlyoffice.preview.document.size.threshold", "0"));
        Long documentSize = contentData.getSize();

        if (documentMaxSize != 0 && documentSize > documentMaxSize) {
            logger.debug("Document size {} exceeds threshold {}.", documentSize, documentMaxSize);
            result = true;
        }

        if (!result) {
            String mimeType = contentData.getMimetype();

            if (mimeType != null) {

                Integer docxMaxParagraph = Integer.parseInt((String) globalProp.getOrDefault("onlyoffice.preview.docx.threshold", "0"));
                Integer docMaxPage = Integer.parseInt((String) globalProp.getOrDefault("onlyoffice.preview.doc.threshold", "0"));
                Integer xlsxMaxRows = Integer.parseInt((String) globalProp.getOrDefault("onlyoffice.preview.xlsx.threshold", "0"));
                Integer xlsMaxRows = Integer.parseInt((String) globalProp.getOrDefault("onlyoffice.preview.xls.threshold", "0"));
                Integer pptxMaxSlides = Integer.parseInt((String) globalProp.getOrDefault("onlyoffice.preview.pptx.threshold", "0"));
                Integer pptMaxSlides = Integer.parseInt((String) globalProp.getOrDefault("onlyoffice.preview.ppt.threshold", "0"));

                logger.debug("Thresholds: docx: {}, doc: {}, xlsx: {}, xls: {}, pptx: {}, ppt: {}", docxMaxParagraph, docMaxPage, xlsxMaxRows, xlsMaxRows, pptxMaxSlides, pptMaxSlides);

                ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                try {
                    switch (mimeType) {
                        case MimetypeMap.MIMETYPE_OPENXML_WORD_TEMPLATE:
                        case MimetypeMap.MIMETYPE_OPENXML_WORD_TEMPLATE_MACRO:
                        case MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING_MACRO:
                        case MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING:
                            if (docxMaxParagraph > 0) {
                                try (InputStream inputStream = contentReader.getContentInputStream()) {
                                    XWPFDocument docx = new XWPFDocument(inputStream);
                                    int paragraphNum = docx.getBodyElements().size();
                                    logger.debug("DOCX paragraph number is: {}", paragraphNum);
                                    result = paragraphNum > docxMaxParagraph;
                                }
                            }
                            break;
                        case MimetypeMap.MIMETYPE_WORD:
                            if (docMaxPage > 0) {
                                try (InputStream inputStream = contentReader.getContentInputStream()) {
                                    HWPFDocument doc = new HWPFDocument(inputStream);
                                    int pageCount = doc.getSummaryInformation().getPageCount();
                                    logger.debug("DOC page count is: {}", pageCount);
                                    result = pageCount > docMaxPage;
                                }
                            }
                            break;
                        case MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET_TEMPLATE:
                        case MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET_TEMPLATE_MACRO:
                        case MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET_MACRO:
                        case MimetypeMap.MIMETYPE_OPENXML_SPREADSHEET:
                            if (xlsxMaxRows > 0) {
                                try (InputStream inputStream = contentReader.getContentInputStream()) {
                                    XSSFWorkbook xlsx = new XSSFWorkbook(inputStream);
                                    Integer totalRows = 0;
                                    for (int i = 0; i < xlsx.getNumberOfSheets(); i++) {
                                        XSSFSheet sheet = xlsx.getSheetAt(i);
                                        totalRows += sheet.getPhysicalNumberOfRows();
                                        logger.debug("XLSX totalRows: {}", totalRows);
                                        if (result = totalRows > xlsxMaxRows) {
                                            break;
                                        }
                                    }
                                }
                            }
                            break;
                        case MimetypeMap.MIMETYPE_EXCEL:
                            if (xlsMaxRows > 0) {
                                try (InputStream inputStream = contentReader.getContentInputStream()) {
                                    HSSFWorkbook xls = new HSSFWorkbook(inputStream);
                                    Integer xlsTotalRows = 0;
                                    for (int i = 0; i < xls.getNumberOfSheets(); i++) {
                                        HSSFSheet sheet = xls.getSheetAt(i);
                                        xlsTotalRows += sheet.getPhysicalNumberOfRows();
                                        logger.debug("XLS totalRows: {}", xlsTotalRows);
                                        if (result = xlsTotalRows > xlsMaxRows) {
                                            break;
                                        }
                                    }
                                }
                            }
                            break;
                        case MimetypeMap.MIMETYPE_OPENXML_PRESENTATION_TEMPLATE:
                        case MimetypeMap.MIMETYPE_OPENXML_PRESENTATION_TEMPLATE_MACRO:
                        case MimetypeMap.MIMETYPE_OPENXML_PRESENTATION_SLIDESHOW:
                        case MimetypeMap.MIMETYPE_OPENXML_PRESENTATION_SLIDESHOW_MACRO:
                        case MimetypeMap.MIMETYPE_OPENXML_PRESENTATION_MACRO:
                        case MimetypeMap.MIMETYPE_OPENXML_PRESENTATION:
                            if (pptxMaxSlides > 0) {
                                try (InputStream inputStream = contentReader.getContentInputStream()) {
                                    XMLSlideShow pptx = new XMLSlideShow(inputStream);
                                    int slidesNum = pptx.getSlides().length;
                                    logger.debug("PPTX slides number is: {}", slidesNum);
                                    result = slidesNum > pptxMaxSlides;
                                }
                            }
                            break;
                        case MimetypeMap.MIMETYPE_PPT:
                            if (pptMaxSlides > 0) {
                                try (InputStream inputStream = contentReader.getContentInputStream()) {
                                    HSLFSlideShow ppt = new HSLFSlideShow(inputStream);
                                    int slidesCount = ppt.getDocumentSummaryInformation().getSlideCount();
                                    logger.debug("PPT slides count is: {}", slidesCount);
                                    result = slidesCount > pptMaxSlides;
                                }
                            }
                        default:
                            break;
                    }
                } catch (Exception e) {
                    logger.error("Get input stream of file failed, can't parse message: {}", e.getMessage());
                }
            }
        }
        return result;
    }
}

