package com.qingdai.controller;

import com.qingdai.service.SharePhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@Tag(name = "照片分享管理", description = "照片分享相关接口")
@RequestMapping("/shares")
public class SharePhotoController {

    @Autowired
    private SharePhotoService sharePhotoService;

    @PostMapping
    @Operation(summary = "创建分享链接", description = "创建照片分享链接，返回分享ID")
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> createShareLink(
            @Parameter(description = "分享参数") @RequestBody CreateShareLinkRequest request) {
        try {
            String shareId = sharePhotoService.createShareLink(request.getPhotoIds(), request.getExpireDays());
            log.info("成功创建分享链接，shareId: {}", shareId);
            return ResponseEntity.ok(shareId);
        } catch (IllegalArgumentException e) {
            log.warn("创建分享链接参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("创建分享链接时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("创建分享链接失败");
        }
    }

    @GetMapping("/{shareId}/photos")
    @Operation(summary = "获取分享的照片", description = "根据分享ID获取分享的照片ID列表")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<String>> getSharePhotos(
            @Parameter(description = "分享ID") @PathVariable String shareId) {
        try {
            List<String> photoIds = sharePhotoService.getSharePhotoIds(shareId);
            if (photoIds.isEmpty()) {
                log.warn("未找到有效的分享照片，shareId: {}", shareId);
                return ResponseEntity.notFound().build();
            }
            log.info("成功获取分享照片，shareId: {}, 照片数量: {}", shareId, photoIds.size());
            return ResponseEntity.ok(photoIds);
        } catch (Exception e) {
            log.error("获取分享照片时发生错误，shareId: {}, 错误: {}", shareId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{shareId}/validate")
    @Operation(summary = "验证分享链接", description = "验证分享链接是否有效")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Boolean> validateShareLink(
            @Parameter(description = "分享ID") @PathVariable String shareId) {
        try {
            boolean isValid = sharePhotoService.validateShareLink(shareId);
            log.info("验证分享链接结果，shareId: {}, isValid: {}", shareId, isValid);
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            log.error("验证分享链接时发生错误，shareId: {}, 错误: {}", shareId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(false);
        }
    }
    
    @GetMapping
    @Operation(summary = "获取所有分享", description = "获取所有分享的列表")
    @PreAuthorize("hasAnyRole('ADMIN', 'VIEWER')")
    public ResponseEntity<List<Map<String, Object>>> getAllShares() {
        try {
            List<Map<String, Object>> shares = sharePhotoService.getAllShares();
            log.info("成功获取所有分享，数量: {}", shares.size());
            return ResponseEntity.ok(shares);
        } catch (Exception e) {
            log.error("获取所有分享时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @DeleteMapping("/{shareId}")
    @Operation(summary = "删除分享", description = "删除指定ID的分享")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boolean> deleteShare(
            @Parameter(description = "分享ID") @PathVariable String shareId) {
        try {
            boolean deleted = sharePhotoService.deleteShare(shareId);
            if (deleted) {
                log.info("成功删除分享，shareId: {}", shareId);
                return ResponseEntity.ok(true);
            } else {
                log.warn("删除分享失败，未找到分享，shareId: {}", shareId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("删除分享时发生错误，shareId: {}, 错误: {}", shareId, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(false);
        }
    }

    @Data
    public static class CreateShareLinkRequest {
        private String[] photoIds;
        private int expireDays;
    }
} 