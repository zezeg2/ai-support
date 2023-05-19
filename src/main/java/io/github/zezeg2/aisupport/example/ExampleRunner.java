package io.github.zezeg2.aisupport.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.AISupporter;
import io.github.zezeg2.aisupport.ai.function.AIFunction;
import io.github.zezeg2.aisupport.ai.function.argument.ArgumentsFactory;
import io.github.zezeg2.aisupport.ai.function.constraint.ConstraintsFactory;
import io.github.zezeg2.aisupport.ai.model.gpt.GPT3Model;
import io.github.zezeg2.aisupport.resolver.JAVAConstructResolver;

import java.time.Duration;
import java.util.List;

public class ExampleRunner {
    public static void main(String[] args) throws Exception {
        Question question = new Question();
        question.setContent("""
                \\begin{align}&0.8x^{2}-1.4x+0.2=0\\\\&
                \\rightarrow x=\\boxed{}\\end{align}
                """);
        question.setSolving("""
                \\begin{align} &0.8x^{2}-1.4x+0.2=0에서\\\\&
                양변에~10을~곱하면\\\\&
                8x^{2}-14x+2=0\\\\&
                4x^{2}-7x+1=0\\\\& \\phantom{0}\s
                \\\\&근의~공식에\\\\&
                a=4,~b=-7,~c=1을~대입하면\\\\&
                x=\\frac{-\\left(-7\\right)\\pm\\sqrt{\\left(-7\\right)^{2}-4\\times4\\times1}}{2\\times4}\\\\&
                ~~=\\frac{7\\pm\\sqrt{33}}{8}  \\end{align}
                """);
        question.setAnswer(List.of("""
                \\frac{7\\pm\\sqrt{33}}{8}
                """));

        AISupporter aiSupporter = new AISupporter(
                new OpenAiService("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", Duration.ofSeconds(60)),
                new ObjectMapper(),
                new JAVAConstructResolver());

        AIFunction<AnalysisPromptResponse> analysisFunction = aiSupporter.createSingleFunction(
                "analyzeMathQuestion",
                "analyze the concrete mathematical knowledge required to solve given mathematics question and return them",
                AnalysisPromptResponse.class,
                ConstraintsFactory
                        .builder()
                        .addConstraint("think as a mathematical item review specialist")
                        .addConstraint("at least 3 required knowledge")
                        .addConstraint("each description within 15 words")
                        .addConstraint("language", "english")
                        .build()
        );

        AnalysisPromptResponse res1 = analysisFunction.execute(ArgumentsFactory
                .builder()
                .addArgument(Question.class, "question", question, "given Question")
                .build(), GPT3Model.GPT_3_5_TURBO
        );

        AIFunction<GeneratePromptResponse> generateQuestionFunction = aiSupporter.createSingleFunction(
                "generateQuestions",
                "generate new mathematics question base on given resource.requiredKnowledgeList",
                GeneratePromptResponse.class,
                ConstraintsFactory
                        .builder()
                        .addConstraint("think as a Professional of Mathematics Education")
                        .addConstraint("use laTex expression to express formula and enclose them $expression$, example : $2x^2 + 3x - 5 = 0$")
                        .addConstraint("If there are multiple answers list them in answer List")
                        .addConstraint("type of question", "subjective")
                        .addConstraint("difficulty of question", "advanced")
                        .addConstraint("number of question", "1")
                        .addConstraint("language", "english")
                        .build()
        );

        GeneratePromptResponse res2 = generateQuestionFunction.execute(ArgumentsFactory
                .builder()
                .addArgument(RequiredKnowledge.class, "resource", res1.requiredKnowledgeList, "base knowledge to solve new mathematics question")
                .build(), GPT3Model.GPT_3_5_TURBO
        );

        AIFunction<ValidationResponse> fixLaTexExpressionFunction = aiSupporter.createSingleFunction("validateAndFix",
                "Correct the given LaTex expression if it is not valid",
                ValidationResponse.class,
                ConstraintsFactory.builder()
                        .addConstraint("", "[Result] will be parsing by ObjectMapper")
                        .addConstraint("", "Please consider escape characters")
                        .build()
        );

        ValidationResponse res3 = fixLaTexExpressionFunction.execute(ArgumentsFactory.builder()
                .addArgument(Question.class, "validationTarget", res2.questionList.get(0), "LaTex expression before fix")
                .build(), GPT3Model.GPT_3_5_TURBO
        );

//        AIFunction<List<String>> fixLaTexExpressionFunction = aiSupporter.createListFunction("fixLaTexExpression",
//                "Correct the given LaTex expression if it is not valid",
//                String.class,
//                ConstraintsFactory.builder()
//                        .addConstraint("", "[Result] will be parsing by ObjectMapper")
//                        .addConstraint("", "Please consider escape characters")
//                        .build()
//        );
//        List<String> fixed = fixLaTexExpressionFunction.execute(ArgumentsFactory.builder()
//                .addArgument(List.class, "abc", Map.of("key1", List.of("a", "b", "c"), "key2", List.of("d", "e", "f")), "LaTex expression before fix")
//                .addArgument(String.class, "def", List.of("d", "e", "f"), "LaTex expression before fix")
//                .build(), GPT3Model.GPT_3_5_TURBO
//        );
//
//        System.out.println(fixed);

//        AIFunction<String> summarizeFunction = aiSupporter.createFunction("summarizeFunction",
//                "summarize given paragraph",
//                WRAPPING.NONE,
//                String.class,
//                ConstraintsFactory.builder()
//                        .addConstraint("", "Keep the LaTex expression(enclosed with $)")
//                        .build()
//        );
//
//        String summarized =summarizeFunction.execute(ArgumentsFactory.builder()
//                .addArgument(WRAPPING.NONE, String.class, "paragraph", fixed, "input paragraph")
//                .build(), GPT3Model.GPT_3_5_TURBO);
//        System.out.println(summarized);

        System.out.println("\nFinish");


    }
}