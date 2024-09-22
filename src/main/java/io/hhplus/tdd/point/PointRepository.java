package io.hhplus.tdd.point;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface PointRepository extends JpaRepository<UserPoint, Long> {
}
