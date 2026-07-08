package com.training.system.topic.vo;

public class TopicAiSuggestionVO {

    private String suggestion;

    public TopicAiSuggestionVO() {
    }

    public TopicAiSuggestionVO(String suggestion) {
        this.suggestion = suggestion;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }
}
