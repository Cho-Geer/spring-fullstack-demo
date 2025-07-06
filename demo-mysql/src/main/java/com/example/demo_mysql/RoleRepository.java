package com.example.demo_mysql;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    // 根据角色名查找用户
    Optional<Role> findByRoleName(String roleName);
    // 自定义查询
    @Query("SELECT r FROM Role r WHERE r.description = :description")
    Optional<Role> findByDescription(String description);
    // 原生SQL查询
    @Query(value = "SELECT * FROM roles WHERE description LIKE %:domain%", nativeQuery = true)
    List<Role> findByDescriptionContaining(@Param("domain") String domain);

}
