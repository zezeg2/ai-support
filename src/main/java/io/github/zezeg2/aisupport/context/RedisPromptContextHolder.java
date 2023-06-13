package io.github.zezeg2.aisupport.context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatMessage;
import io.github.zezeg2.aisupport.core.function.prompt.Prompt;
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
    public boolean contains(String namespace) {
        return hashOperations.hasKey(namespace, "prompt");
    }

    @Override
    public void savePrompt(String namespace, Prompt prompt) {
        try {
            String promptJson = mapper.writeValueAsString(prompt);
            hashOperations.put(namespace, "prompt", promptJson);
        } catch (Exception e) {
            log.info("Exception Occurred \n- name : {}\n- message: {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    @Override
    public Prompt get(String namespace) {
        try {
            String promptJson = hashOperations.get(namespace, "prompt");
            return mapper.readValue(promptJson, Prompt.class);
        } catch (Exception e) {
            log.info("Exception Occurred \n- name : {}\n- message: {}", e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public Map<String, List<ChatMessage>> getPromptMessagesContext(String namespace) {
        try {
            String contextJson = hashOperations.get(namespace, "promptMessagesContext");
            return mapper.readValue(contextJson, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.info("Exception Occurred \n- name : {}\n- message: {}", e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public Map<String, List<ChatMessage>> getFeedbackMessagesContext(String namespace) {
        try {
            String contextJson = hashOperations.get(namespace, "feedbackMessagesContext");
            return mapper.readValue(contextJson, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.info("Exception Occurred \n- name : {}\n- message: {}", e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public List<ChatMessage> getPromptChatMessages(String namespace, String identifier) {
        try {
            String messagesJson = hashOperations.get(namespace + ":" + identifier, "promptChatMessages");
            return mapper.readValue(messagesJson, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.info("Exception Occurred \n- name : {}\n- message: {}", e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public List<ChatMessage> getFeedbackChatMessages(String namespace, String identifier) {
        try {
            String messagesJson = hashOperations.get(namespace + ":" + identifier, "feedbackChatMessages");
            return mapper.readValue(messagesJson, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.info("Exception Occurred \n- name : {}\n- message: {}", e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public void savePromptMessagesContext(String namespace, String identifier, ChatMessage message) {
        try {
            String messagesJson = hashOperations.get(namespace + ":" + identifier, "promptChatMessages");
            List<ChatMessage> messages;
            if (messagesJson != null) {
                messages = this.mapper.readValue(messagesJson, new TypeReference<>() {
                });
            } else messages = new ArrayList<>();
            messages.add(message);
            hashOperations.put(namespace + ":" + identifier, "promptChatMessages", mapper.writeValueAsString(messages));
        } catch (Exception e) {
            log.info("Exception Occurred \n- name : {}\n- message: {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    @Override
    public void saveFeedbackMessagesContext(String namespace, String identifier, ChatMessage message) {
        try {
            String messagesJson = hashOperations.get(namespace + ":" + identifier, "feedbackChatMessages");
            List<ChatMessage> messages;
            if (messagesJson != null) {
                messages = this.mapper.readValue(messagesJson, new TypeReference<>() {
                });
            } else messages = new ArrayList<>();
            messages.add(message);
            hashOperations.put(namespace + ":" + identifier, "feedbackChatMessages", mapper.writeValueAsString(messages));
        } catch (Exception e) {
            log.info("Exception Occurred \n- name : {}\n- message: {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }
}