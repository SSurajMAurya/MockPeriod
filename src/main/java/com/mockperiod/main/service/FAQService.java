package com.mockperiod.main.service;


import com.mockperiod.main.dto.FAQDTO;

import java.util.List;

public interface FAQService {
    
    FAQDTO createFAQ(FAQDTO faqDTO);
    
    FAQDTO getFAQById(Long id);
    
    List<FAQDTO> getAllFAQs();
    
    FAQDTO updateFAQ(Long id, FAQDTO faqDTO);
    
    void deleteFAQ(Long id);
}
