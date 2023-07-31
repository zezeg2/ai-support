package io.github.zezeg2.aisupport.core.validator;

import io.github.zezeg2.aisupport.common.annotation.FieldDesc;
import io.github.zezeg2.aisupport.common.type.BaseSupportType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class FeedbackResponse extends BaseSupportType {
    @FieldDesc("Boolean indicating if the JSON is perfectly valid (true) or has issues (false)")
    private boolean valid;
    @FieldDesc("List of problems(issues) found in the JSON")
    private List<String> problems;
}