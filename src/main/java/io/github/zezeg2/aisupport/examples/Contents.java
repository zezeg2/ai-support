package io.github.zezeg2.aisupport.examples;

import io.github.zezeg2.aisupport.common.BaseSupportType;
import io.github.zezeg2.aisupport.common.FieldDesc;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Contents implements BaseSupportType {
    @FieldDesc("each line")
    private List<String> lines;

    public Contents(List<String> lines) {
        this.lines = lines;
    }

    @Override
    public String toString() {
        return "Contents{" +
                "lines=" + lines +
                '}';
    }
}