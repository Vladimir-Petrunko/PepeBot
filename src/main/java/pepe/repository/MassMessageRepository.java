package pepe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pepe.entity.MassMessage;

import java.util.List;

@Repository
public interface MassMessageRepository extends JpaRepository<MassMessage, Long> {
    @Query(nativeQuery = true, value = """
        SELECT * FROM mass_message
        WHERE id >= :fromId AND id <= :toId AND sent IS NULL
    """)
    List<MassMessage> findInRange(@Param("fromId") Long fromId, @Param("toId") Long toId);
}
