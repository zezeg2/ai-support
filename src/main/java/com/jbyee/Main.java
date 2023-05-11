package com.jbyee;

import com.jbyee.ai.AISupporter;
import com.jbyee.ai.ArgumentRecord;
import com.jbyee.ai.ArgumentsFactory;
import com.jbyee.common.FC;
import com.jbyee.resolver.JAVAConstructResolver;
import com.theokanning.openai.service.OpenAiService;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        String function = "function exampleFunction(a: number, b: number): number {\n  return a + b;\n}";
        List<ArgumentRecord> records = ArgumentsFactory.generate(
                "a", 1, Integer.class
                , "b", 3, Integer.class
                , "c", 7, Integer.class
        );
        String description = "Adds two numbers together";
        String model = "gpt-3.5-turbo";
        AISupporter aiSupporter = new AISupporter(new OpenAiService("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"), new JAVAConstructResolver());
        Integer result = aiSupporter.<Integer>aiFunction(function, Integer.class, records, description, model);
        System.out.println(result);

        String formatted = """
                @FunctionalInterface
                public interface %s {
                    %s functionName(%s);
                }
                """.formatted();

        FC fc = (a) -> {
            return a;
        };
    }

    FC fc = (a) -> {
        return a;
    };
}