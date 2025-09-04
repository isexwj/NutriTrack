/**
 * 文件路径: src/main/java/com/myproject/dto/DailyAnalysisRequestDTO.java
 * 说明: 前端可能传入的请求参数封装（可选：controller 也接收 request param）
 */

package com.myproject.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyAnalysisRequestDTO {
    private Long userId;
    private LocalDate date;
}
