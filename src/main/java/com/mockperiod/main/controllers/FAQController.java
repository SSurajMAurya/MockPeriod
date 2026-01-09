package com.mockperiod.main.controllers;



import com.mockperiod.main.dto.FAQDTO;
import com.mockperiod.main.service.FAQService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faqs")
@RequiredArgsConstructor
public class FAQController {

    private final FAQService faqService;

    @PostMapping
    public ResponseEntity<FAQDTO> createFAQ(@Valid @RequestBody FAQDTO faqDTO) {
        FAQDTO createdFAQ = faqService.createFAQ(faqDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFAQ);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FAQDTO> getFAQById(@PathVariable Long id) {
        FAQDTO faqDTO = faqService.getFAQById(id);
        return ResponseEntity.ok(faqDTO);
    }

    @GetMapping
    public ResponseEntity<List<FAQDTO>> getAllFAQs() {
        List<FAQDTO> faqs = faqService.getAllFAQs();
        return ResponseEntity.ok(faqs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FAQDTO> updateFAQ(@PathVariable Long id, 
                                           @Valid @RequestBody FAQDTO faqDTO) {
        FAQDTO updatedFAQ = faqService.updateFAQ(id, faqDTO);
        return ResponseEntity.ok(updatedFAQ);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFAQ(@PathVariable Long id) {
        faqService.deleteFAQ(id);
        return ResponseEntity.noContent().build();
    }
}
