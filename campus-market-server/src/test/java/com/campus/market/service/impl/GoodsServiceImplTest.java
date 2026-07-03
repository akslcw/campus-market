package com.campus.market.service.impl;

import com.campus.market.common.exception.BusinessException;
import com.campus.market.dto.GoodsCreateDTO;
import com.campus.market.entity.Goods;
import com.campus.market.entity.User;
import com.campus.market.mapper.GoodsMapper;
import com.campus.market.service.GoodsImageService;
import com.campus.market.service.UserService;
import com.campus.market.service.ai.AiService;
import com.campus.market.vo.ContentCheckVO;
import com.campus.market.vo.GoodsDetailVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GoodsServiceImplTest {

    private GoodsMapper goodsMapper;
    private GoodsImageService goodsImageService;
    private UserService userService;
    private AiService aiService;
    private GoodsServiceImpl goodsService;
    private Goods savedGoods;

    @BeforeEach
    void setUp() {
        goodsMapper = mock(GoodsMapper.class, Answers.RETURNS_DEFAULTS);
        goodsImageService = mock(GoodsImageService.class);
        userService = mock(UserService.class);
        aiService = mock(AiService.class);
        goodsService = spy(new GoodsServiceImpl(goodsImageService, userService, aiService));
        ReflectionTestUtils.setField(goodsService, "baseMapper", goodsMapper);

        User seller = new User();
        seller.setId(16L);
        seller.setUsername("user1");
        when(userService.getById(16L)).thenReturn(seller);

        doAnswer(invocation -> {
            savedGoods = invocation.getArgument(0);
            savedGoods.setId(13L);
            return 1;
        }).when(goodsMapper).insert(any(Goods.class));
        doReturn(new GoodsDetailVO()).when(goodsService).getDetail(13L);
    }

    @Test
    void publishesAsApprovedWhenAiReviewCompletesAndPasses() {
        ContentCheckVO result = ContentCheckVO.pass();
        result.setReviewCompleted(true);
        when(aiService.checkContent("农夫山泉", "未开封饮用水")).thenReturn(result);

        goodsService.publish(goodsDto(), 16L);

        assertEquals(GoodsServiceImpl.AUDIT_APPROVED, savedGoods.getAuditStatus());
        verify(goodsImageService).saveImages(13L, List.of("/static/water.jpg"));
    }

    @Test
    void publishesAsPendingWhenAiReviewFallsBack() {
        ContentCheckVO result = ContentCheckVO.pass();
        result.setReviewCompleted(false);
        when(aiService.checkContent("农夫山泉", "未开封饮用水")).thenReturn(result);

        goodsService.publish(goodsDto(), 16L);

        assertEquals(GoodsServiceImpl.AUDIT_PENDING, savedGoods.getAuditStatus());
    }

    @Test
    void rejectsPublishWhenAiFindsViolation() {
        ContentCheckVO result = new ContentCheckVO();
        result.setViolation(true);
        result.setReason("违禁品");
        result.setCategory("违禁品");
        result.setReviewCompleted(true);
        when(aiService.checkContent("农夫山泉", "未开封饮用水")).thenReturn(result);

        assertThrows(BusinessException.class, () -> goodsService.publish(goodsDto(), 16L));

        verify(goodsMapper, never()).insert(any(Goods.class));
    }

    private GoodsCreateDTO goodsDto() {
        GoodsCreateDTO dto = new GoodsCreateDTO();
        dto.setTitle("农夫山泉");
        dto.setDescription("未开封饮用水");
        dto.setPrice(new BigDecimal("2.00"));
        dto.setOriginalPrice(new BigDecimal("2.00"));
        dto.setCategoryId(7);
        dto.setCategoryName("食品饮料");
        dto.setImages(List.of("/static/water.jpg"));
        return dto;
    }
}
