package io.github.zezeg2.aisupport.example;

import io.github.zezeg2.aisupport.common.BaseSupportType;
import io.github.zezeg2.aisupport.common.FieldDesc;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Paragraph implements BaseSupportType {
    @FieldDesc("Each lines of paragraph")
    private List<String> lines;

    public Paragraph(List<String> lines) {
        this.lines = lines;
    }

    @Override
    public String toString() {
        return "Paragraph{" +
                "lines=" + lines +
                '}';
    }
}