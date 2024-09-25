package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    private ExecutorService executor;

    @InjectMocks
    private PointService pointService;

    @BeforeEach
    void setUp() {
        executor = Executors.newFixedThreadPool(1);
        pointService = new PointService(userPointTable, pointHistoryTable, executor);
        Mockito.reset(userPointTable, pointHistoryTable);
    }

    @Test
    void test_add_0_point_userExists() {
        long userId = 1L;
        long chargeAmount = 0L;
        CompletableFuture<UserPoint> future = pointService.chargePoints(userId, chargeAmount);

        CompletionException exception = assertThrows(CompletionException.class, future::join);
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("포인트는 0보다 큰 값이어야 합니다.", exception.getCause().getMessage());
    }

    @Test
    void test_add_minus_point_userExists() {
        long userId = 1L;
        long chargeAmount = -100L;
        CompletableFuture<UserPoint> future = pointService.chargePoints(userId, chargeAmount);

        CompletionException exception = assertThrows(CompletionException.class, future::join);
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("포인트는 0보다 큰 값이어야 합니다.", exception.getCause().getMessage());
    }

    @Test
    @DisplayName("포인트 증가 메서드 테스트")
    void test_update_Points() {
        long initialPoints = 100L;
        long chargeAmount = 50L;
        long updatedPoints = pointService.calculateIncreasedPoints(initialPoints, chargeAmount);
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

        CompletableFuture<UserPoint> future = pointService.chargePoints(userId, chargeAmount);
        UserPoint result = future.join();

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

        CompletableFuture<UserPoint> future = pointService.chargePoints(userId, chargeAmount);
        UserPoint result = future.join();

        assertEquals(chargeAmount, result.point());
        verify(userPointTable).insertOrUpdate(userId, chargeAmount);
    }

    @Test
    void test_get_point_userExists() {
        long userId = 1L;
        UserPoint existingUserPoint = new UserPoint(userId, 100L, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(existingUserPoint);

        CompletableFuture<UserPoint> future = pointService.getPoint(userId);
        UserPoint result = future.join();

        assertNotNull(result);
        assertEquals(100L, result.point());
        verify(userPointTable).selectById(userId);
    }

    @Test
    void test_get_point_userNotFound() {
        long userId = 999L;

        when(userPointTable.selectById(userId)).thenReturn(null);
        CompletableFuture<UserPoint> future = pointService.getPoint(userId);

        CompletionException exception = assertThrows(CompletionException.class, future::join);
        assertTrue(exception.getCause() instanceof  IllegalArgumentException);
        assertEquals("해당 유저(" + userId + ")는 존재하지 않습니다.", exception.getCause().getMessage());
    }

    @Test
    void test_use_points_userExistsAndSufficientBalance() {
        long userId = 1L;
        long initialPoints = 150L;
        long useAmount = 50L;
        UserPoint existingUserPoint = new UserPoint(userId, initialPoints, System.currentTimeMillis());
        UserPoint updatedUserPoint = new UserPoint(userId, initialPoints - useAmount, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(existingUserPoint);
        when(userPointTable.insertOrUpdate(userId, initialPoints - useAmount)).thenReturn(updatedUserPoint);

        CompletableFuture<UserPoint> future = pointService.usePoints(userId, useAmount);
        UserPoint result = future.join();

        assertEquals(initialPoints - useAmount, result.point());
        verify(userPointTable).insertOrUpdate(userId, initialPoints - useAmount);
    }

    @Test
    void test_use_points_insufficientBalance() {
        long userId = 1L;
        long initialPoints = 30L;
        long useAmount = 50L;
        UserPoint existingUserPoint = new UserPoint(userId, initialPoints, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(existingUserPoint);
        CompletableFuture<UserPoint> future = pointService.usePoints(userId, useAmount);

        CompletionException exception = assertThrows(CompletionException.class, future::join);
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("사용할 포인트가 충분하지 않습니다.", exception.getCause().getMessage());
    }

    @Test
    void test_use_points_userNotFound() {
        long userId = 999L;
        long useAmount = 100L;

        when(userPointTable.selectById(userId)).thenReturn(null);
        CompletableFuture<UserPoint> future = pointService.usePoints(userId, useAmount);

        CompletionException exception = assertThrows(CompletionException.class, future::join);
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("해당 유저(" + userId + ")는 존재하지 않습니다.", exception.getCause().getMessage());
    }

    @Test
    void test_get_history_userExists() {
        long userId = 1L;
        List<PointHistory> historyList = new ArrayList<>();
        historyList.add(new PointHistory(1L, userId, 100L, TransactionType.CHARGE, System.currentTimeMillis()));
        historyList.add(new PointHistory(2L, userId, 50L, TransactionType.USE, System.currentTimeMillis()));

        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(historyList);

        List<PointHistory> result = pointService.getHistory(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(TransactionType.CHARGE, result.get(0).type());
        assertEquals(TransactionType.USE, result.get(1).type());

        verify(pointHistoryTable).selectAllByUserId(userId);
    }

    @Test
    void test_get_history_userHasNoHistory() {
        long userId = 1L;

        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(new ArrayList<>());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pointService.getHistory(userId);
        });

        assertEquals("해당 유저(" + userId + ")의 트랜잭션 내역이 없습니다.", exception.getMessage());
        verify(pointHistoryTable).selectAllByUserId(userId);
    }
}
