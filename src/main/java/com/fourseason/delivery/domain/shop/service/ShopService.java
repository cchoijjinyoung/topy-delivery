package com.fourseason.delivery.domain.shop.service;

import com.fourseason.delivery.domain.image.enums.S3Folder;
import com.fourseason.delivery.domain.image.service.FileService;
import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.exception.MemberErrorCode;
import com.fourseason.delivery.domain.member.repository.MemberRepository;
import com.fourseason.delivery.domain.shop.dto.request.CreateShopRequestDto;
import com.fourseason.delivery.domain.shop.dto.request.UpdateShopRequestDto;
import com.fourseason.delivery.domain.shop.dto.response.ShopResponseDto;
import com.fourseason.delivery.domain.shop.entity.Category;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.domain.shop.entity.ShopImage;
import com.fourseason.delivery.domain.shop.exception.ShopErrorCode;
import com.fourseason.delivery.domain.shop.repository.CategoryRepository;
import com.fourseason.delivery.domain.shop.repository.ShopImageRepository;
import com.fourseason.delivery.domain.shop.repository.ShopRepository;
import com.fourseason.delivery.domain.shop.repository.ShopRepositoryCustom;
import com.fourseason.delivery.global.dto.FilterRequestDto;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;

    private final ShopRepositoryCustom shopRepositoryCustom;

    private final ShopImageRepository shopImageRepository;

    private final CategoryRepository categoryRepository;

    private final MemberRepository memberRepository;

    private final FileService fileService;

    @Transactional(readOnly = true)
    public PageResponseDto<ShopResponseDto> getShopList(PageRequestDto pageRequestDto) {
        return shopRepositoryCustom.findShopListWithPage(pageRequestDto);
    }

    @Transactional(readOnly = true)
    public ShopResponseDto getShop(final UUID id) {
        return shopRepositoryCustom.findShop(id);
    }

    @Transactional
    public void registerShop(CreateShopRequestDto createShopRequestDto, List<MultipartFile> images, final Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        Category category = categoryRepository.findByName(createShopRequestDto.category())
            .orElseThrow(() -> new CustomException(ShopErrorCode.CATEGORY_NOT_FOUND));

        Shop shop = Shop.addOf(createShopRequestDto, member, category);
        shopRepository.save(shop);

        for (MultipartFile file : images) {
            fileService.saveImageFile(S3Folder.SHOP, file, shop.getId());
        }
    }

    @Transactional
    public void updateShop(final UUID id, UpdateShopRequestDto updateShopRequestDto, List<MultipartFile> newImages, final String memberName) {
        Shop shop = shopRepository.findById(id)
            .orElseThrow(() -> new CustomException(ShopErrorCode.SHOP_NOT_FOUND));

        Category category = categoryRepository.findByName(updateShopRequestDto.category())
            .orElseThrow(() -> new CustomException(ShopErrorCode.CATEGORY_NOT_FOUND));

        List<UUID> exitingImages = shopImageRepository.findByShopIdAndDeletedByIsNull(id).stream()
            .map(ShopImage::getId)
            .toList();

        for (UUID imageId : exitingImages) {
            if (!updateShopRequestDto.images().contains(imageId)) {
                ShopImage shopImage = shopImageRepository.findById(imageId)
                    .orElseThrow(() -> new CustomException(ShopErrorCode.SHOP_IMAGE_NOT_FOUND));

                shopImage.deleteOf(memberName);
            }
        }

        newImages = newImages.stream()
            .filter(file -> !file.isEmpty())  // 빈 파일 제거
            .toList();

        for (MultipartFile file : newImages) {
            fileService.saveImageFile(S3Folder.SHOP, file, shop.getId());
        }

        shop.updateOf(updateShopRequestDto, category);
    }

    @Transactional
    public void deleteShop(final UUID id, final Long memberId) {
        Shop shop = shopRepository.findById(id)
            .orElseThrow(() -> new CustomException(ShopErrorCode.SHOP_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        shop.deleteOf(member.getUsername());
    }

    @Transactional
    public PageResponseDto<ShopResponseDto> searchShop(PageRequestDto pageRequestDto, String keyword, FilterRequestDto filterRequestDto) {
        return shopRepositoryCustom.searchShopWithPage(pageRequestDto, keyword, filterRequestDto);
    }
}
