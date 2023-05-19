package io.github.zezeg2.aisupport.example;

import io.github.zezeg2.aisupport.common.BaseSupportType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GeneratePromptResponse implements BaseSupportType {
    List<Question> questionList;
}
