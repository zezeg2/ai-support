package io.github.zezeg2.aisupport.core.validator;

import io.github.zezeg2.aisupport.common.type.BaseSupportType;
import io.github.zezeg2.aisupport.common.annotation.FieldDesc;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class FeedbackResponse extends BaseSupportType {
    @FieldDesc("Boolean value indicating whether the given result is perfect (true) or not (false) here")
    private boolean valid;
    @FieldDesc("List of problems in the results here")
    private List<String> problems;
}