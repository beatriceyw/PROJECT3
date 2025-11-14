package org.example.project3.web;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

@WebFilter("/*")
public class Utf8Filter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 필터 초기화 시 필요한 설정이 있다면 여기에 작성
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        res.setCharacterEncoding("UTF-8");
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
        // 필터 종료 시 리소스 정리 작업이 있다면 여기에 작성
    }
}
