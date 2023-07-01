package com.genai.tmgenai.dto;

import com.genai.tmgenai.dto.enums.DocTypeEnum;
import lombok.Data;

@Data
public class Question {
    private String question;
    private DocTypeEnum docType;
    private String fileId;
}
