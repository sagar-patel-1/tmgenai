package com.genai.tmgenai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FileResponseMeta {
    @JsonProperty("pid")
    private String fileId;
}