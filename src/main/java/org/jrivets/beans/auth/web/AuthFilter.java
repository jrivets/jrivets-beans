package org.jrivets.beans.auth.web;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jrivets.beans.auth.SecurityContextHolder;
import org.jrivets.beans.web.Constant;
import org.jrivets.log.Logger;
import org.jrivets.log.LoggerFactory;

import com.google.inject.Singleton;

@Singleton
public class AuthFilter implements Filter {
    
    protected Logger logger = LoggerFactory.getLogger(AuthFilter.class);
    
    private final String supportedMethods;

    @Inject
    AuthFilter(@Named("auth.methods") String supportedMethods) {
        this.supportedMethods = supportedMethods;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String authHeader = httpRequest.getHeader(Constant.AUTHORIZATION_HEADER);

        if (authHeader != null && SecurityContextHolder.getContext().getSession() == null) {
            unauthenticated(httpResponse, ErrorCode.UNSUPPORTED_AUTH_METHOD);
            return;
        }

        // at this point the call can be authenticated, or completely not (no
        // authentication header and session == null), so we propagate the
        // request to other filters and it is now matter of authorization to
        // perform it or reject with the settings
        chain.doFilter(request, response);
        
        if (SecurityContextHolder.getContext().isAuthRequired()) {
            unauthenticated(httpResponse, ErrorCode.AUTH_REQUIRED);
        }
    }

    @Override
    public void destroy() {

    }

    protected String getErrorMessage(ErrorCode errorCode) {
        return errorCode.message;
    }

    protected void unauthenticated(HttpServletResponse httpResponse, ErrorCode errorCode) throws IOException {
        logger.debug("Unathorized with the message: ", errorCode.message);
        if (errorCode == ErrorCode.INVALID_BASIC_AUTH_TOKEN || errorCode == ErrorCode.BAD_CREDENTIALS) {
            httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, getErrorMessage(errorCode));
            return;
        }
        
        StringTokenizer st = new StringTokenizer(supportedMethods, ";");
        while (st.hasMoreTokens()) {
            httpResponse.addHeader(Constant.AUTHENTICATE_HEADER, st.nextToken());
        }
        httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, getErrorMessage(errorCode));
    }

}
