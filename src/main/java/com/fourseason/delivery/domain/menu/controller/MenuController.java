package com.fourseason.delivery.domain.menu.controller;

import com.fourseason.delivery.domain.menu.dto.request.CreateMenuRequestDto;
import com.fourseason.delivery.domain.menu.dto.request.UpdateMenuRequestDto;
import com.fourseason.delivery.domain.menu.dto.response.MenuResponseDto;
import com.fourseason.delivery.domain.menu.service.MenuService;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/api/menus")
@RestController
public class MenuController {

    private final MenuService menuService;

    /**
     * 메뉴 목록 조회 API
     */
    @GetMapping
    public ResponseEntity<PageResponseDto<MenuResponseDto>> getMenuList(@RequestParam UUID shopId,
                                                                        @RequestParam(defaultValue = "1") int page,
                                                                        @RequestParam(defaultValue = "10") int size,
                                                                        @RequestParam(defaultValue = "latest") String orderBy) {
        PageRequestDto pageRequestDto = PageRequestDto.of(page-1, size, orderBy);
        return ResponseEntity.ok(menuService.getMenuList(shopId, pageRequestDto));
    }

    /**
     * 메뉴 상세 조회 API
     */
    @GetMapping("/{id}")
    public ResponseEntity<MenuResponseDto> getMenu(@PathVariable String id) {
        return ResponseEntity.ok(menuService.getMenu(UUID.fromString(id)));
    }

    /**
     * 메뉴 등록 API
     */
    @PostMapping
    public ResponseEntity<Void> registerMenu(@RequestBody @Valid CreateMenuRequestDto createMenuRequestDto) {
        menuService.registerMenu(createMenuRequestDto);
        return ResponseEntity.ok().build();
    }

    /**
     * 메뉴 수정 API
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateMenu(@PathVariable UUID id,
                                           @RequestBody @Valid UpdateMenuRequestDto updateMenuRequestDto) {
        menuService.updateMenu(id, updateMenuRequestDto);
        return ResponseEntity.ok().build();
    }

    /**
     * 메뉴 삭제 API
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenu(@PathVariable UUID id) {
        menuService.deleteMenu(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 메뉴 검색 API
     */
    @GetMapping("/search")
    public ResponseEntity<PageResponseDto<MenuResponseDto>> searchMenu(@RequestParam @NotBlank(message = "검색어를 입력해주세요.") String keyword,
                                                                       @RequestParam @NotBlank(message = "가게 id 값을 입력해주세요.") UUID shopId,
                                                                       @RequestParam(defaultValue = "1") int page,
                                                                       @RequestParam(defaultValue = "10") int size,
                                                                       @RequestParam(defaultValue = "latest") String orderBy) {
        PageRequestDto pageRequestDto = PageRequestDto.of(page-1, size, orderBy);
        return ResponseEntity.ok(menuService.searchMenu(shopId, pageRequestDto, keyword));
    }
}
