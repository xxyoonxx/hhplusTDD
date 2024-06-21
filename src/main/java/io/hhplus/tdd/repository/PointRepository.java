package io.hhplus.tdd.repository;

import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;

import java.util.List;

public interface PointRepository {

    UserPoint getPoint(long id);

    List<PointHistory> getHistories(long userId);

    UserPoint chargePoints(long id, long amount);

    UserPoint usePoints(long id, long amount);

}