package io.github.zezeg2.aisupport.context;

import jakarta.servlet.http.HttpServletRequest;

public class ThreadContextIdentifierProvider implements ContextIdentifierProvider {
    @Override
    public String getId(HttpServletRequest request) {
        return Thread.currentThread().getName();
    }
}
