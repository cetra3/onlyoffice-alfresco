package com.parashift.onlyoffice;

import com.parashift.onlyoffice.conversion.FileResult;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractContentTransformer2;
import org.alfresco.service.cmr.repository.*;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by cetra on 16/08/2016.
 */
public class Transform extends AbstractContentTransformer2 {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private OnlyOfficeService onlyOfficeService;

    private TransformGet transformGet;

    private Unmarshaller unmarshaller = null;

    private static final Set<String> SOURCE_MIMETYPES = new HashSet<String>() {{
        add("application/vnd.ms-excel");
        add("application/vnd.ms-powerpoint");
        add("application/msword");
        add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        add("application/vnd.openxmlformats-officedocument.presentationml.presentation");
        add("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }};

    private static final Set<String> TARGET_MIMETYPES = new HashSet<String>() {{
        add("application/pdf");
    }};

    @Override
    public boolean isTransformableMimetype(String sourceMimetype, String targetMimetype, TransformationOptions options) {
        return SOURCE_MIMETYPES.contains(sourceMimetype) && TARGET_MIMETYPES.contains(targetMimetype);
    }

    @Override
    protected void transformInternal(ContentReader reader, ContentWriter writer, TransformationOptions options) throws Exception {

        if(unmarshaller == null) {
            unmarshaller = JAXBContext.newInstance(FileResult.class).createUnmarshaller();
        }

        logger.debug("Received transformation request: {}", options);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpGet request = new HttpGet(getUri(reader, writer, options));

            logger.debug("Sending request to: {}", request.getURI().toString());

            String key = onlyOfficeService.getKey(options.getSourceNodeRef());

            transformGet.submitReader(key, reader);

            try(CloseableHttpResponse response = httpClient.execute(request)) {

                int status = response.getStatusLine().getStatusCode();

                if(status != HttpStatus.SC_OK) {
                    logger.error("Error converting node: {}, status: {}", options.getSourceNodeRef(), status);
                } else {

                    FileResult fileResult = (FileResult) unmarshaller.unmarshal(response.getEntity().getContent());

                    if(fileResult.fileUrl != null) {
                        writeUrlToFile(fileResult.fileUrl, writer);
                    } else {
                        logger.error("Problem with result from OnlyOffice: {}", fileResult);
                    }


                }

            }

            transformGet.deleteReader(key);

        }
    }

    private void writeUrlToFile(String fileUrl, ContentWriter writer) throws IOException {

        logger.debug("Writing: {} to writer", fileUrl);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(fileUrl);

            try(CloseableHttpResponse response = httpClient.execute(request)) {
                int status = response.getStatusLine().getStatusCode();

                if(status != HttpStatus.SC_OK) {
                    logger.error("Error writing Url: {}, status: {}", fileUrl, status);
                } else {

                    writer.putContent(response.getEntity().getContent());

                }

            }

        }


    }

    private URI getUri(ContentReader reader, ContentWriter writer, TransformationOptions options) throws URISyntaxException {

        URIBuilder builder = new URIBuilder(onlyOfficeService.getOnlyOfficeTransformUrl() + "/ConvertService.ashx");

        String key = onlyOfficeService.getKey(options.getSourceNodeRef());

        builder.setParameter("key", key);
        builder.setParameter("url", onlyOfficeService.getTransformUrl(key));
        builder.setParameter("filetype", getMimetypeService().getExtension(reader.getMimetype()));
        builder.setParameter("outputtype", getMimetypeService().getExtension(writer.getMimetype()));
        builder.setParameter("embeddedfonts", "true");
        builder.setParameter("async", "false");

        return builder.build();

    }

    public void setOnlyOfficeService(OnlyOfficeService onlyOfficeService) {
        this.onlyOfficeService = onlyOfficeService;
    }


    public void setTransformGet(TransformGet transformGet) {
        this.transformGet = transformGet;
    }
}
