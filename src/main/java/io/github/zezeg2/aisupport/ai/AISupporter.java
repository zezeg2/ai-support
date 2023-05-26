package io.github.zezeg2.aisupport.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.function.AIFunction;
import io.github.zezeg2.aisupport.ai.function.AIListFunction;
import io.github.zezeg2.aisupport.ai.function.AIMapFunction;
import io.github.zezeg2.aisupport.ai.function.AISingleFunction;
import io.github.zezeg2.aisupport.ai.function.constraint.Constraint;
import io.github.zezeg2.aisupport.common.BuildFormatUtil;
import io.github.zezeg2.aisupport.common.enums.STRUCTURE;
import io.github.zezeg2.aisupport.resolver.ConstructResolver;

import java.util.List;
import java.util.Map;

public class AISupporter {
    private final OpenAiService service;
    private final ObjectMapper mapper;
    private final ConstructResolver resolver;
    private final BuildFormatUtil formatUtil;

    public AISupporter(OpenAiService service, ObjectMapper mapper, ConstructResolver resolver, BuildFormatUtil formatUtil) {
        this.service = service;
        this.mapper = mapper;
        this.resolver = resolver;
        this.formatUtil = formatUtil;
    }

    public <T> AIFunction<?> createFunction(STRUCTURE structure, String functionName, String purpose, Class<T> returnType, List<Constraint> constraintList) {
        return switch (structure) {
            case SINGLE ->
                    new AISingleFunction<>(functionName, purpose, constraintList, returnType, service, mapper, resolver, formatUtil);
            case LIST ->
                    new AIListFunction<>(functionName, purpose, constraintList, (Class<List<T>>) (Class<?>) List.class, service, mapper, resolver, formatUtil, returnType);
            case MAP ->
                    new AIMapFunction<>(functionName, purpose, constraintList, (Class<Map<String, T>>) (Class<?>) Map.class, service, mapper, resolver, formatUtil, returnType);
        };
    }

    public <T> AIFunction<T> createSingleFunction(String functionName, String purpose, Class<T> returnType, List<Constraint> constraintList) {
        return new AISingleFunction<>(functionName, purpose, constraintList, returnType, service, mapper, resolver, formatUtil);
    }

    public <T> AIFunction<List<T>> createListFunction(String functionName, String purpose, Class<T> returnType, List<Constraint> constraintList) {
        return new AIListFunction<>(functionName, purpose, constraintList, (Class<List<T>>) (Class<?>) List.class, service, mapper, resolver, formatUtil, returnType);
    }

    public <T> AIFunction<Map<String, T>> createMapFunction(String functionName, String purpose, Class<T> returnType, List<Constraint> constraintList) {
        return new AIMapFunction<>(functionName, purpose, constraintList, (Class<Map<String, T>>) (Class<?>) Map.class, service, mapper, resolver, formatUtil, returnType);
    }
}
