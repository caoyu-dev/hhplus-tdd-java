package io.hhplus.tdd.point;

import io.hhplus.tdd.BaseException;
import io.hhplus.tdd.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PointService {

    private final PointRepository pointRepository;
    private static final Logger log = LoggerFactory.getLogger(PointService.class);

    public PointService(PointRepository PointRepository) {
        this.pointRepository = PointRepository;
    }

    public UserPoint chargePoints(long id, long amount) {
        if (amount <= 0) {
            throw new BaseException(ErrorCode.ADD_UNDER_VALUE_FAILED);
        }
        UserPoint findUserPoint = pointRepository.findById(id)
                .orElse(UserPoint.empty(id));

        long updatedPoints = updatePoints(findUserPoint.point(), amount);
        UserPoint updatedUserPoint = new UserPoint(id, updatedPoints, System.currentTimeMillis());
        pointRepository.save(updatedUserPoint);

        return updatedUserPoint;
    }

    public long updatePoints(long baseAmount,long chargeAmount) {
        return baseAmount + chargeAmount;
    }
}
