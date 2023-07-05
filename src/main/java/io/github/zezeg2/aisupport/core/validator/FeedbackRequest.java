package io.github.zezeg2.aisupport.core.validator;

import io.github.zezeg2.aisupport.common.BaseSupportType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class FeedbackRequest extends BaseSupportType {
    private String userInput;
    private String assistantOutput;
}