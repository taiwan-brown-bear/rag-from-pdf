package com.taiwan_brown_bear.rag_from_pdf.controller;

import com.taiwan_brown_bear.rag_from_pdf.service.RagFromPdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/rag-from-pdf")
public class RagFromPdfController {

    @Autowired
    private RagFromPdfService service;

    @GetMapping
    public ResponseEntity<String> evaluate(@RequestBody String question)
    {
        String answer = service.askQuestion(question);
        return ResponseEntity.ok(answer);
    }

}
