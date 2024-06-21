package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.repository.PointRepository;
import io.hhplus.tdd.repository.PointRepositoryImpl;
import io.hhplus.tdd.service.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ConcurrencyTest {

    PointService pointService;

    @BeforeEach
    void setUp(){
        PointRepository pointRepository = new PointRepositoryImpl(new PointHistoryTable(), new UserPointTable());
        pointService = new PointService(pointRepository);
    }

    @Test
    @DisplayName("한번에 여러 포인트가 충전됨.")
    void ChargeMultiplePoints() throws InterruptedException {

        final int threadCount = 3;
        final long incrementsPerThread = 2L;

        CountDownLatch startLatch = new CountDownLatch(threadCount);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.countDown();
                    startLatch.await();
                    for (int j = 0; j < incrementsPerThread; j++) {
                        pointService.chargePoints(1L, 2L);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        doneLatch.await();

        assertEquals(12, pointService.getPoint(1L).point());
        executor.shutdown();
    }

    @Test
    @DisplayName("한번에 여러 포인트가 사용됨.")
    void useMultiplePoints() throws InterruptedException {
        pointService.chargePoints(1,10000L);

        CompletableFuture.allOf(
            CompletableFuture.runAsync(() ->{
                try {
                    pointService.usePoints(1, 5000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }),
            CompletableFuture.runAsync(() ->{
                try {
                    pointService.usePoints(1, 3000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }),
            CompletableFuture.runAsync(() ->{
                try {
                    pointService.usePoints(1, 2000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            })).join();

        assertEquals(pointService.getPoint(1).point(), 0); // 최종 포인트 합산 검증

    }

}
