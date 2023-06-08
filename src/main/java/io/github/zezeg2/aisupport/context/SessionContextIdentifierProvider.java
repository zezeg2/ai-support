package io.github.zezeg2.aisupport.context;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SessionContextIdentifierProvider implements ContextIdentifierProvider {
    @Override
    public String getId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        } else {
            return null;
        }
    }

    public String getId(HttpServletRequest request) {
        return request.getSession().getId();
    }
}
