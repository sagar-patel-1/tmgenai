package com.genai.tmgenai.controller;

import com.genai.tmgenai.dto.FileServiceResponse;
import com.genai.tmgenai.service.ChatDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URISyntaxException;

@RestController
public class ChatWithDocumentController {

    private final ChatDocumentService chatDocumentService;

    @Autowired
    public ChatWithDocumentController(ChatDocumentService chatDocumentService) {
        this.chatDocumentService = chatDocumentService;
    }

    @PostMapping("/uploadPolicyDocument")
    public ResponseEntity<Object> uploadPolicyDocument( @RequestParam("file") MultipartFile file) throws URISyntaxException, IOException {
      FileServiceResponse fileServiceResponse = chatDocumentService.uploadPolicyDocument(file);
      if(fileServiceResponse == null){
        return ResponseEntity.badRequest().body("File upload and review failed");
      }
      return ResponseEntity.ok(fileServiceResponse);
    }


}
