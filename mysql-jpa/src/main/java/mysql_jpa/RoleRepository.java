package main.java.mysql_jpa;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    // JpaRepository已经提供了save, findAll, findById, deleteById等方法
    // 只需要定义自定义查询方法
    
    @Query("Select r From RoleEntity r Where r.roleName = :roleName")
    Optional<RoleEntity> findByRoleName(@Param("roleName") String roleName);
    
    @Query(value = "SELECT * FROM roles WHERE role_name LIKE CONCAT('%', :domain, '%')", nativeQuery = true)
    List<RoleEntity> findByRoleNameLike(@Param("domain") String domain);
    
    @Query("SELECT r FROM RoleEntity r WHERE r.roleName LIKE %:keyword%")
    Page<RoleEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
