package pepe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pepe.entity.UserBan;

@Repository
public interface UserBanRepository extends JpaRepository<UserBan, Long> {
    boolean existsByUserId(Long userId);
}
