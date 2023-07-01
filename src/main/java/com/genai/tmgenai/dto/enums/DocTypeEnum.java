package com.genai.tmgenai.dto.enums;

public enum DocTypeEnum {
    INSURER_NOTICE("insurernotice"),
    BROCHURE("brochure"),

    POLICY_DOCUMENT("policydocument");

    private String docType;

    DocTypeEnum(String docType) {
        this.docType = docType;
    }

    public String getDocType() {
        return docType;
    }
}
