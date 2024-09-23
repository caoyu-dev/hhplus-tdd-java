package io.hhplus.tdd.point;

import io.hhplus.tdd.BaseException;
import io.hhplus.tdd.ErrorCode;
import io.hhplus.tdd.database.UserPointTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PointService {

    private final UserPointTable userPointTable;
    private static final Logger log = LoggerFactory.getLogger(PointService.class);

    public PointService(UserPointTable userPointTable) {
        this.userPointTable = userPointTable;
    }

    public UserPoint chargePoints(long id, long amount) {
        if (amount <= 0) {
            throw new BaseException(ErrorCode.ADD_UNDER_VALUE_FAILED);
        }
        UserPoint existingUserPoint = userPointTable.selectById(id);
        long updatedPoints = calculateIncreasedPoints(existingUserPoint.point(), amount);
        UserPoint updatedUserPoint = userPointTable.insertOrUpdate(id, updatedPoints);

        return updatedUserPoint;
    }

    public UserPoint getPoint(long id) {
        UserPoint existingUserPoint = userPointTable.selectById(id);
        if (existingUserPoint == null) {
            throw new BaseException(ErrorCode.USER_NOT_FOUND, "해당 유저(" + id + ") 는 존재하지 않습니다.");
        }
        return existingUserPoint;
    }

    public UserPoint usePoints(long id, long amount) {
        if (amount <= 0) {
            throw new BaseException(ErrorCode.INVALID_OPERATION, "사용할 포인트는 0보다 커야 합니다.");
        }
        UserPoint existingUserPoint = userPointTable.selectById(id);
        if (existingUserPoint == null) {
            throw new BaseException(ErrorCode.USER_NOT_FOUND, "해당 유저(" + id + ")는 존재하지 않습니다.");
        }
        if (existingUserPoint.point() < amount) {
            throw new BaseException(ErrorCode.INSUFFICIENT_BALANCE, "사용할 포인트가 충분하지 않습니다.");
        }

        long updatedPointsTotal = calculateReducedPoints(existingUserPoint.point(), amount);
        return userPointTable.insertOrUpdate(id, updatedPointsTotal);
    }

    public long calculateIncreasedPoints(long baseAmount, long chargeAmount) {
        return baseAmount + chargeAmount;
    }

    public long calculateReducedPoints(long baseAmount, long chargeAmount) {
        return baseAmount - chargeAmount;
    }
}
