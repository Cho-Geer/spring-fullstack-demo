package com.demo.backend.repositories;

import com.demo.backend.entities.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {
    
    /**
     * 根据权限代码查找权限
     */
    Optional<PermissionEntity> findByPermissionCode(String permissionCode);
    
    /**
     * 检查权限代码是否存在
     */
    boolean existsByPermissionCode(String permissionCode);
    
    /**
     * 根据权限代码集合查找权限
     */
    List<PermissionEntity> findByPermissionCodeIn(Set<String> permissionCodes);
    
    /**
     * 根据权限分类查找权限
     */
    List<PermissionEntity> findByCategory(String category);
    
    /**
     * 查找所有启用的权限
     */
    @Query("SELECT p FROM PermissionEntity p WHERE p.status = true AND p.isDeleted = false")
    List<PermissionEntity> findAllActivePermissions();
    
    /**
     * 根据权限代码查找启用的权限
     */
    @Query("SELECT p FROM PermissionEntity p WHERE p.permissionCode = :permissionCode AND p.status = true AND p.isDeleted = false")
    Optional<PermissionEntity> findActivePermissionByCode(@Param("permissionCode") String permissionCode);
    
    /**
     * 根据分类查找启用的权限
     */
    @Query("SELECT p FROM PermissionEntity p WHERE p.category = :category AND p.status = true AND p.isDeleted = false")
    List<PermissionEntity> findActivePermissionsByCategory(@Param("category") String category);
    
    /**
     * 根据角色ID查找权限
     */
    @Query("SELECT p FROM PermissionEntity p JOIN p.roles r WHERE r.id = :roleId AND p.status = true AND p.isDeleted = false")
    List<PermissionEntity> findPermissionsByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 根据角色名查找权限
     */
    @Query("SELECT p FROM PermissionEntity p JOIN p.roles r WHERE r.name = :roleName AND p.status = true AND p.isDeleted = false")
    List<PermissionEntity> findPermissionsByRoleName(@Param("roleName") String roleName);
    
    /**
     * 根据用户ID查找权限
     */
    @Query("SELECT DISTINCT p FROM PermissionEntity p JOIN p.roles r JOIN r.users u WHERE u.id = :userId AND p.status = true AND p.isDeleted = false")
    List<PermissionEntity> findPermissionsByUserId(@Param("userId") Long userId);
    
    /**
     * 根据权限名称模糊查找
     */
    @Query("SELECT p FROM PermissionEntity p WHERE p.name LIKE %:name% AND p.status = true AND p.isDeleted = false")
    List<PermissionEntity> findPermissionsByNameContaining(@Param("name") String name);
}