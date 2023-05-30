package io.github.zezeg2.aisupport.context;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SessionContextIdentifierProvider implements ContextIdentifierProvider {
    @Override
    public String getId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attributes.getRequest().getSession(false);
        if (session != null) {
            return session.getId();
        } else {
            return null;
        }
    }
}
