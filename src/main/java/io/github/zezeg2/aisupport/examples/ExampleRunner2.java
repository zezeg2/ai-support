package io.github.zezeg2.aisupport.examples;

public class ExampleRunner2 {
    public static void main(String[] args) throws IllegalAccessException {
        AnalysisPromptResponse analysisPromptResponse = new AnalysisPromptResponse();
        String example = analysisPromptResponse.getExample();
        System.out.println(example);
    }
}
