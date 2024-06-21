package io.hhplus.tdd.repository;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static io.hhplus.tdd.point.TransactionType.USE;

@Repository
@RequiredArgsConstructor
public class PointRepositoryImpl implements PointRepository {

    private final PointHistoryTable pointHistoryTable;
    private final UserPointTable userPointTable;

    // 포인트 조회
    public UserPoint getPoint(long id){
        return userPointTable.selectById(id);
    }

    // 포인트 충전내역, 이용내역 조회
    public List<PointHistory> getHistories(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

    // 포인트 충전
    public UserPoint chargePoints(long id, long amount) {
        return updatePoints(id, amount, CHARGE);
    }

    // 포인트 사용
    public UserPoint usePoints(long id, long amount) {
        return updatePoints(id, amount, USE);
    }

    // 포인트 충전 / 사용 처리
    public UserPoint updatePoints(long id, long amount, TransactionType Type) {
        UserPoint userPoint =  userPointTable.selectById(id);
        long newAmount = Type==USE ? userPoint.point()-amount : userPoint.point()+amount;
        userPoint = userPointTable.insertOrUpdate(id, newAmount);
        pointHistoryTable.insert(id, amount, Type, 0);
        return userPoint;
    }

}
