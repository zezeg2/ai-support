package io.github.zezeg2.aisupport.ai.function.prompt.refactor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class RedisPromptContextHolder implements PromptContextHolder {

    private final HashOperations<String, String, String> hashOperations;
    private final ObjectMapper mapper;

    public RedisPromptContextHolder(RedisTemplate<String, String> template, ObjectMapper mapper) {
        this.hashOperations = template.opsForHash();
        this.mapper = mapper;
    }

    @Override
    public boolean contains(String functionName) {
        return hashOperations.hasKey(functionName, "prompt");
    }

    @Override
    public void savePrompt(String functionName, Prompt prompt) {
        try {
            String promptJson = mapper.writeValueAsString(prompt);
            hashOperations.put(functionName, "prompt", promptJson);
        } catch (Exception e) {
            log.info("Exception Occurred \n- name : {}\n-message: {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    @Override
    public Prompt get(String functionName) {
        try {
            String promptJson = hashOperations.get(functionName, "prompt");
            return mapper.readValue(promptJson, Prompt.class);
        } catch (Exception e) {
            log.info("Exception Occurred \n- name : {}\n-message: {}", e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public Map<String, List<ChatMessage>> getPromptMessagesContext(String functionName) {
        try {
            String contextJson = hashOperations.get(functionName, "promptMessagesContext");
            return mapper.readValue(contextJson, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.info("Exception Occurred \n- name : {}\n-message: {}", e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public Map<String, List<ChatMessage>> getFeedbackMessagesContext(String validatorName) {
        try {
            String contextJson = hashOperations.get(validatorName, "feedbackMessagesContext");
            return mapper.readValue(contextJson, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.info("Exception Occurred \n- name : {}\n-message: {}", e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public List<ChatMessage> getPromptChatMessages(String functionName, String identifier) {
        try {
            String messagesJson = hashOperations.get(functionName + ":" + identifier, "promptChatMessages");
            return mapper.readValue(messagesJson, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.info("Exception Occurred \n- name : {}\n-message: {}", e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public List<ChatMessage> getFeedbackChatMessages(String validatorName, String identifier) {
        try {
            String messagesJson = hashOperations.get(validatorName + ":" + identifier, "feedbackChatMessages");
            return mapper.readValue(messagesJson, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.info("Exception Occurred \n- name : {}\n-message: {}", e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public void savePromptMessagesContext(String functionName, String identifier, ChatMessage message) {
        try {
            String messagesJson = hashOperations.get(functionName + ":" + identifier, "promptChatMessages");
            List<ChatMessage> messages;
            if (messagesJson != null) {
                messages = this.mapper.readValue(messagesJson, new TypeReference<>() {
                });
            } else messages = new ArrayList<>();
            messages.add(message);
            hashOperations.put(functionName + ":" + identifier, "promptChatMessages", mapper.writeValueAsString(messages));
        } catch (Exception e) {
            log.info("Exception Occurred \n- name : {}\n-message: {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    @Override
    public void saveFeedbackMessagesContext(String validatorName, String identifier, ChatMessage message) {
        try {
            String messagesJson = hashOperations.get(validatorName + ":" + identifier, "feedbackChatMessages");
            List<ChatMessage> messages;
            if (messagesJson != null) {
                messages = this.mapper.readValue(messagesJson, new TypeReference<>() {
                });
            } else messages = new ArrayList<>();
            messages.add(message);
            hashOperations.put(validatorName + ":" + identifier, "feedbackChatMessages", mapper.writeValueAsString(messages));
        } catch (Exception e) {
            log.info("Exception Occurred \n- name : {}\n-message: {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }
}