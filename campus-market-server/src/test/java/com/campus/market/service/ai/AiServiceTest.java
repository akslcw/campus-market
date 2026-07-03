package com.campus.market.service.ai;

import com.campus.market.common.exception.BusinessException;
import com.campus.market.vo.ContentCheckVO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiServiceTest {

    @Test
    void reportsAiServiceErrorWhenDescriptionOptimizationFails() {
        DeepSeekClient client = mock(DeepSeekClient.class);
        when(client.chat(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()))
                .thenThrow(new RuntimeException("timeout"));

        BusinessException error = assertThrows(BusinessException.class,
                () -> new AiService(client).optimizeDescription("二手椅子", "有点瑕疵"));

        assertEquals(3001, error.getCode());
    }

    @Test
    void marksReviewCompletedWhenAiReturnsValidResult() {
        DeepSeekClient client = mock(DeepSeekClient.class);
        when(client.chat(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()))
                .thenReturn("{\"violation\":false,\"reason\":\"\",\"category\":\"\"}");

        ContentCheckVO result = new AiService(client)
                .checkContent("农夫山泉", "未开封饮用水");

        assertTrue(result.isReviewCompleted());
    }

    @Test
    void marksReviewIncompleteWhenAiCallFails() {
        DeepSeekClient client = mock(DeepSeekClient.class);
        when(client.chat(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()))
                .thenThrow(new RuntimeException("timeout"));

        ContentCheckVO result = new AiService(client)
                .checkContent("农夫山泉", "未开封饮用水");

        assertFalse(result.isReviewCompleted());
        assertFalse(result.getViolation());
    }
}
