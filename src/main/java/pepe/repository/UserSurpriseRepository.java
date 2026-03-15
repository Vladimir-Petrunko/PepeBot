package pepe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pepe.entity.UserSurprise;

import java.util.List;

@Repository
public interface UserSurpriseRepository extends JpaRepository<UserSurprise, Long> {
    @Modifying
    @Query(nativeQuery = true, value = """
            DELETE FROM user_surprise
            WHERE user_from_id = :userId
            AND message_id IS NULL
            """)
    void deleteOngoingSurprises(@Param("userId") Long userId);

    @Modifying
    @Query(nativeQuery = true, value = """
            DELETE FROM user_surprise
            WHERE user_from_id = :userId
            AND sent IS NULL OR sent = 0
    """)
    void deleteOngoingSurprises2(@Param("userId") Long userId);

    @Query(nativeQuery = true, value = """
            SELECT * FROM user_surprise
            WHERE sent = 0 OR sent IS NULL
            AND user_from_id = :userId
            ORDER BY ID DESC
            LIMIT 1
    """)
    UserSurprise getActiveSurprise(@Param("userId") Long userId);

    UserSurprise findByMessageId(Integer messageId);

    @Query(nativeQuery = true, value = """
            SELECT message_id FROM user_surprise
            WHERE user_to_id = :userId
            AND user_from_name = 'bot'
    """)
    List<Integer> findTrash(@Param("userId") Long userId);

    @Modifying
    @Query(nativeQuery = true, value = """
            DELETE FROM user_surprise
            WHERE user_to_id = :userId
            AND user_from_name = 'bot'
    """)
    void clearTrash(@Param("userId") Long userId);

    @Modifying
    @Query(nativeQuery = true, value = """
            UPDATE user_surprise
            SET can_reply = 1
            WHERE message_id = :messageId
    """)
    void markCanReply(@Param("messageId") Integer messageId);
}
