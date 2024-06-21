package io.hhplus.tdd;

import io.hhplus.tdd.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ConcurrencyTest {

    @Autowired
    PointService pointService;

    @Test
    @DisplayName("한번에 여러 포인트가 충전됨.")
    void chargeMultiplePoints() throws InterruptedException {
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
    void useMultiplePoints() {
        pointService.chargePoints(1L,10000L);

        CompletableFuture.allOf(
            CompletableFuture.runAsync(() ->{
                pointService.usePoints(1L, 5000L);
            }),
            CompletableFuture.runAsync(() ->{
                pointService.usePoints(1L, 3000L);
            }),
            CompletableFuture.runAsync(() ->{
                pointService.usePoints(1L, 2000L);
            })).join();

        assertEquals(0, pointService.getPoint(1).point()); // 최종 포인트 합산 검증

    }


    @Test
    @DisplayName("충전/사용 경합")
    void useMultipleFunction(){
        pointService.chargePoints(1L, 10000L);

        CompletableFuture.allOf(
            CompletableFuture.runAsync(() ->{
                pointService.usePoints(1L, 5000L);
            }),
            CompletableFuture.runAsync(() ->{
                pointService.chargePoints(1L, 3000L);
            }),
            CompletableFuture.runAsync(() ->{
                pointService.usePoints(1L, 1000L);
            }),
            CompletableFuture.runAsync(() ->{
                pointService.usePoints(1L, 1000L);
            }),
            CompletableFuture.runAsync(() ->{
                pointService.chargePoints(1L, 2000L);
            })
        ).join();

        // then
        assertEquals(10000L-5000L+3000L-1000L-1000L+2000L, pointService.getPoint(1).point(), 0);
    }

}
