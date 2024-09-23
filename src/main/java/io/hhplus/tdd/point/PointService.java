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
        UserPoint findUserPoint = userPointTable.selectById(id);
        long updatedPoints = updatePoints(findUserPoint.point(), amount);
        UserPoint updatedUserPoint = userPointTable.insertOrUpdate(id, updatedPoints);

        return updatedUserPoint;
    }

    public long updatePoints(long baseAmount,long chargeAmount) {
        return baseAmount + chargeAmount;
    }
}
