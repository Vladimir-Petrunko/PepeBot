package pepe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pepe.entity.UserHoroscope;

@Repository
public interface UserHoroscopeRepository extends JpaRepository<UserHoroscope, Long> {
    UserHoroscope findByUserIdAndDate(Long userId, String date);
}
