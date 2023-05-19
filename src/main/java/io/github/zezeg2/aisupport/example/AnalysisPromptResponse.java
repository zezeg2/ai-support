package io.github.zezeg2.aisupport.example;

import io.github.zezeg2.aisupport.common.BaseSupportType;
import io.github.zezeg2.aisupport.common.FieldDesc;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class AnalysisPromptResponse implements BaseSupportType {
    @FieldDesc("requiredKnowledgeList")
    List<RequiredKnowledge> requiredKnowledgeList;
}
