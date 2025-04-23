package com.qingdai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingdai.entity.dto.TimelineDTO;
import com.qingdai.entity.Timeline;
import com.qingdai.service.TimelineService;
import com.qingdai.utils.SnowflakeIdGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-03-01
 */
@Slf4j
@RestController
@Tag(name = "时间线管理", description = "时间线相关操作接口")
@SecurityRequirement(name = "BearerAuth")
@RequestMapping("/timelines")
public class TimelineController {

    @Autowired
    private TimelineService timelineService;

    private final SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(1, 1);

    @GetMapping
    @PreAuthorize("permitAll()")
    @Operation(summary = "获取全部时间线信息(时间倒叙)", description = "从数据库获取所有时间线的详细信息(时间倒叙)")
    public ResponseEntity<List<Timeline>> getAllTimelines() {
        try {
            List<Timeline> timelines = timelineService.list(
                    new LambdaQueryWrapper<Timeline>()
                            .orderByDesc(Timeline::getTime)
            );

            if (timelines == null || timelines.isEmpty()) {
                log.warn("未找到任何时间线记录");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
            }

            log.info("成功获取所有时间线信息，共{}条记录", timelines.size());
            return ResponseEntity.ok().body(timelines);

        } catch (Exception e) {
            log.error("获取时间线信息时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping
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
                log.info("成功添加时间线信息，ID: {}", timeline.getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(timeline);
            } else {
                log.error("添加时间线信息失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        } catch (Exception e) {
            log.error("添加时间线信息时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除时间线信息", description = "根据ID从数据库中删除一条时间线信息")
    public ResponseEntity<Void> deleteTimeline(@PathVariable String id) {
        try {
            Timeline timeline = timelineService.getById(Long.parseLong(id));
            if (timeline == null) {
                log.warn("未找到ID为{}的时间线记录", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            boolean success = timelineService.removeById(Long.parseLong(id));
            if (success) {
                log.info("成功删除时间线信息，ID: {}", id);
                return ResponseEntity.ok().build();
            } else {
                log.error("删除时间线信息失败，ID: {}", id);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            log.error("删除时间线信息时发生错误，ID: {}, 错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新时间线信息", description = "根据传入的时间线信息更新数据库中的记录")
    public ResponseEntity<Timeline> updateTimeline(@RequestBody Timeline timeline, @PathVariable String id) {
        try {
            boolean success = timelineService.updateById(timeline);
            if (success) {
                log.info("成功更新时间线信息，ID: {}", timeline.getId());
                return ResponseEntity.status(HttpStatus.OK).body(timeline);
            } else {
                log.warn("未找到ID为{}的时间线记录", timeline.getId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            log.error("更新时间线信息时发生错误，ID: {}, 错误: {}", timeline.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}