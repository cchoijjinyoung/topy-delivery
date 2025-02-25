package com.fourseason.delivery.domain.shop.repository;

import com.fourseason.delivery.domain.shop.dto.response.ShopResponseDto;
import com.fourseason.delivery.domain.shop.exception.ShopErrorCode;
import com.fourseason.delivery.global.dto.FilterRequestDto;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.exception.CustomException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.querydsl.core.group.GroupBy.list;
import static com.querydsl.core.group.GroupBy.groupBy;

import static com.fourseason.delivery.domain.shop.entity.QShop.shop;
import static com.fourseason.delivery.domain.shop.entity.QShopImage.shopImage;
import static com.fourseason.delivery.domain.review.entity.QReview.review;

@Repository
@RequiredArgsConstructor
public class ShopRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 가게 상세 조회
     */
    public ShopResponseDto findShop(UUID id) {
        return jpaQueryFactory
            .from(shop)
            .leftJoin(shopImage).on(shop.id.eq(shopImage.shop.id)
                .and(shopImage.deletedAt.isNull())
            )
            .where(shop.id.eq(id)
                .and(shop.deletedAt.isNull())
            )
            .transform(
                groupBy(shop.id).list(
                    Projections.constructor(ShopResponseDto.class,
                        Expressions.stringTemplate("CAST({0} AS string)", shop.id),
                        shop.name,
                        shop.description,
                        shop.tel,
                        shop.address,
                        shop.detailAddress,
                        list(shopImage.imageUrl),
                        JPAExpressions
                            .select(review.rating.avg().coalesce(0.0))
                            .from(review)
                            .where(review.shop.id.eq(shop.id)
                                .and(review.deletedAt.isNull()
                            )
                        )
                    )
                )
            )
            .stream()
            .findFirst()
            .orElseThrow(() -> new CustomException(ShopErrorCode.SHOP_NOT_FOUND));
    }

    /**
     * 가게 목록 조회
     */
    public PageResponseDto<ShopResponseDto> findShopListWithPage(PageRequestDto pageRequestDto) {
        List<ShopResponseDto> content = getShopList(pageRequestDto);
        long total = getTotalDataCount();

        return new PageResponseDto<>(content, total);
    }

    /**
     * 가게 검색 조회
     */
    public PageResponseDto<ShopResponseDto> searchShopWithPage(PageRequestDto pageRequestDto, String keyword, FilterRequestDto filterRequestDto) {
        List<ShopResponseDto> content = getShopList(pageRequestDto, keyword, filterRequestDto);
        long total = getTotalDataCount(keyword, filterRequestDto);

        return new PageResponseDto<>(content, total);
    }

    /**
     * 페이징 결과 조회 메서드
     */
    private List<ShopResponseDto> getShopList(PageRequestDto pageRequestDto) {
        return jpaQueryFactory
            .from(shop)
            .leftJoin(shopImage).on(shop.id.eq(shopImage.shop.id).and(shopImage.deletedBy.isNull()))
            .where(getWhereConditions())
            .offset(pageRequestDto.getFirstIndex())
            .limit(pageRequestDto.getSize())
            .orderBy(getOrderConditions(pageRequestDto))
            .transform(groupBy(shop.id).list(
                Projections.constructor(ShopResponseDto.class,
                    Expressions.stringTemplate("CAST({0} AS string)", shop.id),
                    shop.name,
                    shop.description,
                    shop.tel,
                    shop.address,
                    shop.detailAddress,
                    list(shopImage.imageUrl),
                    JPAExpressions
                        .select(review.rating.avg().coalesce(0.0))
                        .from(review)
                        .where(review.shop.id.eq(shop.id)
                            .and(review.deletedAt.isNull()
                        )
                    )
                )
            ));
    }

    /**
     * 검색 결과 페이징 조회 메서드
     */
    private List<ShopResponseDto> getShopList(PageRequestDto pageRequestDto, String keyword, FilterRequestDto filterRequestDto) {
        return jpaQueryFactory
            .from(shop)
            .leftJoin(shopImage).on(shop.id.eq(shopImage.shop.id))
            .where(getWhereConditions(keyword, filterRequestDto))
            .offset(pageRequestDto.getFirstIndex())
            .limit(pageRequestDto.getSize())
            .orderBy(getOrderConditions(pageRequestDto))
            .transform(groupBy(shop.id).list(
                Projections.constructor(ShopResponseDto.class,
                    Expressions.stringTemplate("CAST({0} AS string)", shop.id),
                    shop.name,
                    shop.description,
                    shop.tel,
                    shop.address,
                    shop.detailAddress,
                    list(shopImage.imageUrl),
                    JPAExpressions
                        .select(review.rating.avg().coalesce(0.0))
                        .from(review)
                        .where(review.shop.id.eq(shop.id)
                            .and(review.deletedAt.isNull()
                        )
                    )
                )
            ));
    }

    /**
     * 전체 데이터 수 조회
     */
    private long getTotalDataCount() {
        return Optional.ofNullable(jpaQueryFactory
                .select(shop.count())
                .from(shop)
                .where(getWhereConditions())
                .fetchOne()
            )
            .orElse(0L);
    }

    /**
     * 전체 데이터 수 조회 - keyword
     */
    private long getTotalDataCount(String keyword, FilterRequestDto filterRequestDto) {
        return Optional.ofNullable(jpaQueryFactory
                .select(shop.count())
                .from(shop)
                .where(getWhereConditions(keyword, filterRequestDto))
                .fetchOne()
            )
            .orElse(0L);
    }

    /**
     * 조회 조건
     */
    private BooleanBuilder getWhereConditions() {
        BooleanBuilder builder = new BooleanBuilder();

        return builder.and(shop.deletedAt.isNull());
    }

    /**
     * 조회 조건 - keyword, filtering
     */
    private BooleanBuilder getWhereConditions(String keyword, FilterRequestDto filterRequestDto) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(shop.deletedAt.isNull());

        // 검색 키워드 필터링
        addKeywordFilters(keyword, builder);

        // 카테고리 필터링
        addCategoryFilters(filterRequestDto.getCategory(), builder);

        // 지역 필터링
        addRegionFilters(filterRequestDto.getRegion(), builder);

        return builder;
    }

    /**
     * 검색 키워드 필터링 메서드
     */
    private static void addKeywordFilters(String keyword, BooleanBuilder builder) {
        if (!StringUtils.hasText(keyword)) {
            throw new CustomException(ShopErrorCode.NO_KEYWORD);
        }

        builder.and(
            shop.name.containsIgnoreCase(keyword)
                .or(shop.description.containsIgnoreCase(keyword))
        );
    }

    /**
     * 카테고리 필터링 메서드
     */
    private static void addCategoryFilters(String category, BooleanBuilder builder) {
        if (!category.equals("category")) {
            builder.and(
                shop.category.name.eq(category)
            );
        }
    }

    /**
     * 지역 필터링 메서드
     */
    private static void addRegionFilters(String region, BooleanBuilder builder) {
        if (!region.equals("region")) {
            builder.and(
                shop.detailAddress.containsIgnoreCase(region)
                    .or(shop.detailAddress.containsIgnoreCase(region))
            );
        }
    }

    /**
     * 정렬 조건
     */
    private OrderSpecifier<?> getOrderConditions(PageRequestDto pageRequestDto) {
        final String order = pageRequestDto.getOrder();

        return switch (order) {
            case "latest" -> shop.createdAt.desc();
            case "earliest" -> shop.createdAt.asc();
            default -> throw new CustomException(ShopErrorCode.ORDER_BY_NOT_FOUND);
        };
    }
}
