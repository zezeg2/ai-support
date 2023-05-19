package io.github.zezeg2.aisupport.example;

import io.github.zezeg2.aisupport.common.BaseSupportType;
import io.github.zezeg2.aisupport.common.FieldDesc;
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
