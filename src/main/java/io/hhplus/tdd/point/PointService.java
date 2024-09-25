package io.hhplus.tdd.point;

import io.hhplus.tdd.ErrorCode;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final ExecutorService executor;
    private static final Logger log = LoggerFactory.getLogger(PointService.class);

    public PointService(UserPointTable userPointTable,
                        PointHistoryTable pointHistoryTable,
                        ExecutorService executor)
    {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
        this.executor = executor;
    }

    public CompletableFuture<UserPoint> chargePoints(long id, long amount) {
        return CompletableFuture.supplyAsync(() -> {
            if (amount <= 0) {
                throw new IllegalArgumentException(ErrorCode.ADD_UNDER_VALUE_FAILED.getMessage());
            }
            UserPoint existingUserPoint = userPointTable.selectById(id);
            long updatedPoints = calculateIncreasedPoints(existingUserPoint.point(), amount);
            UserPoint updatedUserPoint = userPointTable.insertOrUpdate(id, updatedPoints);

            recordTransaction(id, amount, TransactionType.CHARGE);
            return updatedUserPoint;
        }, executor);
    }

    public CompletableFuture<UserPoint> getPoint(long id) {
        return CompletableFuture.supplyAsync(() -> {
            UserPoint existingUserPoint = userPointTable.selectById(id);
            if (existingUserPoint == null) {
                throw new IllegalArgumentException("해당 유저(" + id + ")는 존재하지 않습니다.");
            }
            return existingUserPoint;
        }, executor);
    }

    public CompletableFuture<UserPoint> usePoints(long id, long amount) {
        return CompletableFuture.supplyAsync(() -> {
            if (amount <= 0) {
                throw new IllegalArgumentException(ErrorCode.INVALID_OPERATION.getMessage());
            }
            UserPoint existingUserPoint = userPointTable.selectById(id);
            if (existingUserPoint == null) {
                throw new IllegalArgumentException("해당 유저(" + id + ")는 존재하지 않습니다.");
            }
            if (existingUserPoint.point() < amount) {
                throw new IllegalArgumentException(ErrorCode.INSUFFICIENT_BALANCE.getMessage());
            }

            long updatedPointsTotal = calculateReducedPoints(existingUserPoint.point(), amount);
            recordTransaction(id, amount, TransactionType.USE);
            return userPointTable.insertOrUpdate(id, updatedPointsTotal);
        }, executor);
    }

    public long calculateIncreasedPoints(long baseAmount, long chargeAmount) {
        return baseAmount + chargeAmount;
    }

    public long calculateReducedPoints(long baseAmount, long chargeAmount) {
        return baseAmount - chargeAmount;
    }

    public List<PointHistory> getHistory(long id) {
        List<PointHistory> history = pointHistoryTable.selectAllByUserId(id);
        if (history.isEmpty()) {
            throw new IllegalArgumentException("해당 유저(" + id + ")의 트랜잭션 내역이 없습니다.");
        }
        return history;
    }

    public void recordTransaction(long userId, long amount, TransactionType type) {
        long updateMillis = System.currentTimeMillis();
        pointHistoryTable.insert(userId, amount, type, updateMillis);
    }
}
