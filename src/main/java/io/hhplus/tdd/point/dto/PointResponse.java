package io.hhplus.tdd.point.dto;

import io.hhplus.tdd.point.UserPoint;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointResponse {
    private long id;
    private long point;
    private long updateMillis;

    public static PointResponse of(final UserPoint userPoint) {
        return PointResponse.builder()
                .id(userPoint.id())
                .point(userPoint.point())
                .updateMillis(userPoint.updateMillis())
                .build();
    }
}
