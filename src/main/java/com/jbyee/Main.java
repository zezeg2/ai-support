package com.jbyee;

import com.jbyee.ai.AISupporter;
import com.jbyee.ai.ArgumentRecord;
import com.jbyee.ai.ArgumentsFactory;
import com.jbyee.resolver.JAVAConstructResolver;
import com.theokanning.openai.service.OpenAiService;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        String functionName = "generateDocument";
        List<ArgumentRecord> records = ArgumentsFactory
                .builder()
                .addArgument("pros", 4, Integer.class)
                .addArgument("cosn", 4, Integer.class)
                .build();
        String description = "generate Document about Python, list pros and cons in given number";
        String model = "gpt-3.5-turbo";
        AISupporter aiSupporter = new AISupporter(new OpenAiService("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"), new JAVAConstructResolver());
        String result = aiSupporter.<String>aiFunction(functionName, String.class, records, description, model);
        System.out.println(result);

    }

}