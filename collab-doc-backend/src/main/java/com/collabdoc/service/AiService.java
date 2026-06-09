package com.collabdoc.service;

import com.collabdoc.dto.ai.ChatEventVO;
import reactor.core.publisher.Flux;

public interface AiService {

    Flux<ChatEventVO> expand(String selectedText);

    Flux<ChatEventVO> polish(String selectedText);

    Flux<ChatEventVO> summarize(String selectedText);

    Flux<ChatEventVO> translate(String selectedText, String targetLang);

    Flux<ChatEventVO> chat(String prompt);
}
