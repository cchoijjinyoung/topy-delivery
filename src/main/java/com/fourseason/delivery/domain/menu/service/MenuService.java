package com.fourseason.delivery.domain.menu.service;

import com.fourseason.delivery.domain.image.enums.S3Folder;
import com.fourseason.delivery.domain.image.service.FileService;
import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.repository.MemberRepository;
import com.fourseason.delivery.domain.menu.dto.request.CreateMenuRequestDto;
import com.fourseason.delivery.domain.menu.dto.request.UpdateMenuRequestDto;
import com.fourseason.delivery.domain.menu.dto.response.MenuResponseDto;
import com.fourseason.delivery.domain.menu.entity.Menu;
import com.fourseason.delivery.domain.menu.entity.MenuImage;
import com.fourseason.delivery.domain.menu.exception.MenuErrorCode;
import com.fourseason.delivery.domain.menu.repository.MenuImageRepository;
import com.fourseason.delivery.domain.menu.repository.MenuRepository;
import com.fourseason.delivery.domain.menu.repository.MenuRepositoryCustom;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.domain.shop.exception.ShopErrorCode;
import com.fourseason.delivery.domain.shop.repository.ShopRepository;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MenuService {

    private final MenuRepository menuRepository;

    private final MenuImageRepository menuImageRepository;

    private final MemberRepository memberRepository;

    private final ShopRepository shopRepository;

    private final MenuRepositoryCustom menuRepositoryCustom;

    private final FileService fileService;

    @Transactional(readOnly = true)
    public PageResponseDto<MenuResponseDto> getMenuList(final UUID shopId, PageRequestDto pageRequestDto) {
        return menuRepositoryCustom.findMenuListWithPage(shopId, pageRequestDto);
    }

    @Transactional(readOnly = true)
    public MenuResponseDto getMenu(final UUID id) {
        Menu menu = menuRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_NOT_FOUND));

        List<String> images = menuImageRepository.findByMenuId(id)
            .stream()
            .map(MenuImage::getImageUrl)
            .toList();

        return MenuResponseDto.of(menu, images);
    }

    @Transactional
    public void registerMenu(CreateMenuRequestDto createMenuRequestDto, List<MultipartFile> images) {
        Shop shop = shopRepository.findById(UUID.fromString(createMenuRequestDto.shopId()))
            .orElseThrow(() -> new CustomException(ShopErrorCode.SHOP_NOT_FOUND));

        Menu menu = Menu.addOf(createMenuRequestDto, shop);
        menuRepository.save(menu);

        for (MultipartFile file : images) {
            fileService.saveImageFile(S3Folder.MENU, file, menu.getId());
        }
    }

    @Transactional
    public void updateMenu(final UUID id, UpdateMenuRequestDto updateMenuRequestDto) {
        Menu menu = menuRepository.findById(id)
            .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_NOT_FOUND));

        menu.updateOf(updateMenuRequestDto);
    }

    @Transactional
    public void deleteMenu(final UUID id) {
        Menu menu = menuRepository.findById(id)
            .orElseThrow(() -> new CustomException(MenuErrorCode.MENU_NOT_FOUND));

        // TODO: 현재 임시 유저를 넣었음. 이후 수정 필요.
        Member member = memberRepository.findById(1L)
            .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        menu.deleteOf(member.getUsername());
    }

    @Transactional
    public PageResponseDto<MenuResponseDto> searchMenu(UUID shopId, PageRequestDto pageRequestDto, String keyword) {
        return menuRepositoryCustom.searchMenuWithPage(shopId, pageRequestDto, keyword);
    }
}
