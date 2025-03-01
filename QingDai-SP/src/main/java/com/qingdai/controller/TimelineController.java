package com.qingdai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingdai.entity.Timeline;
import com.qingdai.service.TimelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-03-01
 */
@Tag(name = "时间线管理", description = "时间线相关操作接口")
@RestController
@RequestMapping("/timeline")
public class TimelineController {

    @Autowired
    private TimelineService timelineService;

    @GetMapping("/getAllTimelines")
    @Operation(summary = "获取全部时间线信息(时间倒叙)", description = "从数据库获取所有时间线的详细信息(时间倒叙)")
    public ResponseEntity<List<Timeline>> getAllTimelines() {
        try {
            // 1. 使用MyBatis Plus的list方法获取所有记录
            List<Timeline> timelines = timelineService.list(
                    new LambdaQueryWrapper<Timeline>()
                            .orderByDesc(Timeline::getTime) // 按时间倒序
            );

            // 2. 处理空数据集情况
            if (timelines == null || timelines.isEmpty()) {
                // 返回空数据的响应，自动使用404状态码
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }

            // 3. 返回成功结果，自动使用200状态码
            return ResponseEntity.ok().body(timelines);

        } catch (Exception e) {
            // 4. 异常处理
            e.printStackTrace();
            // 返回500状态码并自动使用错误信息
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}