package io.github.zezeg2.aisupport.core.validator;

import io.github.zezeg2.aisupport.common.annotation.FieldDesc;
import io.github.zezeg2.aisupport.common.type.BaseSupportType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Issue extends BaseSupportType {
    @FieldDesc("caught problem")
    String problem;
    @FieldDesc("Resolution of problem. this feedback will be utilize to improve next JSON result.")
    String solution;
}
