package io.github.zezeg2.aisupport.common.type;

import com.theokanning.openai.Usage;
import lombok.*;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class SimpleResult<T> extends BaseSupportType {
    private final T result;
    private final Usage totalUsage;
}
