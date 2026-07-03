package com.campus.market.controller;

import com.campus.market.common.Result;
import com.campus.market.service.ai.AiService;
import com.campus.market.vo.ContentCheckVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * AI 接口 — IF-09 / IF-10
 * @author 成员一
 */
@Tag(name = "AI 模块", description = "IF-09 描述优化 / IF-10 违规检测(内部调用)")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @Operation(summary = "IF-09 描述一键优化",
               description = "用户输入简单描述，AI 自动生成规范的商品详情文案(50-100字)")
    @PostMapping("/optimize")
    public Result<String> optimize(@RequestBody OptimizeReq req) {
        String result = aiService.optimizeDescription(req.getTitle(), req.getDescription());
        return Result.success(result);
    }

    @Operation(summary = "IF-10 违规内容检测",
               description = "发布前自动检测违规品/色情/暴恐/学术作弊/隐私/欺诈等内容。IF-06发布时内置调用，前端亦可独立调用做前置校验")
    @PostMapping("/check")
    public Result<ContentCheckVO> check(@RequestBody CheckReq req) {
        ContentCheckVO vo = aiService.checkContent(req.getTitle(), req.getDescription());
        return Result.success(vo);
    }

    @Data
    @Schema(description = "AI优化请求")
    public static class OptimizeReq {
        @Schema(description = "商品标题", example = "出二手iPhone")
        private String title;
        @NotBlank(message = "描述不能为空")
        @Schema(description = "原始描述", example = "用了一年，有点划痕，便宜出了")
        private String description;
    }

    @Data
    @Schema(description = "AI审核请求")
    public static class CheckReq {
        @Schema(description = "商品标题")
        private String title;
        @Schema(description = "商品描述")
        private String description;
    }
}
