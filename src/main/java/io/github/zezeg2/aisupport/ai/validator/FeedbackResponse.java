package io.github.zezeg2.aisupport.ai.validator;

import io.github.zezeg2.aisupport.common.BaseSupportType;
import io.github.zezeg2.aisupport.common.FieldDesc;
import lombok.Data;

import java.util.List;

@Data
public class FeedbackResponse extends BaseSupportType {
    @FieldDesc("Determine whether the given result is flawless or not and express it as boolean value true or false")
    private boolean valid;
    @FieldDesc("This is List of problems in the results")
    private List<String> feedbacks;
}