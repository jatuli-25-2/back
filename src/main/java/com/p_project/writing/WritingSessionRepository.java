package com.p_project.writing;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface WritingSessionRepository extends JpaRepository<WritingSessionEntity, Long> {

    @Query("SELECT w FROM WritingSessionEntity w " +
            "WHERE w.userId = :userId AND w.deletedAt IS NULL " +
            "AND w.status = 'COMPLETE'" +
            "AND (w.type = 'diary' OR w.type = 'book') " +
            "ORDER BY w.createdAt DESC")
    List<WritingSessionEntity> findRecentWritingSessions(Long userId, Pageable pageable);


    @Query("SELECT COUNT(ws) FROM WritingSessionEntity ws " +
            "WHERE ws.emotion = :emotion " +
            "AND ws.status = 'COMPLETE'" +
            "AND DATE(ws.createdAt) = :date")
    int countByEmotionAndCreatedAt(@Param("emotion") String emotion,
                                   @Param("date") LocalDate date);

    /** 해당 월의 완료된 글 전체 조회 (일기 우선·먼저 쓴 순 정렬용) */
    @Query("SELECT w FROM WritingSessionEntity w " +
            "WHERE w.userId = :userId AND w.deletedAt IS NULL " +
            "AND w.status = 'COMPLETE' " +
            "AND (w.type = 'diary' OR w.type = 'book') " +
            "AND w.createdAt >= :start AND w.createdAt < :end " +
            "ORDER BY w.createdAt ASC")
    List<WritingSessionEntity> findByUserIdAndMonth(@Param("userId") Long userId,
                                                     @Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);

    /** 해당 날짜에 쓴 완료된 글 목록 (일기 우선, 같은 타입이면 먼저 쓴 순) */
    @Query("SELECT w FROM WritingSessionEntity w " +
            "WHERE w.userId = :userId AND w.deletedAt IS NULL " +
            "AND w.status = 'COMPLETE' " +
            "AND (w.type = 'diary' OR w.type = 'book') " +
            "AND DATE(w.createdAt) = :date " +
            "ORDER BY w.type ASC, w.createdAt ASC")
    List<WritingSessionEntity> findByUserIdAndDate(@Param("userId") Long userId,
                                                   @Param("date") LocalDate date);

}
