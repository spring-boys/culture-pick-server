package com.ssafy.culturepick.culture.controller;

import com.ssafy.culturepick.culture.service.CultureBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("local")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dev")
public class CultureDevController {

    private final CultureBatchService cultureBatchService;

    @PostMapping("/fetch")
    public ResponseEntity<Void> fetch() {
        cultureBatchService.fetchAndSaveAll();
        return ResponseEntity.ok().build();
    }
}
