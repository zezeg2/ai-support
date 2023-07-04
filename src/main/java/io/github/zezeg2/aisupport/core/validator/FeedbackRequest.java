package io.github.zezeg2.aisupport.core.validator;

import io.github.zezeg2.aisupport.common.BaseSupportType;
import io.github.zezeg2.aisupport.common.FieldDesc;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class FeedbackRequest extends BaseSupportType {
    private String userInput;
    private String assistantOutput;
}