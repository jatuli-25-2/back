package com.p_project.friend;

import com.p_project.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendRepository extends JpaRepository<FriendEntity, Long> {

    @Query(value = """
    SELECT u.*
    FROM users u
    WHERE u.id IN (
      SELECT DISTINCT 
        CASE
          WHEN f1.from_user_id = :userId THEN f1.to_user_id
          ELSE f1.from_user_id
        END
      FROM friends f1
      JOIN friends f2
        ON f1.from_user_id = f2.to_user_id
       AND f1.to_user_id = f2.from_user_id
      WHERE :userId IN (f1.from_user_id, f1.to_user_id)
    )
    """, nativeQuery = true)
    List<UserEntity> findMutualFriends(@Param("userId") Long userId);


}
