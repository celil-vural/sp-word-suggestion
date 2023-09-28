package com.spwordsuggestion.controller;

import com.spwordsuggestion.dto.RequestAddText;
import com.spwordsuggestion.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/word")
@RequiredArgsConstructor
public class WordController {
    private final WordService wordService;
    @PostMapping("/addText")
    public void addText(@RequestBody RequestAddText text){
        wordService.addText(text.getText());
    }
    @GetMapping("/getSuggestions/{text}")
    public ResponseEntity<Set<String>> getSuggestions(@PathVariable String text) throws IOException {
        Set<String> word= wordService.getSuggestions(text);
        return ResponseEntity.ok(word);
    }
}
