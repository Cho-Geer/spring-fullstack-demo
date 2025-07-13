package com.demo.mysql_jpa_01.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "roles")
public class RoleEntity extends BaseEntity {
    
    /**
     * 角色名称（如：ROLE_USER, ROLE_ADMIN）
     */
    @Column(name = "name", unique = true, nullable = false, length = 20, columnDefinition = "varchar(20) comment '角色名称'")
    private String name;
    
    /**
     * 角色描述
     */
    @Column(name = "description", length = 200, columnDefinition = "varchar(200) comment '角色描述'")
    private String description;
    
    /**
     * 角色状态（1正常，0禁用）
     */
    @Column(name = "status", nullable = false, columnDefinition = "tinyint(1) default 1 comment '角色状态（1正常，0禁用）'")
    private Boolean status;

    /**
     * 拥有此角色的用户集合
     */
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<UserEntity> users = new HashSet<>();
    
    public RoleEntity() {}
    
    public RoleEntity(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
