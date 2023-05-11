package com.jbyee;

import com.jbyee.ai.AISupporter;
import com.theokanning.openai.service.OpenAiService;

public class Main {
    public static void main(String[] args) {
        String function = "function exampleFunction(a: number, b: number): number {\n  return a + b;\n}";
        Object[] arguments = {Integer.valueOf(1),Integer.valueOf(2)};
        String description = "Adds two numbers together";
        String model = "gpt-3.5-turbo";
        AISupporter aiSupporter = new AISupporter(new OpenAiService("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"));
        Integer result = aiSupporter.<Integer>aiFunction(function, Integer.class, arguments, description, model);
        System.out.println(result);
    }
}