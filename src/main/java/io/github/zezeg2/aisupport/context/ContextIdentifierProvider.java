package io.github.zezeg2.aisupport.context;

import jakarta.servlet.http.HttpServletRequest;

public interface ContextIdentifierProvider {
    String getId(HttpServletRequest request);
}
