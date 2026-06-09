package com.collabdoc.dto.ai;

import lombok.Data;

@Data
public class AiRequest {

    private String action;
    private String text;
    private String targetLang;
}
