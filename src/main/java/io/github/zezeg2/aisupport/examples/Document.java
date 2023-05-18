package io.github.zezeg2.aisupport.examples;

import io.github.zezeg2.aisupport.common.BaseSupportType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Document implements BaseSupportType {
    private String title;
    private Contents contents;

    public Document(String title, Contents contents) {
        this.title = title;
        this.contents = contents;
    }

    @Override
    public String toString() {

        return "Document{" +
                "title='" + title + '\'' +
                ", contents=" + contents +
                '}';
    }
}