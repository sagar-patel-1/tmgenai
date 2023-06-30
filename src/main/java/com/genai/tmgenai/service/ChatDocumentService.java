package com.genai.tmgenai.service;

import com.genai.tmgenai.dto.FileServiceResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;

public interface ChatDocumentService {
    public FileServiceResponse uploadPolicyDocument(MultipartFile file) throws URISyntaxException, IOException;
}
