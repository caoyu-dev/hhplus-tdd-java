package io.hhplus.tdd.point;

import io.hhplus.tdd.point.dto.HistoryResponse;
import io.hhplus.tdd.point.dto.PointRequest;
import io.hhplus.tdd.point.dto.PointResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/point")
public class PointController {

    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}")
    public ResponseEntity<PointResponse> point(
            @PathVariable("id") long id
    ) {
        UserPoint userPoint = pointService.getPoint(id);
        return ResponseEntity.ok().body(PointResponse.of(userPoint));
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}/histories")
    public ResponseEntity<List<HistoryResponse>> history(
            @PathVariable("id") long id
    ) {
        List<PointHistory> pointHistories = pointService.getHistory(id);
        return ResponseEntity.ok().body(pointHistories.stream().map(HistoryResponse::of).toList());
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/charge")
    public CompletableFuture<ResponseEntity<PointResponse>> charge(
            @PathVariable("id") long id,
            @RequestBody PointRequest request
    ) {
        return pointService.chargePoints(id, request)
                .thenApply(updatedUserPoint -> ResponseEntity.ok().body(PointResponse.of(updatedUserPoint)));
    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/use")
    public CompletableFuture<ResponseEntity<PointResponse>> use(
            @PathVariable("id") long id,
            @RequestBody PointRequest request
    ) {
        return pointService.usePoints(id, request)
                .thenApply(updatedUserPoint -> ResponseEntity.ok().body(PointResponse.of(updatedUserPoint)));
    }
}
