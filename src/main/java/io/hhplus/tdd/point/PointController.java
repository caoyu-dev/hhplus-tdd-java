package io.hhplus.tdd.point;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> point(
            @PathVariable long id
    ) {
        return ResponseEntity.ok().body(pointService.getPoint(id));
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}/histories")
    public ResponseEntity<?> history(
            @PathVariable long id
    ) {
        return ResponseEntity.ok().body(pointService.getHistory(id));
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/charge")
    public CompletableFuture<ResponseEntity<?>> charge(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        return pointService.chargePoints(id, amount)
                .thenApply(updatedUserPoint -> ResponseEntity.ok().body(updatedUserPoint));
    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/use")
    public CompletableFuture<ResponseEntity<?>> use(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        return pointService.usePoints(id, amount)
                .thenApply(updatedUserPoint -> ResponseEntity.ok().body(updatedUserPoint));
    }
}
