package com.qingdai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingdai.dto.TimelineDTO;
import com.qingdai.entity.Timeline;
import com.qingdai.service.TimelineService;
import com.qingdai.utils.SnowflakeIdGenerator;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody; // 添加缺失的导入语句
import org.springframework.web.bind.annotation.PathVariable; // 添加缺失的导入语句
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-03-01
 */
@RestController
@Tag(name = "时间线管理", description = "时间线相关操作接口")
@SecurityRequirement(name = "BearerAuth")
@RequestMapping("/timeline")
public class TimelineController {

    @Autowired
    private TimelineService timelineService;

    private final SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(1, 1);

    @GetMapping("/getAllTimelines")
    @PreAuthorize("permitAll()")
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

    @PostMapping("/addTimeline")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "添加时间线信息", description = "向数据库中添加一条时间线信息")
    public ResponseEntity<Timeline> addTimeline(@RequestBody TimelineDTO timelineDTO) {
        try {
            Timeline timeline = new Timeline();
            timeline.setId(snowflakeIdGenerator.nextId());
            timeline.setTitle(timelineDTO.getTitle());
            timeline.setTime(timelineDTO.getTime());
            timeline.setText(timelineDTO.getText());

            boolean success = timelineService.save(timeline);
            if (success) {
                return ResponseEntity.status(HttpStatus.CREATED).body(timeline);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/deleteTimeline/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除时间线信息", description = "根据ID从数据库中删除一条时间线信息")
    public ResponseEntity<Void> deleteTimeline(@PathVariable String id) {
        try {
            System.out.println(timelineService.getById(Long.parseLong(id)));
            boolean success = timelineService.removeById(Long.parseLong(id));
            System.out.println(success);
            if (success) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/updateTimeline")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新时间线信息", description = "根据传入的时间线信息更新数据库中的记录")
    public ResponseEntity<Timeline> updateTimeline(@RequestBody Timeline timeline) {
        try {
            boolean success = timelineService.updateById(timeline);
            if (success) {
                return ResponseEntity.status(HttpStatus.OK).body(timeline);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}