package com.qingdai.controller;

import com.qingdai.entity.dto.GroupPhotoDTO;
import com.qingdai.entity.GroupPhoto;
import com.qingdai.entity.GroupPhotoPhoto;
import com.qingdai.service.GroupPhotoPhotoService;
import com.qingdai.service.GroupPhotoService;
import com.qingdai.utils.SnowflakeIdGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author LiuZiMing
 * @since 2025-03-19
 */
@Slf4j
@RestController
@Tag(name = "组图管理", description = "组图相关操作接口")
@RequestMapping("/group-photos")
public class GroupPhotoController {
    @Autowired
    private GroupPhotoService groupPhotoService;
    @Autowired
    private GroupPhotoPhotoService groupPhotoPhotoService;

    private final SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(1, 1);

    @Operation(summary = "获取组图详情", description = "根据ID查询组图详细信息")
    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<GroupPhotoDTO> getGroupPhotoById(@PathVariable String id) {
        try {
            GroupPhotoDTO groupPhotoDTO = groupPhotoService.getGroupPhotoDTOById(id);

            if (groupPhotoDTO == null) {
                log.warn("未找到ID为{}的组图", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            log.info("成功获取组图详情，ID: {}", id);
            return ResponseEntity.ok(groupPhotoDTO);
        } catch (Exception e) {
            log.error("查询组图详情时发生异常，ID: {}，错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "获取所有组图", description = "获取所有组图列表")
    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<GroupPhotoDTO>> getAllGroupPhotos() {
        try {
            List<GroupPhotoDTO> groupPhotoDTOs = groupPhotoService.getAllGroupPhotoDTOs();

            if (groupPhotoDTOs == null || groupPhotoDTOs.isEmpty()) {
                log.warn("当前没有组图记录");
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            log.info("成功获取{}条组图记录", groupPhotoDTOs.size());
            return ResponseEntity.ok(groupPhotoDTOs);
        } catch (Exception e) {
            log.error("获取组图列表时发生异常: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "分页获取组图", description = "分页获取组图列表数据")
    @GetMapping("/page")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<GroupPhotoDTO>> getGroupPhotosByPage(
            @RequestParam int page,
            @RequestParam int pageSize) {
        try {
            if (page < 1) {
                page = 1;
                log.warn("页码小于1，已自动调整为1");
            }
            
            Page<GroupPhotoDTO> groupPhotoPage = groupPhotoService.getGroupPhotoDTOsByPage(page, pageSize);
            
            log.info("成功获取组图分页信息，总记录数: {}, 总页数: {}", groupPhotoPage.getTotal(), groupPhotoPage.getPages());
            return ResponseEntity.ok().body(groupPhotoPage);
        } catch (Exception e) {
            log.error("获取组图分页信息时发生错误，页码: {}, 每页大小: {}, 错误: {}", page, pageSize, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "创建组图", description = "创建新的组图记录")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createGroupPhoto(@RequestBody GroupPhotoDTO groupPhotoDTO) {
        try {
            if (groupPhotoDTO.photoIds == null || groupPhotoDTO.photoIds.isEmpty()) {
                log.warn("创建组图失败：照片ID列表不能为空");
                return ResponseEntity.badRequest().body("照片ID列表不能为空");
            }
            String groupPhotoId = snowflakeIdGenerator.nextId();
            groupPhotoDTO.getGroupPhoto().setId(groupPhotoId);

            // 保存组图基本信息
            boolean saved = groupPhotoService.save(groupPhotoDTO.getGroupPhoto());
            if (!saved) {
                log.error("组图保存失败");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("组图创建失败");
            }

            // 添加照片关联（使用服务层方法处理多表操作）
            groupPhotoPhotoService.updateGroupPhotoPhoto(groupPhotoDTO);

            log.info("成功创建组图，ID: {}", groupPhotoId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(String.format("组图创建成功，ID: %s", groupPhotoId));
        } catch (Exception e) {
            log.error("创建组图时发生异常: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("服务器内部错误: " + e.getMessage());
        }
    }

    @Operation(summary = "更新组图", description = "更新组图信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateGroupPhoto(@RequestBody GroupPhotoDTO groupPhotoDTO, @PathVariable String id) {
        try {
            GroupPhoto groupPhoto = groupPhotoDTO.getGroupPhoto();
            if (id == null) {
                return ResponseEntity.badRequest().body("ID不能为空");
            }

            // 更新组图基本信息
            boolean updated = groupPhotoService.updateById(groupPhoto);
            if (!updated) {
                log.warn("组图不存在，ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("未找到指定组图");
            }

            // 处理照片关联
            if (groupPhotoDTO.getPhotoIds() != null && !groupPhotoDTO.getPhotoIds().isEmpty()) {
                groupPhotoPhotoService.updateGroupPhotoPhoto(groupPhotoDTO);
            }

            log.info("成功更新组图，ID: {}", id);
            return ResponseEntity.ok("组图更新成功");
        } catch (Exception e) {
            log.error("更新组图时发生异常，错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("服务器内部错误: " + e.getMessage());
        }
    }

    @Operation(summary = "删除组图", description = "删除指定ID的组图记录")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteGroupPhoto(@PathVariable String id) {
        try {
            // 使用Service层方法处理删除逻辑
            boolean result = groupPhotoService.deleteGroupPhotoWithRelations(id);
            
            if (result) {
                log.info("成功删除组图及其关联照片，ID: {}", id);
                return ResponseEntity.noContent().build();
            } else {
                log.warn("组图不存在或删除失败，ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("组图不存在或删除失败");
            }
        } catch (Exception e) {
            log.error("删除组图时发生异常，ID: {}，错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("服务器内部错误: " + e.getMessage());
        }
    }

    @Operation(summary = "获取组图照片数量", description = "根据ID获取组图中照片的数量")
    @GetMapping("/{id}/photos/count")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Integer> getPhotoCountById(@PathVariable String id) {
        try {
            GroupPhoto groupPhoto = groupPhotoService.getById(id);

            if (groupPhoto == null) {
                log.warn("未找到ID为{}的组图", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            int count = groupPhotoPhotoService.countByGroupPhotoId(id);
            log.info("成功获取组图ID: {} 的照片数量: {}", id, count);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("获取组图照片数量时发生异常，ID: {}，错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}