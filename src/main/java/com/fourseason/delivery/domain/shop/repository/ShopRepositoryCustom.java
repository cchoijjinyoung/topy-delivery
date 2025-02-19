package com.fourseason.delivery.domain.shop.repository;

import com.fourseason.delivery.domain.shop.dto.response.ShopResponseDto;
import com.fourseason.delivery.domain.shop.exception.ShopErrorCode;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.exception.CustomException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static com.fourseason.delivery.domain.shop.entity.QShop.shop;
import static com.fourseason.delivery.domain.shop.entity.QShopImage.shopImage;

@Repository
@RequiredArgsConstructor
public class ShopRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

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
    public PageResponseDto<ShopResponseDto> searchShopWithPage(PageRequestDto pageRequestDto, String keyword) {
        List<ShopResponseDto> content = getShopList(pageRequestDto, keyword);
        long total = getTotalDataCount(keyword);

        return new PageResponseDto<>(content, total);
    }

    /**
     * 페이징 결과 조회 메서드
     */
    private List<ShopResponseDto> getShopList(PageRequestDto pageRequestDto) {
        return jpaQueryFactory
            .select(Projections.constructor(ShopResponseDto.class,
                Expressions.stringTemplate("CAST({0} AS string)", shop.id),
                shop.name,
                shop.description,
                shop.tel,
                shop.address,
                shop.detailAddress,
                GroupBy.list(shopImage.imageUrl)
            ))
            .from(shop)
            .leftJoin(shopImage).on(shop.id.eq(shopImage.shop.id))
            .where(getWhereConditions())
            .offset(pageRequestDto.getFirstIndex())
            .limit(pageRequestDto.getSize())
            .orderBy(getOrderConditions(pageRequestDto))
            .fetch();
    }

    /**
     * 검색 결과 페이징 조회 메서드
     */
    private List<ShopResponseDto> getShopList(PageRequestDto pageRequestDto, String keyword) {
        return jpaQueryFactory
            .select(Projections.constructor(ShopResponseDto.class,
                Expressions.stringTemplate("CAST({0} AS string)", shop.id),
                shop.name,
                shop.description,
                shop.tel,
                shop.address,
                shop.detailAddress,
                GroupBy.list(shopImage.imageUrl)
            ))
            .from(shop)
            .leftJoin(shopImage).on(shop.id.eq(shopImage.shop.id))
            .where(getWhereConditions(keyword))
            .offset(pageRequestDto.getFirstIndex())
            .limit(pageRequestDto.getSize())
            .orderBy(getOrderConditions(pageRequestDto))
            .fetch();
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
    private long getTotalDataCount(String keyword) {
        return Optional.ofNullable(jpaQueryFactory
                .select(shop.count())
                .from(shop)
                .where(getWhereConditions(keyword))
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
     * 조회 조건 - keyword
     */
    private BooleanBuilder getWhereConditions(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new CustomException(ShopErrorCode.NO_KEYWORD);
        }

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(shop.deletedAt.isNull());

        BooleanExpression keywordCondition = shop.name.containsIgnoreCase(keyword)
                .or(shop.description.containsIgnoreCase(keyword));

        builder.and(keywordCondition);

        return builder;
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
