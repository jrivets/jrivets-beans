package org.jrivets.beans.auth.web;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jrivets.beans.auth.SecurityContext;
import org.jrivets.beans.auth.SecurityContextHolder;
import org.jrivets.beans.auth.Session;
import org.jrivets.beans.auth.SessionService;
import org.jrivets.log.Logger;
import org.jrivets.log.LoggerFactory;
import org.jrivets.util.UID;

/**
 * This filter looks for session by cookie and update SecurityContextHolder, if
 * the session is found. This filter should always be placed before any
 * AuthFilter, because it clears SecurityContextHolder as #1 thing.
 * 
 * @author Dmitry Spasibenko
 *
 */
@Singleton
public class CookieAuthFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(CookieAuthFilter.class);

    private final SessionService sessionService;

    private final String cookieName;

    @Inject
    CookieAuthFilter(SessionService sessionService, @Named("auth.cookieName") String cookieName) {
        this.sessionService = sessionService;
        this.cookieName = cookieName;
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        SecurityContextHolder.setContext(new SecurityContext());
        try {
            String cookie = CookieUtils.getCookie(httpRequest, cookieName);
            UID sessionId = null;
            if (cookie != null) {
                sessionId = new UID(cookie);
                Session session = sessionService.get(sessionId);
                if (session != null) {
                    logger.debug("Found session by cookie: ", session);
                    SecurityContextHolder.getContext().setSession(session);
                } else {
                    logger.debug("Unset cookie ", cookieName, "=", cookie, " , no session for it.");
                    CookieUtils.deleteCookie(httpResponse, cookieName);
                }
            }
            
            chain.doFilter(request, response);
            
            if (sessionId != null && SecurityContextHolder.getContext().getSession() == null) {
                logger.info("Catch unset session in the context, loging out");
                sessionService.delete(sessionId);
                CookieUtils.deleteCookie(httpResponse, cookieName);
            }
        } finally {
            SecurityContextHolder.setContext(null);
        }
    }

    public void destroy() {

    }
}
