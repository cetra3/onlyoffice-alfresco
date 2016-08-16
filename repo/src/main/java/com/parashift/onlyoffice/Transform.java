package com.parashift.onlyoffice;

import com.parashift.onlyoffice.conversion.FileResult;
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

/**
 * Created by cetra on 16/08/2016.
 */
public class Transform extends AbstractContentTransformer2 {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private OnlyOfficeService onlyOfficeService;

    private MimetypeService mimetypeService;

    private Unmarshaller unmarshaller = null;

    @Override
    protected void transformInternal(ContentReader reader, ContentWriter writer, TransformationOptions options) throws Exception {

        if(unmarshaller == null) {
            unmarshaller = JAXBContext.newInstance(FileResult.class).createUnmarshaller();
        }

        logger.debug("Received transformation request:{}", options);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpGet request = new HttpGet(getUri(reader, writer, options));

            logger.debug("Sending request to:{}", request.getURI().toString());

            try(CloseableHttpResponse response = httpClient.execute(request)) {

                int status = response.getStatusLine().getStatusCode();

                if(status != HttpStatus.SC_OK) {
                    logger.error("Error converting node:{}, status:{}", options.getSourceNodeRef(), status);
                } else {

                    FileResult fileResult = (FileResult) unmarshaller.unmarshal(response.getEntity().getContent());

                    writeUrlToFile(fileResult.getFileUrl(), writer);

                }

            }

        }
    }

    private void writeUrlToFile(String fileUrl, ContentWriter writer) throws IOException {

        logger.debug("Writing:{} to writer", fileUrl);


        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(fileUrl);

            try(CloseableHttpResponse response = httpClient.execute(request)) {
                int status = response.getStatusLine().getStatusCode();

                if(status != HttpStatus.SC_OK) {
                    logger.error("Error writing Url:{}, status:{}", fileUrl, status);
                } else {

                    writer.putContent(response.getEntity().getContent());

                }

            }

        }


    }

    private URI getUri(ContentReader reader, ContentWriter writer, TransformationOptions options) throws URISyntaxException {

        URIBuilder builder = new URIBuilder(onlyOfficeService.getOnlyOfficeUrl() + "/ConvertService.ashx");

        builder.setParameter("key", onlyOfficeService.getKey(options.getSourceNodeRef()));
        builder.setParameter("url", onlyOfficeService.getContentUrl(options.getSourceNodeRef()));
        builder.setParameter("filetype", mimetypeService.getExtension(reader.getMimetype()));
        builder.setParameter("outputtype", mimetypeService.getExtension(writer.getMimetype()));
        builder.setParameter("async", "false");

        return builder.build();

    }

    public void setOnlyOfficeService(OnlyOfficeService onlyOfficeService) {
        this.onlyOfficeService = onlyOfficeService;
    }


    @Override
    public void setMimetypeService(MimetypeService mimetypeService) {
        this.mimetypeService = mimetypeService;
    }

}
