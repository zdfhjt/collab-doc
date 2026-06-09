package com.collabdoc.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

public enum DocumentType implements IEnum<String> {
    FOLDER("FOLDER"),
    DOCUMENT("DOCUMENT");

    private final String value;

    DocumentType(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
