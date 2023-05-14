package io.github.zezeg2.aisupport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.zezeg2.aisupport.ai.AISupporter;
import io.github.zezeg2.aisupport.ai.function.Argument;
import io.github.zezeg2.aisupport.ai.function.ArgumentsFactory;
import io.github.zezeg2.aisupport.ai.function.Constraint;
import io.github.zezeg2.aisupport.ai.function.ConstraintsFactory;
import io.github.zezeg2.aisupport.ai.model.gpt.GPT3Model;
import io.github.zezeg2.aisupport.resolver.JAVAConstructResolver;
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
                .build();

        String description = "this function generate 'pros' and 'cons' in given number about Python.  and store them in Map. keys are 'pros' , 'cons'value is list";

        AISupporter aiSupporter = new AISupporter(new OpenAiService("ENTER THE OPENAI API KEY"), new ObjectMapper(), new JAVAConstructResolver());
        Map<String, Object> result = aiSupporter.aiFunction(functionName, records, constraintList, description, GPT3Model.GPT_3_5_TURBO);
//        String result = aiSupporter.aiFunction(functionName, String.class, records, constraintList, description, GPT3Model.GPT_3_5_TURBO);
        System.out.println(result);
    }

}