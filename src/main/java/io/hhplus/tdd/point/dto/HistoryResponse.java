package io.hhplus.tdd.point.dto;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HistoryResponse {
    private long id;
    private long userId;
    private long amount;
    private TransactionType type;
    private long updateMillis;

    public static HistoryResponse of(final PointHistory pointHistory) {
        return HistoryResponse.builder()
                .id(pointHistory.id())
                .userId(pointHistory.userId())
                .amount(pointHistory.amount())
                .type(pointHistory.type())
                .updateMillis(pointHistory.updateMillis())
                .build();
    }
}
