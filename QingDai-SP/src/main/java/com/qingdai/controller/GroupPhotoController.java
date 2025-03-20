package com.qingdai.controller;

import com.qingdai.dto.GroupPhotoDTO;
import com.qingdai.entity.GroupPhoto;
import com.qingdai.service.GroupPhotoService;
import com.qingdai.service.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
@RequestMapping("/groupPhoto")
public class GroupPhotoController {
    @Autowired
    private GroupPhotoService groupPhotoService;


    @Operation(summary = "获取组图详情", description = "根据ID查询组图详细信息")
    @GetMapping("/getGroupPhoto/{id}")
    public ResponseEntity<GroupPhoto> getGroupPhotoById(@PathVariable Long id) {
        try {
            log.info("开始查询组图详情，ID: {}", id);
            GroupPhoto groupPhoto = groupPhotoService.getById(id);

            if (groupPhoto == null) {
                log.warn("未找到ID为{}的组图", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            log.info("成功获取组图详情，ID: {}", id);
            return ResponseEntity.ok(groupPhoto);
        } catch (Exception e) {
            log.error("查询组图详情时发生异常，ID: {}，错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "获取所有组图", description = "获取所有组图列表")
    @GetMapping("/getAllGroupPhotos")
    public ResponseEntity<List<GroupPhoto>> getAllGroupPhotos() {
        try {
            log.info("开始查询所有组图");
            List<GroupPhoto> groupPhotos = groupPhotoService.list();

            if (groupPhotos == null || groupPhotos.isEmpty()) {
                log.warn("当前没有组图记录");
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            log.info("成功获取{}条组图记录", groupPhotos.size());
            return ResponseEntity.ok(groupPhotos);
        } catch (Exception e) {
            log.error("获取组图列表时发生异常: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "创建组图", description = "创建新的组图记录")
    @PostMapping("/addGroupPhoto")
    public ResponseEntity<String> createGroupPhoto(@RequestBody GroupPhoto groupPhoto) {
        try {
            log.info("开始创建组图，请求参数: {}", groupPhoto);

            if (groupPhoto.getPhotos() == null || groupPhoto.getPhotos().isEmpty()) {
                log.warn("创建组图失败：照片列表不能为空");
                return ResponseEntity.badRequest().body("照片列表不能为空");
            }

            boolean saved = groupPhotoService.save(groupPhoto);
            if (!saved) {
                log.error("组图保存失败，ID: {}", groupPhoto.getId());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("组图创建失败");
            }
            log.info("成功创建组图，ID: {}", groupPhoto.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(String.format("组图创建成功，ID: %s", groupPhoto.getId()));
        } catch (Exception e) {
            log.error("创建组图时发生异常: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("服务器内部错误: " + e.getMessage());
        }
    }

    @Operation(summary = "更新组图", description = "更新指定ID的组图信息")
    @PutMapping("/updateGroupPhoto")
    public ResponseEntity<String> updateGroupPhoto(@RequestBody GroupPhoto groupPhoto) {
        try {
            Long id = groupPhoto.getId();
            if (id == null) {
                return ResponseEntity.badRequest().body("ID不能为空");
            }
            log.info("开始更新组图，ID: {}", id);

            boolean updated = groupPhotoService.updateById(groupPhoto);
            if (!updated) {
                log.warn("组图不存在，ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("未找到指定组图");
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
    @DeleteMapping("/deleteGroupPhoto/{id}")
    public ResponseEntity<String> deleteGroupPhoto(@PathVariable Long id) {
        try {
            log.info("开始删除组图，ID: {}", id);

            if (groupPhotoService.getById(id) == null) {
                log.warn("尝试删除不存在的组图，ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("组图不存在");
            }

            boolean removed = groupPhotoService.removeById(id);
            if (removed) {
                log.info("成功删除组图，ID: {}", id);
                return ResponseEntity.noContent().build();
            } else {
                log.error("删除组图失败，ID: {}", id);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("组图删除失败");
            }
        } catch (Exception e) {
            log.error("删除组图时发生异常，ID: {}，错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("服务器内部错误: " + e.getMessage());
        }
    }

    @Operation(summary = "获取组图预览列表", description = "获取所有组图的预览信息（含封面图）")
    @GetMapping("/previews")
    public ResponseEntity<List<GroupPhotoDTO>> getAllGroupPhotoPreviews() {
        try {
            log.info("开始获取组图预览列表");
            List<GroupPhoto> groupPhotos = groupPhotoService.list();

            if (groupPhotos == null || groupPhotos.isEmpty()) {
                log.warn("当前没有组图记录");
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }

            List<GroupPhotoDTO> dtos = groupPhotos.stream()
            .map(groupPhotoService::convertToDTO)
            .collect(Collectors.toList());

            log.info("成功获取{}条组图预览记录", dtos.size());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("获取组图预览列表时发生异常: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}