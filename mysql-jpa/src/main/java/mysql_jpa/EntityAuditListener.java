package main.java.mysql_jpa;

import lombok.extern.slf4j.Slf4j;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Slf4j
public class EntityAuditListener {
    @PrePersist
    public void prePersist(Object entity) {
        log.info("新規エンティティ作成: {}", entity);
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        log.info("エンティティ更新: {}", entity);
    }
}
