package io.hhplus.tdd;

import io.hhplus.tdd.point.PointController;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static io.hhplus.tdd.point.TransactionType.CHARGE;
import static io.hhplus.tdd.point.TransactionType.USE;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PointController.class)
public class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    long id = 1L;

    @Test
    @DisplayName("유저 포인트 조회")
    void getUserInfo() throws Exception {
        UserPoint userPoint = new UserPoint(id,0,0);
        when(pointService.getPoint(id)).thenReturn(userPoint);

        mockMvc.perform(get("/point/{id}",id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.point").value(0));
    }

    @Test
    @DisplayName("포인트 충전/사용 이력 확인")
    void getPointHistories() throws Exception {
        List<PointHistory> pointHistoryList = List.of(new PointHistory(1, 1L, 10L, CHARGE, System.currentTimeMillis())
                                                    ,new PointHistory(1, 1L, 5L, USE, System.currentTimeMillis()));

        // 충전 이력 확인
        when(pointService.getHistories(id)).thenReturn(pointHistoryList);

        mockMvc.perform(get("/point/{id}/histories", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(id))
                .andExpect(jsonPath("$[0].amount").value(10L))
                .andExpect(jsonPath("$[1].amount").value(5L));
    }

    @Test
    @DisplayName("포인트 충전")
    void chargePoints() throws Exception {
        // 포인트 충전
        UserPoint userPoint = new UserPoint(1L, 10L, System.currentTimeMillis());
        when(pointService.chargePoints(id,10L)).thenReturn(userPoint);
        mockMvc.perform(patch("/point/{id}/charge",id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(10L)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("포인트 사용")
    void usePoints() throws Exception {
        UserPoint userPoint = new UserPoint(1L, 10L, System.currentTimeMillis());
        when(pointService.usePoints(id, 5L)).thenReturn(userPoint);

        mockMvc.perform(patch("/point/{id}/use",id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(5L)))
                .andExpect(status().isOk());
    }

}
