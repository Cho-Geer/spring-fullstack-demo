package com.demo.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true, exclude = {"users", "permissions"})
@Entity
@Table(name = "roles")
public class RoleEntity extends BaseEntity {
    
    /**
     * 角色名称（如：ROLE_USER, ROLE_ADMIN）
     */
    // @Column(name = "name", unique = true, nullable = false, length = 20, columnDefinition = "varchar(20) /* comment '角色名称' */")
    @Column(name = "name", unique = true, nullable = false, length = 20)
    private String name;
    
    /**
     * 角色描述
     */
    // @Column(name = "description", length = 200, columnDefinition = "varchar(200) /* comment '角色描述' */")
    @Column(name = "description", length = 200)
    private String description;
    
    /**
     * 角色状态（1正常，0禁用）
     */
    // @Column(name = "status", nullable = false, columnDefinition = "tinyint(1) default 1 /* comment '角色状态（1正常，0禁用）' */")
    @Column(name = "status", nullable = false)
    private Boolean status = true;

    /**
     * 拥有此角色的用户集合
     */
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<UserEntity> users = new HashSet<>();
    
    /**
     * 角色拥有的权限集合
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<PermissionEntity> permissions = new HashSet<>();
    
    public RoleEntity() {}
    
    public RoleEntity(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    /**
     * 添加权限
     */
    public void addPermission(PermissionEntity permission) {
        this.permissions.add(permission);
        permission.getRoles().add(this);
    }
    
    /**
     * 移除权限
     */
    public void removePermission(PermissionEntity permission) {
        this.permissions.remove(permission);
        permission.getRoles().remove(this);
    }
    
    /**
     * 检查是否拥有指定权限
     */
    public boolean hasPermission(String permissionCode) {
        return permissions.stream()
                .anyMatch(permission -> permission.getPermissionCode().equals(permissionCode));
    }
}
