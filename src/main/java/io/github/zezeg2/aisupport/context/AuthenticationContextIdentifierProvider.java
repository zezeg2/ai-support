package io.github.zezeg2.aisupport.context;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticationContextIdentifierProvider implements ContextIdentifierProvider {
    @Override
    public String getId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        } else {
            return null;
        }
    }

    public String getId(HttpServletRequest request, String s) {
        return request.getUserPrincipal().getName() == null ? "anonymous" : request.getUserPrincipal().getName();
    }
}
