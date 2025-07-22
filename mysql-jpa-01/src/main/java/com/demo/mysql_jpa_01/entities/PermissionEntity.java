package com.demo.mysql_jpa_01.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "permissions")
public class PermissionEntity extends BaseEntity {
    
    /**
     * 权限代码（如：USER_CREATE, USER_DELETE, USER_UPDATE, USER_VIEW）
     */
    // @Column(name = "permission_code", unique = true, nullable = false, length = 100, 
            // columnDefinition = "varchar(100) /* comment '权限代码' */")
        @Column(name = "permission_code", unique = true, nullable = false, length = 100)
    private String permissionCode;
    
    /**
     * 权限名称
     */
    // @Column(nullable = false, length = 100, columnDefinition = "varchar(100) /* comment '权限名称' */")
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    /**
     * 权限描述
     */
    // @Column(length = 200, columnDefinition = "varchar(200) /* comment '权限描述' */")
    @Column(name = "description", length = 200)
    private String description;
    
    /**
     * 权限状态（1启用，0禁用）
     */
    // @Column(columnDefinition = "tinyint(1) default 1 /* comment '权限状态（1启用，0禁用）' */")
    @Column(name = "status", nullable = false)
    private Boolean status = true;
    
    /**
     * 权限分类（如：USER, ROLE, SYSTEM等）
     */
    // @Column(length = 50, columnDefinition = "varchar(50) /* comment '权限分类' */")
    @Column(name = "category", length = 50, nullable = false)
    private String category;
    
    /**
     * 拥有此权限的角色集合
     */
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<RoleEntity> roles = new HashSet<>();
    
    public PermissionEntity() {}
    
    public PermissionEntity(String permissionCode, String name, String description) {
        this.permissionCode = permissionCode;
        this.name = name;
        this.description = description;
    }
    
    public PermissionEntity(String permissionCode, String name, String description, String category) {
        this.permissionCode = permissionCode;
        this.name = name;
        this.description = description;
        this.category = category;
    }
}