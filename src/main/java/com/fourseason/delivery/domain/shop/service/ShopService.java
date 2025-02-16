package com.fourseason.delivery.domain.shop.service;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.entity.Role;
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
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;

    private final ShopRepositoryCustom shopRepositoryCustom;

    private final ShopImageRepository shopImageRepository;

    private final CategoryRepository categoryRepository;

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public PageResponseDto<ShopResponseDto> getShopList(PageRequestDto pageRequestDto) {
        return shopRepositoryCustom.findShopListWithPage(pageRequestDto);
    }

    @Transactional(readOnly = true)
    public ShopResponseDto getShop(final UUID id) {
        Shop shop = shopRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new CustomException(ShopErrorCode.SHOP_NOT_FOUND));

        List<String> images = shopImageRepository.findByShopId(id)
            .stream()
            .map(ShopImage::getImageUrl)
            .toList();

        return ShopResponseDto.of(shop, images);
    }

    @Transactional
    public void registerShop(CreateShopRequestDto createShopRequestDto) {
        // TODO: 현재 임시 유저를 넣었음. 이후 수정 필요.
        Member member = memberRepository.findById(1L)
            .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        Category category = categoryRepository.findByName(createShopRequestDto.category())
                .orElseThrow(() -> new CustomException(ShopErrorCode.CATEGORY_NOT_FOUND));

        Shop shop = Shop.addOf(createShopRequestDto, member, category);

        shopRepository.save(shop);
    }

    @Transactional
    public void updateShop(final UUID id, UpdateShopRequestDto updateShopRequestDto) {
        Shop shop = shopRepository.findById(id)
            .orElseThrow(() -> new CustomException(ShopErrorCode.SHOP_NOT_FOUND));

        Category category = categoryRepository.findByName(updateShopRequestDto.category())
            .orElseThrow(() -> new CustomException(ShopErrorCode.CATEGORY_NOT_FOUND));

        shop.updateOf(updateShopRequestDto, category);
    }

    @Transactional
    public void deleteShop(final UUID id) {
        Shop shop = shopRepository.findById(id)
                .orElseThrow(() -> new CustomException(ShopErrorCode.SHOP_NOT_FOUND));

        // TODO: 현재 임시 유저를 넣었음. 이후 수정 필요.
        Member member = new Member("유저", "user@example.com", "1234", "010-0000-0000", Role.CUSTOMER);

        shop.deleteOf(member.getUsername());
    }
}
