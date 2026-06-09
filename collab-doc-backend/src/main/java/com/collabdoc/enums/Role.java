package com.collabdoc.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

public enum Role implements IEnum<String> {
    OWNER("OWNER"),
    ADMIN("ADMIN"),
    MEMBER("MEMBER");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
