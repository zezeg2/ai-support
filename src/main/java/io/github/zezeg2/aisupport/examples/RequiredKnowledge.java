package io.github.zezeg2.aisupport.examples;

import io.github.zezeg2.aisupport.common.enums.BaseSupportType;
import io.github.zezeg2.aisupport.common.enums.FieldDesc;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RequiredKnowledge implements BaseSupportType {
    @FieldDesc("concept")
    String concept;
    @FieldDesc("description")
    String description;
}
