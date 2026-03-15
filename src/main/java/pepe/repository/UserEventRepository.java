package pepe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pepe.entity.UserEvent;

import java.util.List;

@Repository
public interface UserEventRepository extends JpaRepository<UserEvent, Long> {
    int countByUserIdAndTypeAndInnerType(Long userId, String type, String innerType);

    @Query(nativeQuery = true, value = """
        SELECT DISTINCT user_id
        FROM user_event
        GROUP BY user_id
        HAVING MAX(timestamp) < NOW() - INTERVAL '24 hours'
    """)
    List<Long> findInactiveUsers();

    @Query(nativeQuery = true, value = """
        SELECT user_id
        FROM user_event
        WHERE user_name = :userName
        LIMIT 1
    """)
    Long getUserId(@Param("userName") String userName);

    @Query(nativeQuery = true, value = """
        SELECT user_name
        FROM user_event
        WHERE user_id = :userId
        LIMIT 1
    """)
    Long getUserName(@Param("userId") Long userId);
}
