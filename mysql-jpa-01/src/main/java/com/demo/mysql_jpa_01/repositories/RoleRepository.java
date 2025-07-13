package com.demo.mysql_jpa_01.repositories;

import com.demo.mysql_jpa_01.entities.RoleEntity;
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
    @Query("SELECT r FROM RoleEntity r WHERE r.status = 1 AND r.isDeleted = 0")
    List<RoleEntity> findAllActiveRoles();
    
    /**
     * 根据角色名查找启用的角色
     */
    @Query("SELECT r FROM RoleEntity r WHERE r.name = :name AND r.status = 1 AND r.isDeleted = 0")
    Optional<RoleEntity> findActiveRoleByName(@Param("name") String name);
    
    /**
     * 根据用户ID查找角色
     */
    @Query("SELECT r FROM RoleEntity r JOIN r.users u WHERE u.id = :userId AND r.status = 1 AND r.isDeleted = 0")
    List<RoleEntity> findRolesByUserId(@Param("userId") Long userId);
    
    /**
     * 查找默认角色（通常是ROLE_USER）
     */
    @Query("SELECT r FROM RoleEntity r WHERE r.name = 'ROLE_USER' AND r.status = 1 AND r.isDeleted = 0")
    Optional<RoleEntity> findDefaultRole();
}