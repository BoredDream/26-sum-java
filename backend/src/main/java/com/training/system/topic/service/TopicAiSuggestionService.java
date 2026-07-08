package com.training.system.topic.service;

import com.training.system.topic.dto.TopicAiSuggestionDTO;
import com.training.system.topic.vo.TopicAiSuggestionVO;

public interface TopicAiSuggestionService {
    TopicAiSuggestionVO suggest(TopicAiSuggestionDTO dto);
}
