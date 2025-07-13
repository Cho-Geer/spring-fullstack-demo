package com.demo.mysql_jpa_01.entities;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(updatable = false, columnDefinition = "datetime(3) comment '创建时间'")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记（0未删除，1已删除；需配合 JPA 查询过滤）
     */
    @Column(columnDefinition = "tinyint(1) default 0 comment '逻辑删除标记（0未删，1已删）'")
    private Integer isDeleted;

    /**
     * 创建人（可选，需配合 Spring Security 或自定义审计）
     */
    @CreatedBy
    @Column(columnDefinition = "varchar(64) comment '创建人'")
    private String createdBy;

    /**
     * 更新人（可选）
     */
    @LastModifiedBy
    @Column(columnDefinition = "varchar(64) comment '更新人'")
    private String modifiedBy;
}
