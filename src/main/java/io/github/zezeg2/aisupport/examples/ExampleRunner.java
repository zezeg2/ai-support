package io.github.zezeg2.aisupport.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.AISupporter;
import io.github.zezeg2.aisupport.ai.function.AIFunction;
import io.github.zezeg2.aisupport.ai.function.ArgumentsFactory;
import io.github.zezeg2.aisupport.ai.function.ConstraintsFactory;
import io.github.zezeg2.aisupport.ai.model.gpt.GPT3Model;
import io.github.zezeg2.aisupport.resolver.JAVAConstructResolver;

import java.time.Duration;
import java.util.List;

public class ExampleRunner {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Question question = new Question();
        question.setType("subjective");
        question.setText("Choose the linear function expression that has a line passing the following two points as a graph.");
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

        AIFunction<AnalysisPromptResponse> analysisFunction = aiSupporter.createFunction(
                "analyzeMathQuestion",
                "analyze the concrete mathematical knowledge required to solve given mathematics question and return them",
                AnalysisPromptResponse.class,
                ConstraintsFactory
                        .builder()
                        .addConstraint("", "think as a mathematical item review specialist")
                        .addConstraint("language", "english")
                        .build()
        );

        AnalysisPromptResponse res1 = analysisFunction.execute(ArgumentsFactory
                .builder()
                .addArgument("question", question, Question.class)
                .build(), GPT3Model.GPT_3_5_TURBO
        );

        AIFunction<GeneratePromptResponse> generateQuestionFunction = aiSupporter.createFunction(
                "generateQuestions",
                "generate new mathematics question base on given requiredKnowledgeList",
                GeneratePromptResponse.class,
                ConstraintsFactory
                        .builder()
                        .addConstraint("", "think as a Professional of Mathematics Education")
                        .addConstraint("language", "english")
                        .addConstraint("", "express the generated question contents(question, solving, choices and answer) entirely in a laTex expression")
                        .addConstraint("difficulty level", "advanced")
                        .addConstraint("number of question", "1")
                        .build()
        );

        GeneratePromptResponse res2 = generateQuestionFunction.execute(ArgumentsFactory
                .builder()
                .addArgument("requiredKnowledgeList", res1.getRequiredKnowledgeList(), RequiredKnowledge.class)
                .build(), GPT3Model.GPT_3_5_TURBO
        );
    }
}