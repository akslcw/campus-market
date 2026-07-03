package com.campus.market.vo;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片上传结果 VO
 * @author 成员二
 */
@Data
@RequiredArgsConstructor
public class ImageVO {
    private final String url;
    private final String fileName;
    private final Long size;
}
