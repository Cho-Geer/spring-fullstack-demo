package com.demo.backend.repositories;

import com.demo.backend.entities.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    
    /**
     * 根据角色名查找角色
     */
    Optional<RoleEntity> findByName(String name);
    
    /**
     * 检查角色名是否存在
     */
    boolean existsByName(String name);
    
    /**
     * 根据角色名集合查找角色
     */
    List<RoleEntity> findByNameIn(Set<String> names);
    
    /**
     * 查找所有启用的角色
     */
    @Query("SELECT r FROM RoleEntity r WHERE r.status = true AND r.isDeleted = false")
    List<RoleEntity> findAllActiveRoles();
    
    /**
     * 根据角色名查找启用的角色
     */
    @Query("SELECT r FROM RoleEntity r WHERE r.name = :name AND r.status = true AND r.isDeleted = false")
    Optional<RoleEntity> findActiveRoleByName(@Param("name") String name);
    
    /**
     * 根据用户ID查找角色
     */
    @Query("SELECT r FROM RoleEntity r JOIN r.users u WHERE u.id = :userId AND r.status = true AND r.isDeleted = false")
    List<RoleEntity> findRolesByUserId(@Param("userId") Long userId);
    
    /**
     * 查找默认角色（通常是ROLE_USER）
     */
    @Query("SELECT r FROM RoleEntity r WHERE r.name = 'ROLE_USER' AND r.status = true AND r.isDeleted = false")
    Optional<RoleEntity> findDefaultRole();
    
    /**
     * 根据权限代码查找拥有该权限的角色
     */
    @Query("SELECT r FROM RoleEntity r JOIN r.permissions p WHERE p.permissionCode = :permissionCode AND r.status = true AND r.isDeleted = false")
    List<RoleEntity> findRolesByPermissionCode(@Param("permissionCode") String permissionCode);
    
    /**
     * 根据权限ID查找拥有该权限的角色
     */
    @Query("SELECT r FROM RoleEntity r JOIN r.permissions p WHERE p.id = :permissionId AND r.status = true AND r.isDeleted = false")
    List<RoleEntity> findRolesByPermissionId(@Param("permissionId") Long permissionId);
    
    /**
     * 查找拥有指定权限集合的角色
     */
    @Query("SELECT DISTINCT r FROM RoleEntity r JOIN r.permissions p WHERE p.permissionCode IN :permissionCodes AND r.status = true AND r.isDeleted = false")
    List<RoleEntity> findRolesByPermissionCodes(@Param("permissionCodes") Set<String> permissionCodes);
}