package com.collabdoc.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatEventVO {

    /** 事件类型：1001=数据, 1002=停止 */
    private int eventType;

    /** 文本内容 */
    private String eventData;

    public static ChatEventVO data(String content) {
        return new ChatEventVO(1001, content);
    }

    public static ChatEventVO stop() {
        return new ChatEventVO(1002, null);
    }
}
