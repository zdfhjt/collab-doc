package com.collabdoc.dto.document;

import com.collabdoc.enums.DocumentType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.IOException;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    private DocumentType docType;

    @JsonDeserialize(using = SafeLongDeserializer.class)
    private Long parentId;

    private String icon;

    private Integer sortOrder;

    public DocumentType getDocType() {
        return docType != null ? docType : DocumentType.DOCUMENT;
    }

    static class SafeLongDeserializer extends JsonDeserializer<Long> {
        @Override
        public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            if (node.isNull() || node.isObject()) {
                return null;
            }
            if (node.isNumber()) {
                return node.longValue();
            }
            if (node.isTextual()) {
                try {
                    return Long.parseLong(node.asText());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        }
    }
}
