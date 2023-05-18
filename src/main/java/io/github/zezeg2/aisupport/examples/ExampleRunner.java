package io.github.zezeg2.aisupport.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import io.github.zezeg2.aisupport.ai.AISupporter;
import io.github.zezeg2.aisupport.ai.function.AIFunction;
import io.github.zezeg2.aisupport.ai.function.argument.ArgumentsFactory;
import io.github.zezeg2.aisupport.ai.function.constraint.ConstraintsFactory;
import io.github.zezeg2.aisupport.ai.model.gpt.GPT3Model;
import io.github.zezeg2.aisupport.common.enums.WRAPPING;
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
                WRAPPING.NONE,
                AnalysisPromptResponse.class,
                ConstraintsFactory
                        .builder()
                        .addConstraint("", "think as a mathematical item review specialist")
                        .addConstraint("language", "english")
                        .build()
        );

        AnalysisPromptResponse res1 = analysisFunction.execute(ArgumentsFactory
                .builder()
                .addArgument(WRAPPING.NONE, Question.class, "question", question)
                .build(), GPT3Model.GPT_3_5_TURBO
        );

//        AIFunction<GeneratePromptResponse> generateQuestionFunction = aiSupporter.createFunction(
//                "generateQuestions",
//                "generate new mathematics question base on given requiredKnowledgeList",
//                GeneratePromptResponse.class,
//                ConstraintsFactory
//                        .builder()
//                        .addConstraint("", "think as a Professional of Mathematics Education")
//                        .addConstraint("language", "english")
//                        .addConstraint("", "express the generated question contents(question, solving, choices and answer) entirely in a laTex expression")
//                        .addConstraint("difficulty level", "advanced")
//                        .addConstraint("number of question", "1")
//                        .build()
//        );
//
//        GeneratePromptResponse res2 = generateQuestionFunction.execute(ArgumentsFactory
//                .builder()
//                .addArgument("requiredKnowledgeList", res1.getRequiredKnowledgeList(), RequiredKnowledge.class)
//                .build(), GPT3Model.GPT_3_5_TURBO
//        );

        String prevFix = "Given two points on a graph $(x_1, y_1)$ and $(x_2, y_2)$, we can find the slope of the line passing through them using the formula $m = \\\frac{y_2-y_1}{x_2-x_1}$. Then, using the point-slope form of a line, we can express the equation of the line as $y-y_1 = m(x-x_1)$. Finally, we can simplify this expression to find the slope-intercept form of the line, which is $y = mx+b$, where $m$ is the slope we just found and $b=y_1-mx_1$ is the y-intercept of the line.";
        AIFunction<String> fixLaTexExpressionFunction = aiSupporter.createFunction("fixLaTexExpression",
                "Correct the given LaTex expression if it is not valid",
                WRAPPING.NONE,
                String.class,
                ConstraintsFactory.builder()
                        .addConstraint("", "Optimize for Java String")
                        .addConstraint("", "Please consider escape characters")
                        .addConstraint("", "\\ -> \\\\")
                        .build()
        );
        String fixed = fixLaTexExpressionFunction.execute(ArgumentsFactory.builder()
                .addArgument(WRAPPING.NONE, String.class, "prevFix", prevFix, "LaTex expression before fix")
                .build(), GPT3Model.GPT_3_5_TURBO
        );

        AIFunction<Contents> rewriteFunction = aiSupporter.createFunction("rewriteFunction",
                "Rearrange given paragraph into an array of lines following constraint",
                WRAPPING.NONE,
                Contents.class,
                ConstraintsFactory.builder()
                        .addConstraint("", "Each line within 10 words")
                        .addConstraint("", "Keep the LaTex expression(enclosed with $)")
                        .addConstraint("", "a LaTex expression is considered a word")
                        .build()
        );

        Contents rewrote = rewriteFunction.execute(ArgumentsFactory.builder()
                .addArgument(WRAPPING.NONE, String.class, "paragraph",
                        """
                                %s
                                """.formatted(fixed), "input paragraph")
                .build(), GPT3Model.GPT_3_5_TURBO);


        System.out.println("Finish");


    }
}