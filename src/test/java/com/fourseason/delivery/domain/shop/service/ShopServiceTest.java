package com.fourseason.delivery.domain.shop.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.fourseason.delivery.domain.image.enums.S3Folder;
import com.fourseason.delivery.domain.image.service.FileService;
import com.fourseason.delivery.domain.member.MemberErrorCode;
import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.repository.MemberRepository;
import com.fourseason.delivery.domain.shop.dto.request.CreateShopRequestDto;
import com.fourseason.delivery.domain.shop.dto.response.ShopResponseDto;
import com.fourseason.delivery.domain.shop.entity.Category;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.domain.shop.entity.ShopImage;
import com.fourseason.delivery.domain.shop.exception.ShopErrorCode;
import com.fourseason.delivery.domain.shop.repository.CategoryRepository;
import com.fourseason.delivery.domain.shop.repository.ShopImageRepository;
import com.fourseason.delivery.domain.shop.repository.ShopRepositoryCustom;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.exception.CustomException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import com.fourseason.delivery.domain.shop.repository.ShopRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private ShopRepositoryCustom shopRepositoryCustom;

    @Mock
    private ShopImageRepository shopImageRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private FileService fileService;

    @InjectMocks
    private ShopService shopService;

    private final UUID shopId = UUID.fromString("e3c7a4b4-5aaf-4c7d-bdff-c64dbf7ef6f9");

    @Nested
    @DisplayName("가게 목록 조회")
    class 가게_목록_조회 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            given(shopRepositoryCustom.findShopListWithPage(any())).willReturn(new PageResponseDto<>(Collections.singletonList(ShopResponseDto.builder().build()), 1));

            // when
            PageResponseDto<ShopResponseDto> result = shopService.getShopList(any());

            // then
            assertThat(result).isNotNull();
            then(shopRepositoryCustom).should().findShopListWithPage(any());
        }
    }

    @Nested
    @DisplayName("가게 상세 조회")
    class 가게_상세_조회 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            Shop shop = Mockito.spy(Shop.class);
            given(shop.getId()).willReturn(shopId);

            given(shop.getName()).willReturn("00피자");
            given(shop.getDescription()).willReturn("맛있는 피자");
            given(shop.getTel()).willReturn("010-1234-5678");
            given(shop.getAddress()).willReturn("서울시 강남구");
            given(shop.getDetailAddress()).willReturn("2층");

            ShopImage shopImage1 = Mockito.spy(ShopImage.class);
            given(shopImage1.getImageUrl()).willReturn("image1.jpg");

            ShopImage shopImage2 = Mockito.spy(ShopImage.class);
            given(shopImage2.getImageUrl()).willReturn("image2.jpg");

            given(shopRepository.findByIdAndDeletedAtIsNull(shopId)).willReturn(Optional.of(shop));
            given(shopImageRepository.findByShopId(shopId)).willReturn(List.of(shopImage1, shopImage2));

            // when
            ShopResponseDto result = shopService.getShop(shopId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.images()).hasSize(2);
            then(shopRepository).should().findByIdAndDeletedAtIsNull(shopId);
            then(shopImageRepository).should().findByShopId(shopId);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 가게")
        void 실패_존재하지_않는_가게() {
            // given
            given(shopRepository.findByIdAndDeletedAtIsNull(shopId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> shopService.getShop(shopId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ShopErrorCode.SHOP_NOT_FOUND.getMessage());

            then(shopRepository).should().findByIdAndDeletedAtIsNull(shopId);
        }
    }

    @Nested
    @DisplayName("가게 등록")
    class 가게_등록 {

        @Test
        @DisplayName("성공 - 이미지 O")
        void 성공_이미지_O() {
            // given
            CreateShopRequestDto requestDto = new CreateShopRequestDto("00피자", "맛있는 피자", "010-1234-5678", "서울시 강남구", "2층", "카테고리");
            List<MultipartFile> images = List.of(
                    new MockMultipartFile("file1", "image1.jpg", "image/jpeg", "test1".getBytes()),
                    new MockMultipartFile("file2", "image2.jpg", "image/jpeg", "test2".getBytes())
            );
            Long memberId = 1L;

            Member member = Member.builder().build();
            ReflectionTestUtils.setField(member, "id", memberId);

            Category category = Category.builder().name("카테고리").build();
            Shop shop = Shop.addOf(requestDto, member, category);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(categoryRepository.findByName("카테고리")).willReturn(Optional.of(category));
            given(shopRepository.save(any())).willReturn(shop);

            // when
            shopService.registerShop(requestDto, images, memberId);

            // then
            then(memberRepository).should().findById(memberId);
            then(categoryRepository).should().findByName("카테고리");
            then(shopRepository).should().save(any());
            then(fileService).should(times(2)).saveImageFile(eq(S3Folder.SHOP), any(), any());
        }

        @Test
        @DisplayName("성공 - 이미지 X")
        void 성공_이미지_X() {
            // given
            CreateShopRequestDto requestDto = new CreateShopRequestDto("00피자", "맛있는 피자", "010-1234-5678", "서울시 강남구", "2층", "카테고리");
            List<MultipartFile> images = Collections.emptyList(); // 이미지 없음
            Long memberId = 1L;

            Member member = Member.builder().build();
            ReflectionTestUtils.setField(member, "id", memberId);

            Category category = Category.builder().name("카테고리").build();
            Shop shop = Shop.addOf(requestDto, member, category);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(categoryRepository.findByName("카테고리")).willReturn(Optional.of(category));
            given(shopRepository.save(any())).willReturn(shop);

            // when
            shopService.registerShop(requestDto, images, memberId);

            // then
            then(memberRepository).should().findById(memberId);
            then(categoryRepository).should().findByName("카테고리");
            then(shopRepository).should().save(any());
            then(fileService).should(never()).saveImageFile(eq(S3Folder.SHOP), any(), any());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 회원")
        void 실패_존재하지_않는_회원() {
            // given
            CreateShopRequestDto requestDto = new CreateShopRequestDto("00피자", "맛있는 피자", "010-1234-5678", "서울시 강남구", "2층", "카테고리");
            Long memberId = 1L;

            given(memberRepository.findById(memberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> shopService.registerShop(requestDto, Collections.emptyList(), memberId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());

            then(memberRepository).should().findById(memberId);
            then(categoryRepository).should(never()).findByName(any());
            then(shopRepository).should(never()).save(any());
            then(fileService).should(never()).saveImageFile(any(), any(), any());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 카테고리")
        void 실패_존재하지_않는_카테고리() {
            // given
            CreateShopRequestDto requestDto = new CreateShopRequestDto("00피자", "맛있는 피자", "010-1234-5678", "서울시 강남구", "2층", "카테고리");
            Long memberId = 1L;

            Member member = Member.builder().build();
            ReflectionTestUtils.setField(member, "id", memberId);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(categoryRepository.findByName("카테고리")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> shopService.registerShop(requestDto, Collections.emptyList(), memberId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ShopErrorCode.CATEGORY_NOT_FOUND.getMessage());

            then(memberRepository).should().findById(memberId);
            then(categoryRepository).should().findByName("카테고리");
            then(shopRepository).should(never()).save(any());
            then(fileService).should(never()).saveImageFile(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("가게 삭제")
    class 가게_삭제 {

        private final Long memberId = 1L;

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            Member member = Member.builder().build();
            ReflectionTestUtils.setField(member, "id", memberId);

            Shop shop = Mockito.mock(Shop.class);
            given(shopRepository.findById(shopId)).willReturn(Optional.of(shop));
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

            // when
            shopService.deleteShop(shopId, memberId);

            // then
            then(shopRepository).should().findById(shopId);
            then(memberRepository).should().findById(memberId);
            then(shop).should().deleteOf(member.getUsername());
        }

        @Test
        @DisplayName("가게가 존재하지 않으면 예외 발생")
        void 가게_존재하지_않음() {
            // given
            given(shopRepository.findById(shopId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> shopService.deleteShop(shopId, memberId))
                .isInstanceOf(CustomException.class)
                .hasMessage(ShopErrorCode.SHOP_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("회원이 존재하지 않으면 예외 발생")
        void 회원_존재하지_않음() {
            // given
            Shop shop = Mockito.mock(Shop.class);
            given(shopRepository.findById(shopId)).willReturn(Optional.of(shop));
            given(memberRepository.findById(memberId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> shopService.deleteShop(shopId, memberId))
                .isInstanceOf(CustomException.class)
                .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
        }
    }
}