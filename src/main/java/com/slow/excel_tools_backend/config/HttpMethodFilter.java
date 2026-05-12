package com.slow.excel_tools_backend.config;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

@Component
public class HttpMethodFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String method = httpRequest.getMethod();
        String contentType = httpRequest.getContentType();
        
        // 只转换非multipart请求的HTTP方法
        if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
            // multipart请求不转换方法，直接传递
            chain.doFilter(request, response);
        } else if (method != null && method.equals(method.toLowerCase())) {
            // 非multipart请求且方法为小写时，转换为大写
            chain.doFilter(new HttpMethodRequestWrapper(httpRequest), response);
        } else {
            chain.doFilter(request, response);
        }
    }

    private static class HttpMethodRequestWrapper extends HttpServletRequestWrapper {
        private final String method;

        public HttpMethodRequestWrapper(HttpServletRequest request) {
            super(request);
            this.method = request.getMethod().toUpperCase();
        }

        @Override
        public String getMethod() {
            return method;
        }
    }
}
