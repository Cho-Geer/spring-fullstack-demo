package com.demo.mysql_jpa_01.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users")
public class UserEntity extends BaseEntity implements UserDetails {
    
    /**
     * 用户名
     */
    @Column(name = "username", unique = true, nullable = false, length = 50, columnDefinition = "varchar(50) comment '用户名'")
    private String username;
    
    /**
     * 邮箱
     */
    @Column(name = "email", unique = true, nullable = false, length = 100, columnDefinition = "varchar(100) comment '邮箱'")
    private String email;
    
    /**
     * 密码（加密后）
     */
    @Column(name = "password", nullable = false, length = 255, columnDefinition = "varchar(255) comment '密码'")
    private String password;
    
    /**
     * 真实姓名
     */
    @Column(name = "real_name", length = 50, columnDefinition = "varchar(50) comment '真实姓名'")
    private String realName;
    
    /**
     * 手机号
     */
    @Column(name = "phone", length = 11, columnDefinition = "varchar(11) comment '手机号'")
    private String phone;
    
    /**
     * 账户是否启用
     */
    @Column(name = "enabled", nullable = false, columnDefinition = "tinyint(1) default 1 comment '账户是否启用（1启用，0禁用）'")
    private Boolean enabled = true;
    
    /**
     * 账户是否未过期
     */
    @Column(name = "account_non_expired", nullable = false, columnDefinition = "tinyint(1) default 1 comment '账户是否未过期（1未过期，0已过期）'")
    private Boolean accountNonExpired = true;
    
    /**
     * 账户是否未锁定
     */
    @Column(name = "account_non_locked", nullable = false, columnDefinition = "tinyint(1) default 1 comment '账户是否未锁定（1未锁定，0已锁定）'")
    private Boolean accountNonLocked = true;
    
    /**
     * 凭证是否未过期
     */
    @Column(name = "credentials_non_expired", nullable = false, columnDefinition = "tinyint(1) default 1 comment '凭证是否未过期（1未过期，0已过期）'")
    private Boolean credentialsNonExpired = true;
    
    /**
     * 用户角色集合
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles = new HashSet<>();
    
    public UserEntity() {}
    
    public UserEntity(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
    
    // UserDetails 接口实现
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    
    /**
     * 添加角色
     */
    public void addRole(RoleEntity role) {
        this.roles.add(role);
        role.getUsers().add(this);
    }
    
    /**
     * 移除角色
     */
    public void removeRole(RoleEntity role) {
        this.roles.remove(role);
        role.getUsers().remove(this);
    }
}
