package com.demo.backend.config;

import com.demo.backend.entities.RoleEntity;
import com.demo.backend.entities.PermissionEntity;
import com.demo.backend.repositories.RoleRepository;
import com.demo.backend.repositories.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 数据初始化器
 * 在应用启动时创建默认的角色和权限
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("开始初始化默认数据...");
        
        // 初始化默认权限
        initializePermissions();
        
        // 初始化默认角色
        initializeRoles();
        
        log.info("默认数据初始化完成");
    }
    
    /**
     * 初始化默认权限
     */
    private void initializePermissions() {
        // 创建基础权限
        createPermissionIfNotExists("USER_READ", "用户查看", "用户查看权限", "USER");
        createPermissionIfNotExists("USER_WRITE", "用户编辑", "用户编辑权限", "USER");
        createPermissionIfNotExists("USER_DELETE", "用户删除", "用户删除权限", "USER");
        createPermissionIfNotExists("ADMIN_READ", "管理员查看", "管理员查看权限", "ADMIN");
        createPermissionIfNotExists("ADMIN_WRITE", "管理员编辑", "管理员编辑权限", "ADMIN");
        createPermissionIfNotExists("ADMIN_DELETE", "管理员删除", "管理员删除权限", "ADMIN");
        
        log.info("权限初始化完成");
    }
    
    /**
     * 初始化默认角色
     */
    private void initializeRoles() {
        // 创建用户角色
        createRoleIfNotExists("ROLE_USER", "普通用户角色");
        
        // 创建管理员角色
        createRoleIfNotExists("ROLE_ADMIN", "管理员角色");
        
        log.info("角色初始化完成");
    }
    
    /**
     * 创建权限（如果不存在）
     */
    private void createPermissionIfNotExists(String permissionCode, String name, String description, String category) {
        if (!permissionRepository.existsByPermissionCode(permissionCode)) {
            PermissionEntity permission = new PermissionEntity();
            permission.setPermissionCode(permissionCode);
            permission.setName(name);
            permission.setDescription(description);
            permission.setCategory(category);
            permission.setStatus(true);
            permission.setIsDeleted(false);
            // 审计字段（createdBy, modifiedBy, createdAt, updatedAt）由Spring审计功能自动填充
            permissionRepository.save(permission);
            log.info("创建默认权限: {}", permissionCode);
        }
    }
    
    /**
     * 创建角色（如果不存在）
     */
    private void createRoleIfNotExists(String roleName, String description) {
        if (!roleRepository.existsByName(roleName)) {
            RoleEntity role = new RoleEntity();
            role.setName(roleName);
            role.setDescription(description);
            role.setStatus(true);
            role.setIsDeleted(false);
            // 审计字段（createdBy, modifiedBy, createdAt, updatedAt）由Spring审计功能自动填充
            roleRepository.save(role);
            log.info("创建默认角色: {}", roleName);
        }
    }
}