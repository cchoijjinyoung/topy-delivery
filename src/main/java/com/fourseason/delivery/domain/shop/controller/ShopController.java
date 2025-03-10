package com.fourseason.delivery.domain.shop.controller;

import com.fourseason.delivery.domain.shop.dto.request.CreateShopRequestDto;
import com.fourseason.delivery.domain.shop.dto.request.UpdateShopRequestDto;
import com.fourseason.delivery.domain.shop.dto.response.ShopResponseDto;
import com.fourseason.delivery.domain.shop.service.ShopService;
import com.fourseason.delivery.global.auth.CustomPrincipal;
import com.fourseason.delivery.global.dto.FilterRequestDto;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.resolver.PageSize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RequestMapping("/api/shops")
@RestController
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    /**
     * 가게 목록 조회 API
     */
    @GetMapping
    public ResponseEntity<PageResponseDto<ShopResponseDto>> getShopList(@RequestParam(defaultValue = "1") int page,
                                                                        @PageSize int size,
                                                                        @RequestParam(defaultValue = "latest") String orderBy) {
        PageRequestDto pageRequestDto = PageRequestDto.of(page-1, size, orderBy);
        return ResponseEntity.ok(shopService.getShopList(pageRequestDto));
    }

    /**
     * 가게 상세 조회 API
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShopResponseDto> getShop(@PathVariable UUID id) {
        return ResponseEntity.ok(shopService.getShop(id));
    }

    /**
     * 가게 등록 API
     */
    @PostMapping
    public ResponseEntity<Void> registerShop(@RequestPart @Valid CreateShopRequestDto createShopRequestDto,
                                             @RequestPart List<MultipartFile> images,
                                             @AuthenticationPrincipal CustomPrincipal principal) {
        shopService.registerShop(createShopRequestDto, images, principal.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * 가게 수정 API
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateShop(@PathVariable UUID id,
                                           @RequestPart @Valid UpdateShopRequestDto updateShopRequestDto,
                                           @RequestPart List<MultipartFile> newImages,
                                           @AuthenticationPrincipal CustomPrincipal principal) {
        shopService.updateShop(id, updateShopRequestDto, newImages, principal.getName());
        return ResponseEntity.ok().build();
    }

    /**
     * 가게 삭제 API
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShop(@PathVariable UUID id,
                                           @AuthenticationPrincipal CustomPrincipal principal) {
        shopService.deleteShop(id, principal.getId());
        return ResponseEntity.ok().build();

    }

    /**
     * 가게 검색 API
     */
    @GetMapping("/search")
    public ResponseEntity<PageResponseDto<ShopResponseDto>> searchShop(@RequestParam @NotBlank(message = "검색어를 입력해주세요.") String keyword,
                                                                       @RequestParam(defaultValue = "1") int page,
                                                                       @PageSize int size,
                                                                       @RequestParam(defaultValue = "latest") String orderBy,
                                                                       @RequestParam(defaultValue = "category") String category,
                                                                       @RequestParam(defaultValue = "region") String region) {
        PageRequestDto pageRequestDto = PageRequestDto.of(page-1, size, orderBy);
        FilterRequestDto filterRequestDto = FilterRequestDto.of(category, region);
        return ResponseEntity.ok(shopService.searchShop(pageRequestDto, keyword, filterRequestDto));
    }
}
