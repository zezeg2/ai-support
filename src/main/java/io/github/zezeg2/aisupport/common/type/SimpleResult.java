package io.github.zezeg2.aisupport.common.type;

import io.github.zezeg2.aisupport.common.bill.Bill;
import lombok.*;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class SimpleResult<T> extends BaseSupportType {
    private final T result;
    private final Bill bill;
}
