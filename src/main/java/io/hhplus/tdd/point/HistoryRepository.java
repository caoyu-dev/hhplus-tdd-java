package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class HistoryRepository {
    private final PointHistoryTable pointHistoryTable;

    public HistoryRepository(PointHistoryTable pointHistoryTable) {
        this.pointHistoryTable = pointHistoryTable;
    }

    public PointHistory addHistory(final long userId, final long amount, final TransactionType type, final long updateMillis) {
        return pointHistoryTable.insert(userId, amount, type, updateMillis);
    }

    public List<PointHistory> selectAllByUserId(final long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }
}
