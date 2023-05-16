package io.github.zezeg2.aisupport.example;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Contents {
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