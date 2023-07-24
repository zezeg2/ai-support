package io.github.zezeg2.aisupport.core.validator;

import io.github.zezeg2.aisupport.common.type.BaseSupportType;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@EqualsAndHashCode(callSuper = true)
public class FeedbackRequest extends BaseSupportType {
    private Map<String, Object> userInput;
    private Object assistantOutput;
}