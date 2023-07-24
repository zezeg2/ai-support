package io.github.zezeg2.aisupport.common.argument;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ArgumentDesc {
    private String keyDesc;
    private String valueDesc;
}
