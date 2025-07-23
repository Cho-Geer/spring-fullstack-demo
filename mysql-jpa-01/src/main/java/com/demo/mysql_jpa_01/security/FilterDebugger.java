package com.demo.mysql_jpa_01.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 过滤器调试工具
 * 用于记录过滤器链的执行情况
 * 注意：这不是Spring组件，而是一个工具类
 */
@Slf4j
public class FilterDebugger extends OncePerRequestFilter {

    private final String filterName;
    private final Filter delegate;

    public FilterDebugger(String filterName, Filter delegate) {
        this.filterName = filterName;
        this.delegate = delegate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   jakarta.servlet.http.HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        log.debug("===== 过滤器 [{}] 开始处理请求: {} =====>", filterName, path);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 执行委托的过滤器
            delegate.doFilter(request, response, filterChain);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.debug("<===== 过滤器 [{}] 完成处理请求: {} (耗时: {}ms) =====", filterName, path, duration);
        }
    }

    /**
     * 创建一个调试过滤器包装器
     * @param name 过滤器名称
     * @param filter 要包装的过滤器
     * @return 包装后的调试过滤器
     */
    public static Filter wrap(String name, Filter filter) {
        return new FilterDebugger(name, filter);
    }
}