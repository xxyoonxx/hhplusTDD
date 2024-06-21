package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.repository.PointRepository;
import io.hhplus.tdd.repository.PointRepositoryImpl;
import io.hhplus.tdd.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static io.hhplus.tdd.point.TransactionType.USE;
import static org.junit.jupiter.api.Assertions.*;

public class PointServiceTest {

    private PointService pointService;

    @BeforeEach
    void setUp(){
        PointRepository pointRepository = new PointRepositoryImpl(new PointHistoryTable(), new UserPointTable());
        pointService = new PointService(pointRepository);
    }

    // 단순 조회
    @Test
    @DisplayName("아이디로 포인트를 조회한다.")
    void getPointById(){
        pointService.getPoint(1);
        assertEquals(1, pointService.getPoint(1).id());
        assertEquals(0, pointService.getPoint(1).point());
    }

    @Test
    @DisplayName("특정 유저의 포인트 충전 내역을 조회한다")
    void getHistoryById() {
        pointService.chargePoints(1, 100);
        assertEquals(CHARGE, pointService.getHistories(1).get(0).type());
        assertEquals(100, pointService.getHistories(1).get(0).amount());
    }

    @Test
    @DisplayName("1. 특정 유저의 포인트를 충전한다. / 2. 포인트 충전 시 충전 내역을 저장한다.")
    void chargePointById(){
        // when
        pointService.chargePoints(1,1000);
        // then
        assertEquals(1000, pointService.getHistories(1).get(0).amount());
        assertEquals(CHARGE, pointService.getHistories(1).get(0).type());
    }

    @Test
    @DisplayName("1. 특정 유저가 포인트를 사용한다. / 2. 포인트 사용 시 사용 내역을 저장한다.")
    void usePoint() {
        // given
        pointService.chargePoints(1,1000);
        // when
        pointService.usePoints(1, 100);
        // then
        assertEquals(100, pointService.getHistories(1).get(1).amount());
        assertEquals(USE, pointService.getHistories(1).get(1).type());
        assertEquals(900, pointService.getPoint(1).point());
    }

    // 예외 처리
    @Test
    @DisplayName("1. 사용할 포인트가 0이다. / 2. 보유 포인트보다 사용 포인트가 더 많다.")
    void usePointsExceptionTest() {
        // 1. 사용 포인트가 0이다.
        // when
        RuntimeException exception = assertThrows(RuntimeException.class,()->pointService.usePoints(1, 0));
        // then
        assertEquals("사용 포인트는 0 이상이어야 합니다.", exception.getMessage());

        // 2. 보유 포인트보다 사용 포인트가 더 많다.
        // given
        pointService.chargePoints(1,100);
        pointService.chargePoints(1,100);
        // when
        exception = assertThrows(RuntimeException.class,()->pointService.usePoints(1, 1000));
        // then
        assertEquals("보유 포인트가 부족합니다.", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    @DisplayName("충전 포인트가 0 또는 음수이다.")
    void wrongPointUnit(long amount){
        RuntimeException exception = assertThrows(RuntimeException.class,()->pointService.chargePoints(1, amount));
        assertEquals("포인트 충전은 1 이상만 가능합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("1. 충전 내역이 없다. / 2. 이용 내역이 없다.")
    void chargePointsExceptionTest(){
        RuntimeException exception = assertThrows(RuntimeException.class, () -> pointService.getHistories(1));
        assertEquals("포인트를 사용하거나 충전한 내역이 없습니다.",exception.getMessage());
    }

}
