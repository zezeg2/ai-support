package io.github.zezeg2.aisupport.context;

public class ThreadNameIdentifierProvider implements ContextIdentifierProvider {
    @Override
    public String getId() {
        return Thread.currentThread().getName();
    }
}
