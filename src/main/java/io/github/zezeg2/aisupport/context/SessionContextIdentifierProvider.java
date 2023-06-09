package io.github.zezeg2.aisupport.context;

import jakarta.servlet.http.HttpServletRequest;

public class SessionContextIdentifierProvider implements ContextIdentifierProvider {
    @Override
    public String getId(HttpServletRequest request) {
        return request.getSession().getId();
    }
}
