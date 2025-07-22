package com.demo.mysql_jpa_01.services;

import com.demo.mysql_jpa_01.entities.UserEntity;
import com.demo.mysql_jpa_01.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security用户详情服务实现
 * 负责根据用户名加载用户信息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 根据用户名加载用户详情
     * 支持用户名或邮箱登录
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("尝试加载用户: {}", username);
        
        // 根据用户名或邮箱查找用户
        UserEntity user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> {
                    log.warn("用户不存在: {}", username);
                    return new UsernameNotFoundException("用户不存在: " + username);
                });
        
        // 检查用户是否被删除
        if (user.getIsDeleted() != null && user.getIsDeleted()) {
            log.warn("用户已被删除: {}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        // 检查用户是否被禁用
        if (!user.getEnabled()) {
            log.warn("用户已被禁用: {}", username);
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }
        
        log.debug("成功加载用户: {}, 角色数量: {}", user.getUsername(), user.getRoles().size());
        
        return user;
    }

    /**
     * 根据用户ID加载用户详情
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        log.debug("尝试根据ID加载用户: {}", userId);
        
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("用户ID不存在: {}", userId);
                    return new UsernameNotFoundException("用户ID不存在: " + userId);
                });
        
        // 检查用户是否被删除
        if (user.getIsDeleted() != null && user.getIsDeleted()) {
            log.warn("用户已被删除, ID: {}", userId);
            throw new UsernameNotFoundException("用户不存在: " + userId);
        }
        
        // 检查用户是否被禁用
        if (!user.getEnabled()) {
            log.warn("用户已被禁用, ID: {}", userId);
            throw new UsernameNotFoundException("用户已被禁用: " + userId);
        }
        
        log.debug("成功根据ID加载用户: {}, 用户名: {}", userId, user.getUsername());
        
        return user;
    }

    /**
     * 检查用户是否存在且启用
     */
    @Transactional(readOnly = true)
    public boolean isUserExistsAndEnabled(String username) {
        try {
            UserDetails userDetails = loadUserByUsername(username);
            return userDetails.isEnabled();
        } catch (UsernameNotFoundException e) {
            return false;
        }
    }

    /**
     * 获取用户实体（用于业务逻辑）
     */
    @Transactional(readOnly = true)
    public UserEntity getUserEntity(String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameOrEmail(username, username)
                .filter(user -> (user.getIsDeleted() == null || !user.getIsDeleted()) && user.getEnabled())
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在或已被禁用: " + username));
    }

    /**
     * 根据ID获取用户实体
     */
    @Transactional(readOnly = true)
    public UserEntity getUserEntityById(Long userId) throws UsernameNotFoundException {
        return userRepository.findById(userId)
                .filter(user -> (user.getIsDeleted() == null || !user.getIsDeleted()) && user.getEnabled())
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在或已被禁用, ID: " + userId));
    }
}