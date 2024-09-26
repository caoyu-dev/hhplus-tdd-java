package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Repository;

@Repository
public class PointRepository {
    private final UserPointTable userPointTable;

    public PointRepository(UserPointTable userPointTable) {
        this.userPointTable = userPointTable;
    }

    public UserPoint addPoint(final long id, final long point) {
        return userPointTable.insertOrUpdate(id, point);
    }

    public UserPoint findByUserId(final long id) {
        return userPointTable.selectById(id);
    }
}
