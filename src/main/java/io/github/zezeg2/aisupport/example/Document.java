package io.github.zezeg2.aisupport.example;

import io.github.zezeg2.aisupport.common.BaseSupportType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Document implements BaseSupportType {
    private String title;
    private Paragraph paragraph;

    public Document(String title, Paragraph paragraph) {
        this.title = title;
        this.paragraph = paragraph;
    }

    @Override
    public String toString() {

        return "Document{" +
                "title='" + title + '\'' +
                ", contents=" + paragraph +
                '}';
    }
}