package com.collabdoc.service.impl;

import com.collabdoc.dto.ai.ChatEventVO;
import com.collabdoc.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;

@Slf4j
@Service
public class AiServiceImpl implements AiService {

    private final ChatClient chatClient;

    public AiServiceImpl(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
        ArrayList<String> strings = new ArrayList<>();

    }

    @Override
    public Flux<ChatEventVO> expand(String selectedText) {
        return streamChat("请对以下文本进行扩写，增加更多细节和内容，保持原文风格，直接输出扩写后的文本：\n\n" + selectedText);
    }

    @Override
    public Flux<ChatEventVO> polish(String selectedText) {
        return streamChat("请对以下文本进行润色，改善文字质量、流畅度和表达力，保持原意不变，直接输出润色后的文本：\n\n" + selectedText);
    }

    @Override
    public Flux<ChatEventVO> summarize(String selectedText) {
        return streamChat("请对以下文本进行摘要，提取关键信息，生成简洁的摘要：\n\n" + selectedText);
    }

    @Override
    public Flux<ChatEventVO> translate(String selectedText, String targetLang) {
        return streamChat("请将以下文本翻译为" + targetLang + "，保持原文格式和风格，直接输出翻译结果：\n\n" + selectedText);
    }

    @Override
    public Flux<ChatEventVO> chat(String prompt) {
        return streamChat(prompt);
    }

    private Flux<ChatEventVO> streamChat(String userMessage) {
        return chatClient.prompt()
                .system("你是一个专业的写作助手。请直接输出结果，不要添加额外说明。")
                .user(userMessage)
                .stream()
                .content()
                .map(chunk -> ChatEventVO.data(chunk))
                .concatWithValues(ChatEventVO.stop());
    }
}
