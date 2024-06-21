package io.hhplus.tdd.service;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;

    // 포인트 조회
    public UserPoint getPoint(long userId){
        return pointRepository.getPoint(userId);
    }

    // 포인트 충전내역, 이용내역 조회
    public List<PointHistory> getHistories(long userId) {
        List<PointHistory> pointHistoryList = pointRepository.getHistories(userId);
        if(pointHistoryList.isEmpty()) throw new RuntimeException("포인트를 사용하거나 충전한 내역이 없습니다.");
        return pointHistoryList;
    }

    // 포인트 충전
    public UserPoint chargePoints(long userId, long amount) {
        if(amount <= 0) throw new RuntimeException("포인트 충전은 1 이상만 가능합니다.");
        return pointRepository.chargePoints(userId, amount);
    }

    // 포인트 사용
    public UserPoint usePoints(long userId, long amount) {
        if(amount==0) throw new RuntimeException("사용 포인트는 0 이상이어야 합니다.");

        long currentAmount = getPoint(userId).point();
        if (currentAmount < amount) throw new RuntimeException("보유 포인트가 부족합니다.");

        return pointRepository.usePoints(userId, amount);
    }

}
