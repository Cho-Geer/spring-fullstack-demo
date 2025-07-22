package com.demo.mysql_jpa_01.repositories;

import com.demo.mysql_jpa_01.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<UserEntity> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<UserEntity> findByEmail(String email);
    
    /**
     * 根据用户名或邮箱查找用户
     */
    Optional<UserEntity> findByUsernameOrEmail(String username, String email);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 根据用户名查找启用的用户
     */
    @Query("SELECT u FROM UserEntity u WHERE u.username = :username AND u.enabled = true AND u.isDeleted = false")
    Optional<UserEntity> findActiveUserByUsername(@Param("username") String username);
    
    /**
     * 根据邮箱查找启用的用户
     */
    @Query("SELECT u FROM UserEntity u WHERE u.email = :email AND u.enabled = true AND u.isDeleted = false")
    Optional<UserEntity> findActiveUserByEmail(@Param("email") String email);
    
    /**
     * 根据用户名或邮箱查找启用的用户
     */
    @Query("SELECT u FROM UserEntity u WHERE (u.username = :usernameOrEmail OR u.email = :usernameOrEmail) AND u.enabled = true AND u.isDeleted = false")
    Optional<UserEntity> findActiveUserByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);
    
    /**
     * 查找所有启用的用户
     */
    @Query("SELECT u FROM UserEntity u WHERE u.enabled = true AND u.isDeleted = false")
    java.util.List<UserEntity> findAllActiveUsers();
    
    /**
     * 根据角色名查找用户
     */
    @Query("SELECT u FROM UserEntity u JOIN u.roles r WHERE r.name = :roleName AND u.enabled = true AND u.isDeleted = false")
    java.util.List<UserEntity> findUsersByRoleName(@Param("roleName") String roleName);
}