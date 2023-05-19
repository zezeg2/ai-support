package io.github.zezeg2.aisupport.example;

import io.github.zezeg2.aisupport.common.BaseSupportType;
import io.github.zezeg2.aisupport.common.FieldDesc;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ValidationResponse implements BaseSupportType {
    @FieldDesc("Determining if it's LaTex Expression valid")
    private boolean valid;

    @FieldDesc("Correct the given LaTex expression if it is not valid else empty this field")
    private String fixedExpression;
}
