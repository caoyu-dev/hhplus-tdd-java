package io.hhplus.tdd.point;

import io.hhplus.tdd.ErrorCode;
import io.hhplus.tdd.point.dto.PointRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class PointService {

    private static final long MAXIMUM_POINT_LIMIT = 50000L;
    private final PointRepository pointRepository;
    private final HistoryRepository historyRepository;
    private final ExecutorService executor;
    private final Lock lock = new ReentrantLock(); // 순차적으로 처리
    private static final Logger log = LoggerFactory.getLogger(PointService.class);

    public PointService(PointRepository pointRepository,
                        HistoryRepository historyRepository,
                        ExecutorService executor)
    {
        this.pointRepository = pointRepository;
        this.historyRepository = historyRepository;
        this.executor = executor;
    }

    public CompletableFuture<UserPoint> chargePoints(long id, PointRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            lock.lock(); // 작업 시작할 때 lock 걸고
            try {
                long amount = request.getAmount();

                if (amount <= 0) {
                    throw new IllegalArgumentException(ErrorCode.ADD_UNDER_VALUE_FAILED.getMessage());
                }
                UserPoint existingUserPoint = pointRepository.findByUserId(id);
                long updatedPoints = calculateIncreasedPoints(existingUserPoint.point(), amount);

                if (updatedPoints > MAXIMUM_POINT_LIMIT) {
                    throw new IllegalArgumentException("최대 50000 포인트까지 모을 수 있습니다.");
                }

                UserPoint updatedUserPoint = pointRepository.addPoint(id, updatedPoints);
                recordTransaction(id, amount, TransactionType.CHARGE);
                return updatedUserPoint;
            } finally {
                lock.unlock();  // 작업 완료 후 lock 빼기
            }
        }, executor);
    }

    public UserPoint getPoint(long id) {
        UserPoint existingUserPoint = pointRepository.findByUserId(id);
        if (existingUserPoint == null) {
            throw new IllegalArgumentException("해당 유저(" + id + ")는 존재하지 않습니다.");
        }
        return existingUserPoint;
    }

    public CompletableFuture<UserPoint> usePoints(long id, PointRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            lock.lock();
            try {
                long amount = request.getAmount();

                if (amount <= 0) {
                    throw new IllegalArgumentException(ErrorCode.INVALID_OPERATION.getMessage());
                }
                UserPoint existingUserPoint = pointRepository.findByUserId(id);
                if (existingUserPoint == null) {
                    throw new IllegalArgumentException("해당 유저(" + id + ")는 존재하지 않습니다.");
                }
                if (existingUserPoint.point() < amount) {
                    throw new IllegalArgumentException(ErrorCode.INSUFFICIENT_BALANCE.getMessage());
                }

                long updatedPointsTotal = calculateReducedPoints(existingUserPoint.point(), amount);
                recordTransaction(id, amount, TransactionType.USE);
                return pointRepository.addPoint(id, updatedPointsTotal);
            } finally {
                lock.unlock();
            }
        }, executor);
    }

    public long calculateIncreasedPoints(long baseAmount, long chargeAmount) {
        return baseAmount + chargeAmount;
    }

    public long calculateReducedPoints(long baseAmount, long chargeAmount) {
        return baseAmount - chargeAmount;
    }

    public List<PointHistory> getHistory(long id) {
        List<PointHistory> history = historyRepository.selectAllByUserId(id);
        if (history.isEmpty()) {
            throw new IllegalArgumentException("해당 유저(" + id + ")의 트랜잭션 내역이 없습니다.");
        }
        return history;
    }

    public void recordTransaction(long userId, long amount, TransactionType type) {
        long updateMillis = System.currentTimeMillis();
        historyRepository.addHistory(userId, amount, type, updateMillis);
    }
}
