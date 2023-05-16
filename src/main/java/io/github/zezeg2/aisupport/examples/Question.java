package io.github.zezeg2.aisupport.examples;

import io.github.zezeg2.aisupport.common.enums.BaseSupportType;
import io.github.zezeg2.aisupport.common.enums.FieldDesc;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Question implements BaseSupportType {
    @FieldDesc("subjective or multiple choice")
    String type;
    @FieldDesc("text of the question")
    String text;
    @FieldDesc("how to solve this question, express it with LaTex expression")
    String solving;
    @FieldDesc("contents of question, express it with LaTex expression")
    String content;
    @FieldDesc("answer list, express it with LaTex expression")
    List<String> answer;
    @FieldDesc("null if type is subjective, express it with LaTex expression")
    List<String> choices;
}
