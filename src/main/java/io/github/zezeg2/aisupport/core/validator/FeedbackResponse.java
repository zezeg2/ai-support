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
    @FieldDesc("Boolean value indicating whether the given result is perfect (true) or not (false)")
    private boolean valid;
    private List<Issue> issueList;
}