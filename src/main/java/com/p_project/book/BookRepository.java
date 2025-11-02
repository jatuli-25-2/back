package com.p_project.book;

import com.p_project.writing.WritingSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<WritingSessionEntity, Long> {

    int countByUserIdAndTypeAndDeletedAtIsNull(Long userId, WritingSessionEntity.Type type); // TODO: status도 조건문 추가 필요

}
