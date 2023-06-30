package com.genai.tmgenai.service;

import com.genai.tmgenai.dto.FileServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Service
public class ChatDocumentServiceImpl implements ChatDocumentService{

    private final RestService restService;
    private final FileEmbeddingService fileEmbeddingService;

    @Autowired
    public ChatDocumentServiceImpl(RestService restService, FileEmbeddingService fileEmbeddingService) {
        this.restService = restService;
        this.fileEmbeddingService = fileEmbeddingService;
    }
    @Override
    public FileServiceResponse uploadPolicyDocument(MultipartFile file) throws URISyntaxException, IOException {
        FileServiceResponse fileServiceResponse = uploadFile(file);
        String fileId = fileServiceResponse.getFileResponseMeta().getFileId();
        if(fileId != null){
            fileEmbeddingService.embedFile(file, fileId);
            return fileServiceResponse;
        }
        return null;
    }

    private FileServiceResponse uploadFile(MultipartFile file) throws URISyntaxException {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());
        body.add("path", "/renewals/bulk-upload/" + file.getOriginalFilename());
        body.add("cloudSource", "AWS_S3");
        body.add("broker", "turtlemint");
        body.add("bucket", "policyrenewal-stage");
        body.add("tag", "Document");

        HttpEntity<MultiValueMap<String, Object>> requestEntity= new HttpEntity<>(body, headers);

        return restService.postForEntity(
                new URI( "https://ninja.twilight.turtle-feature.com" + "/api/files/v1/upload"),
                requestEntity,
                FileServiceResponse.class
        ).getBody();
    }

}

