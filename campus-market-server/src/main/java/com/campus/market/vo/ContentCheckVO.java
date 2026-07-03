package com.campus.market.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

// 内容审核结果
@Data
@NoArgsConstructor
public class ContentCheckVO {
    /** 是否违规 */
    private Boolean violation;
    /** 违规原因 */
    private String reason;
    /** 违规类别 */
    private String category;

    /** AI 是否成功完成审核，仅供内部状态流转使用。 */
    @JsonIgnore
    private boolean reviewCompleted;

    public static ContentCheckVO pass() {
        ContentCheckVO result = new ContentCheckVO();
        result.setViolation(false);
        result.setReason("");
        result.setCategory("");
        result.setReviewCompleted(true);
        return result;
    }

    public static ContentCheckVO fallbackPass() {
        ContentCheckVO result = pass();
        result.setReviewCompleted(false);
        return result;
    }
}
