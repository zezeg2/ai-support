package com.jbyee;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jbyee.ai.AISupporter;
import com.jbyee.ai.ArgumentRecord;
import com.jbyee.ai.ArgumentsFactory;
import com.jbyee.resolver.JAVAConstructResolver;
import com.theokanning.openai.service.OpenAiService;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws JsonProcessingException {
        String functionName = "generateProsAndConsMap";
        List<ArgumentRecord> records = ArgumentsFactory
                .builder()
                .addArgument("pros", 4, Integer.class)
                .addArgument("cons", 4, Integer.class)
                .addArgument("docType", "markDown", String.class)
                .build();
        String description = "this function generate 'pros' and 'cons' in given number about Python.  and store them in Map. keys are 'pros' , 'cons'value is list";
        String model = "gpt-3.5-turbo";
        AISupporter aiSupporter = new AISupporter(new OpenAiService("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"), new JAVAConstructResolver());
        Map<String, Object> result = aiSupporter.aiFunction(functionName, records, description, model);
        System.out.println(result);

    }

}