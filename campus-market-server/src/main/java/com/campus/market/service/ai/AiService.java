package com.campus.market.service.ai;

import com.campus.market.common.ResultCode;
import com.campus.market.common.exception.BusinessException;
import com.campus.market.service.ai.prompt.CheckPrompts;
import com.campus.market.service.ai.prompt.OptimizePrompts;
import com.campus.market.vo.ContentCheckVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final DeepSeekClient deepSeekClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 描述一键优化：失败时明确返回 AI 服务错误，避免前端误报成功。 */
    public String optimizeDescription(String title, String rawDescription) {
        if (rawDescription == null || rawDescription.isBlank()) {
            return rawDescription;
        }
        long start = System.currentTimeMillis();
        try {
            String result = deepSeekClient.chat(
                    OptimizePrompts.SYSTEM,
                    OptimizePrompts.userPrompt(title, rawDescription)
            );
            String out = result == null ? rawDescription : result.trim();
            log.info("[AI优化] 成功 | 入参{}字 -> 出参{}字 | 耗时{}ms",
                    rawDescription.length(), out.length(), System.currentTimeMillis()
                            - start);
            return out;
        } catch (Exception e) {
            log.warn("[AI优化] 失败 | 耗时{}ms | 原因: {}",
                    System.currentTimeMillis() - start, e.getMessage());
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR);
        }
    }

    /**
     * 违规内容检测：失败时降级放行（violation=false），交由管理员人工审核兜底。
     */
    public ContentCheckVO checkContent(String title, String description) {
        if ((title == null || title.isBlank()) && (description == null ||
                description.isBlank())) {
            return ContentCheckVO.pass();
        }
        long start = System.currentTimeMillis();
        try {
            String json = deepSeekClient.chat(
                    CheckPrompts.SYSTEM,
                    CheckPrompts.userPrompt(title, description)
            );
            String cleaned = stripCodeFence(json).trim();
            ContentCheckVO vo = objectMapper.readValue(cleaned,
                    ContentCheckVO.class);
            if (vo.getViolation() == null) vo.setViolation(false);
            if (vo.getReason() == null) vo.setReason("");
            if (vo.getCategory() == null) vo.setCategory("");
            vo.setReviewCompleted(true);
            log.info("[AI审核] 成功 | violation={} reason={} | 耗时{}ms",
                    vo.getViolation(), vo.getReason(), System.currentTimeMillis() -
                            start);
            return vo;
        } catch (Exception e) {
            log.warn("[AI审核] 降级(放行交人工审核) | 耗时{}ms | 原因: {}",
                    System.currentTimeMillis() - start, e.getMessage());
            return ContentCheckVO.fallbackPass();
        }
    }

    private String stripCodeFence(String text) {
        if (text == null) return "";
        String s = text.trim();
        if (s.startsWith("```")) {
            int firstNl = s.indexOf('\n');
            if (firstNl >= 0) s = s.substring(firstNl + 1);
            if (s.endsWith("```")) s = s.substring(0, s.length() - 3);
        }
        return s.trim();
    }
}
