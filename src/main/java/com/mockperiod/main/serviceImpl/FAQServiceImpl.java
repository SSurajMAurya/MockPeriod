package com.mockperiod.main.serviceImpl;



import com.mockperiod.main.dto.FAQDTO;
import com.mockperiod.main.entities.FAQ;
import com.mockperiod.main.repository.FAQRepository;
import com.mockperiod.main.service.FAQService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FAQServiceImpl implements FAQService {

    private final FAQRepository faqRepository;

    @Override
    public FAQDTO createFAQ(FAQDTO faqDTO) {
        log.info("Creating new FAQ with question: {}", faqDTO.getQuestion());
        
        FAQ faq = FAQ.builder()
                .question(faqDTO.getQuestion())
                .Answer(faqDTO.getAnswer())
                .description(faqDTO.getDescription())
                .build();
        
        FAQ savedFAQ = faqRepository.save(faq);
        log.info("FAQ created with ID: {}", savedFAQ.getId());
        
        return mapToDTO(savedFAQ);
    }

    @Override
    public FAQDTO getFAQById(Long id) {
        log.info("Fetching FAQ with ID: {}", id);
        
        FAQ faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ not found with id: " + id));
        
        return mapToDTO(faq);
    }

    @Override
    public List<FAQDTO> getAllFAQs() {
        log.info("Fetching all FAQs");
        
        List<FAQ> faqs = faqRepository.findAll();
        
        return faqs.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FAQDTO updateFAQ(Long id, FAQDTO faqDTO) {
        log.info("Updating FAQ with ID: {}", id);
        
        FAQ existingFAQ = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ not found with id: " + id));
        
        existingFAQ.setQuestion(faqDTO.getQuestion());
        existingFAQ.setAnswer(faqDTO.getAnswer());
        existingFAQ.setDescription(faqDTO.getDescription());
        
        FAQ updatedFAQ = faqRepository.save(existingFAQ);
        log.info("FAQ updated with ID: {}", updatedFAQ.getId());
        
        return mapToDTO(updatedFAQ);
    }

    @Override
    public void deleteFAQ(Long id) {
        log.info("Deleting FAQ with ID: {}", id);
        
        if (!faqRepository.existsById(id)) {
            throw new RuntimeException("FAQ not found with id: " + id);
        }
        
        faqRepository.deleteById(id);
        log.info("FAQ deleted with ID: {}", id);
    }

    private FAQDTO mapToDTO(FAQ faq) {
        return FAQDTO.builder()
                .id(faq.getId())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .description(faq.getDescription())
                .build();
    }
}
