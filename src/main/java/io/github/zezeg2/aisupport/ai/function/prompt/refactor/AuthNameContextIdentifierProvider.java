package io.github.zezeg2.aisupport.ai.function.prompt.refactor;

import io.github.zezeg2.aisupport.context.servlet.ContextIdentifierProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthNameContextIdentifierProvider implements ContextIdentifierProvider {
    @Override
    public String getId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getName();
        } else {
            return null;
        }
    }
}
