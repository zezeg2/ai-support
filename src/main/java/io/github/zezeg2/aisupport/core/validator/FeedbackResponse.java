package io.github.zezeg2.aisupport.core.validator;

import io.github.zezeg2.aisupport.common.BaseSupportType;
import io.github.zezeg2.aisupport.common.FieldDesc;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class FeedbackResponse extends BaseSupportType {
    @FieldDesc("Boolean value indicating whether the given result is perfect (true) or not (false) here")
    private boolean valid;
    @FieldDesc("List of problems in the results here")
    private List<String> feedbacks;
}