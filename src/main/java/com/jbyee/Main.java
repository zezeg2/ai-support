package com.jbyee;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jbyee.ai.AISupporter;
import com.jbyee.ai.function.Argument;
import com.jbyee.ai.function.ArgumentsFactory;
import com.jbyee.ai.function.Constraint;
import com.jbyee.ai.function.ConstraintsFactory;
import com.jbyee.ai.model.gpt.GPT3Model;
import com.jbyee.resolver.JAVAConstructResolver;
import com.theokanning.openai.service.OpenAiService;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws JsonProcessingException {
        String functionName = "generateProsAndConsMap";
        List<Argument> records = ArgumentsFactory
                .builder()
                .addArgument("pros", 4, Integer.class)
                .addArgument("cons", 4, Integer.class)
                .addArgument("docType", "markDown", String.class)
                .build();

        List<Constraint> constraintList = ConstraintsFactory
                .builder()
                .addArgument("", "this function generate 'pros' and 'cons' in given number about Python.  and store them in Map. keys are 'pros' , 'cons'value is list")
                .build();

        String description = "this function generate 'pros' and 'cons' in given number about Python.  and store them in Map. keys are 'pros' , 'cons'value is list";

        AISupporter aiSupporter = new AISupporter(new OpenAiService(""), new ObjectMapper(), new JAVAConstructResolver());
        Map<String, Object> result = aiSupporter.aiFunction(functionName, records, constraintList, description, GPT3Model.GPT_3_5_TURBO);
        System.out.println(result);
    }

}