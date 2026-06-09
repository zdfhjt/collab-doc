package com.collabdoc.controller;

import com.collabdoc.dto.ai.AiRequest;
import com.collabdoc.dto.ai.ChatEventVO;
import com.collabdoc.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<ChatEventVO>> stream(@RequestBody AiRequest request) {
        Flux<ChatEventVO> flux = switch (request.getAction()) {
            case "expand" -> aiService.expand(request.getText());
            case "polish" -> aiService.polish(request.getText());
            case "summarize" -> aiService.summarize(request.getText());
            case "translate" -> aiService.translate(request.getText(), request.getTargetLang());
            default -> aiService.chat(request.getText());
        };

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(flux);
    }
}
