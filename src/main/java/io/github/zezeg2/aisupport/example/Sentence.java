package io.github.zezeg2.aisupport.example;

import io.github.zezeg2.aisupport.common.BaseSupportType;
import io.github.zezeg2.aisupport.common.FieldDesc;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Sentence implements BaseSupportType {
    @FieldDesc("Each words of paragraph")
    private List<String> words;

    public Sentence(List<String> lines) {
        this.words = lines;
    }
}