package com.demo.backend.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

/**
 * 过滤器链调试工具
 * 用于监控Spring Security过滤器链的执行顺序和行为
 */
@Slf4j
@Component
public class FilterChainDebugger extends OncePerRequestFilter {

    @Autowired
    private ApplicationContext applicationContext;
    
    private FilterChainProxy filterChainProxy;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   jakarta.servlet.http.HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        log.debug("===== 请求路径: {} 开始处理 =====", path);
        
        try {
            // 尝试获取并打印过滤器链信息
            printFilterChainInfo(request);
        } catch (Exception e) {
            log.warn("无法获取过滤器链信息: {}", e.getMessage());
        }
        
        // 继续过滤器链
        filterChain.doFilter(request, response);
        
        log.debug("===== 请求路径: {} 处理完成 =====", path);
    }

    /**
     * 获取FilterChainProxy（延迟初始化以避免循环依赖）
     */
    private FilterChainProxy getFilterChainProxy() {
        if (filterChainProxy == null) {
            try {
                filterChainProxy = applicationContext.getBean(FilterChainProxy.class);
            } catch (Exception e) {
                log.warn("无法获取FilterChainProxy: {}", e.getMessage());
            }
        }
        return filterChainProxy;
    }
    
    /**
     * 打印过滤器链信息
     */
    private void printFilterChainInfo(HttpServletRequest request) {
        FilterChainProxy proxy = getFilterChainProxy();
        if (proxy == null) {
            log.debug("FilterChainProxy未初始化，跳过过滤器链信息打印");
            return;
        }
        
        try {
            // 获取所有过滤器链
            Field filtersField = FilterChainProxy.class.getDeclaredField("filterChains");
            filtersField.setAccessible(true);
            List<?> filterChains = (List<?>) filtersField.get(proxy);
            
            // 检查filterChains是否为null
            if (filterChains == null) {
                log.debug("过滤器链列表为空，跳过过滤器链信息打印");
                return;
            }
            
            log.debug("总共有 {} 个过滤器链配置", filterChains.size());
            
            // 查找匹配当前请求的过滤器链
            for (int i = 0; i < filterChains.size(); i++) {
                Object chainObject = filterChains.get(i);
                
                // 检查chainObject是否为null
                if (chainObject == null) {
                    log.debug("过滤器链 #{} 为空，跳过", i);
                    continue;
                }
                
                // 获取SecurityFilterChain对象
                Field chainField = chainObject.getClass().getDeclaredField("securityFilterChain");
                chainField.setAccessible(true);
                SecurityFilterChain chain = (SecurityFilterChain) chainField.get(chainObject);
                
                // 检查chain是否为null
                if (chain == null) {
                    log.debug("过滤器链 #{} 的SecurityFilterChain为空，跳过", i);
                    continue;
                }
                
                // 获取过滤器链中的所有过滤器
                Field filtersField2 = chainObject.getClass().getDeclaredField("filters");
                filtersField2.setAccessible(true);
                List<Filter> filters = (List<Filter>) filtersField2.get(chainObject);
                
                // 检查filters是否为null
                if (filters == null) {
                    log.debug("过滤器链 #{} 的过滤器列表为空，跳过", i);
                    continue;
                }
                
                // 检查是否匹配当前请求
                if (chain.matches(request)) {
                    log.debug("请求 {} 匹配过滤器链 #{}", request.getRequestURI(), i);
                    log.debug("过滤器链 #{} 包含 {} 个过滤器:", i, filters.size());
                    
                    for (int j = 0; j < filters.size(); j++) {
                        Filter filter = filters.get(j);
                        
                        // 检查filter是否为null
                        if (filter == null) {
                            log.debug("  {}. [null filter]", j);
                            continue;
                        }
                        
                        log.debug("  {}. {}", j, filter.getClass().getName());
                        
                        // 特别检查JwtAuthenticationFilter
                        if (filter instanceof JwtAuthenticationFilter) {
                            JwtAuthenticationFilter jwtFilter = (JwtAuthenticationFilter) filter;
                            boolean shouldNotFilter = false;
                            try {
                                shouldNotFilter = jwtFilter.shouldNotFilter(request);
                                log.debug("  - JwtAuthenticationFilter.shouldNotFilter({}): {}", 
                                        request.getRequestURI(), shouldNotFilter);
                            } catch (Exception e) {
                                log.warn("  - 调用JwtAuthenticationFilter.shouldNotFilter失败: {}", e.getMessage());
                            }
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("获取过滤器链信息失败: {}", e.getMessage(), e);
        }
    }
}