package io.hhplus.tdd.point;

import io.hhplus.tdd.BaseException;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @Mock
    private UserPointTable userPointTable;

    @InjectMocks
    private PointService pointService;

    @BeforeEach
    void setUp() {
        Mockito.reset(userPointTable);
    }

    @Test
    void test_add_0_point_userExists() {
        long userId = 1L;
        long chargeAmount = 0L;

        assertThrows(BaseException.class, () -> {
            pointService.chargePoints(userId, chargeAmount);
        }, "포인트는 0보다 큰 값이어야 합니다.");
    }

    @Test
    void test_add_minus_point_userExists() {
        long userId = 1L;
        long chargeAmount = -100L;

        assertThrows(BaseException.class, () -> {
            pointService.chargePoints(userId, chargeAmount);
        }, "포인트는 0보다 큰 값이어야 합니다.");
    }

    @Test
    void test_update_Points() {
        long initialPoints = 100L;
        long chargeAmount = 50L;
        long updatedPoints = pointService.updatePoints(initialPoints, chargeAmount);
        assertEquals(initialPoints + chargeAmount, updatedPoints);
    }

    @Test
    void test_charge_points_userExists() {
        long userId = 1L;
        long initialPoints = 100L;
        long chargeAmount = 50L;
        UserPoint existingUserPoint = new UserPoint(userId, initialPoints, System.currentTimeMillis());
        UserPoint updatedUserPoint = new UserPoint(userId, initialPoints + chargeAmount, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(existingUserPoint);
        when(userPointTable.insertOrUpdate(userId, initialPoints + chargeAmount)).thenReturn(updatedUserPoint);
        UserPoint result = pointService.chargePoints(userId, chargeAmount);

        assertEquals(initialPoints + chargeAmount, result.point());
        verify(userPointTable).insertOrUpdate(userId, initialPoints + chargeAmount);
    }

    @Test
    void test_charge_points_userDoesNotExist() {
        long userId = 2L;
        long chargeAmount = 100L;
        UserPoint newPoint = new UserPoint(userId, chargeAmount, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(UserPoint.empty(userId));
        when(userPointTable.insertOrUpdate(userId, chargeAmount)).thenReturn(newPoint);

        UserPoint result = pointService.chargePoints(userId, chargeAmount);

        assertEquals(chargeAmount, result.point());
        verify(userPointTable).insertOrUpdate(userId, chargeAmount);
    }
}
