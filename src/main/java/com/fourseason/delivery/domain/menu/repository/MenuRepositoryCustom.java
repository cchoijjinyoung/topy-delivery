package com.fourseason.delivery.domain.menu.repository;

import com.fourseason.delivery.domain.menu.dto.response.MenuResponseDto;
import com.fourseason.delivery.domain.menu.entity.Menu;
import com.fourseason.delivery.domain.menu.exception.MenuErrorCode;
import com.fourseason.delivery.domain.shop.dto.response.ShopResponseDto;
import com.fourseason.delivery.domain.shop.exception.ShopErrorCode;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.exception.CustomException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.fourseason.delivery.domain.menu.entity.QMenu.menu;
import static com.fourseason.delivery.domain.menu.entity.QMenuImage.menuImage;

@Repository
@RequiredArgsConstructor
public class MenuRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 메뉴 목록 조회
     */
    public PageResponseDto<MenuResponseDto> findMenuListWithPage(UUID shopId, PageRequestDto pageRequestDto) {
        List<MenuResponseDto> content = getMenuList(shopId, pageRequestDto);
        long total = getTotalDataCount(shopId, pageRequestDto);

        return new PageResponseDto<>(content, total);
    }

    /**
     * 메뉴 검색 조회
     */
    public PageResponseDto<MenuResponseDto> searchMenuWithPage(UUID shopId, PageRequestDto pageRequestDto, String keyword) {
        List<MenuResponseDto> content = getMenuList(shopId, pageRequestDto, keyword);
        long total = getTotalDataCount(shopId, pageRequestDto, keyword);

        return new PageResponseDto<>(content, total);
    }

    /**
     * 페이징 결과 조회 메서드
     */
    private List<MenuResponseDto> getMenuList(UUID shopId, PageRequestDto pageRequestDto) {
        return jpaQueryFactory
            .select(Projections.constructor(MenuResponseDto.class,
                Expressions.stringTemplate("CAST({0} AS string)", menu.id),
                menu.name,
                menu.description,
                menu.price,
                GroupBy.list(menuImage.imageUrl)
            ))
            .from(menu)
            .leftJoin(menuImage).on(menu.id.eq(menuImage.menu.id))
            .where(getWhereConditions(shopId, pageRequestDto))
            .offset(pageRequestDto.getFirstIndex())
            .limit(pageRequestDto.getSize())
            .orderBy(getOrderConditions(pageRequestDto))
            .fetch();
    }

    /**
     * 검색 결과 페이징 조회 메서드
     */
    public List<MenuResponseDto> getMenuList(UUID shopId, PageRequestDto pageRequestDto, String keyword) {
        return jpaQueryFactory
            .select(Projections.constructor(MenuResponseDto.class,
                Expressions.stringTemplate("CAST({0} AS string)", menu.id),
                menu.name,
                menu.description,
                menu.price,
                GroupBy.list(menuImage.imageUrl)
            ))
            .from(menu)
            .leftJoin(menuImage).on(menu.id.eq(menuImage.menu.id))
            .where(getWhereConditions(shopId, pageRequestDto, keyword))
            .offset(pageRequestDto.getFirstIndex())
            .limit(pageRequestDto.getSize())
            .orderBy(getOrderConditions(pageRequestDto))
            .fetch();
    }

    /**
     * 전체 데이터 수 조회
     */
    private long getTotalDataCount(UUID shopId, PageRequestDto pageRequestDto) {
        return Optional.ofNullable(jpaQueryFactory
                .select(menu.count())
                .from(menu)
                .where(getWhereConditions(shopId, pageRequestDto))
                .fetchOne()
            )
            .orElse(0L);
    }

    /**
     * 전체 데이터 수 조회 - keyword
     */
    private long getTotalDataCount(UUID shopId, PageRequestDto pageRequestDto, String keyword) {
        return Optional.ofNullable(jpaQueryFactory
                .select(menu.count())
                .from(menu)
                .where(getWhereConditions(shopId, pageRequestDto, keyword))
                .fetchOne()
            )
            .orElse(0L);
    }

    /**
     * 조회 조건
     */
    private BooleanBuilder getWhereConditions(UUID shopId, PageRequestDto pageRequestDto) {
        BooleanBuilder builder = new BooleanBuilder();

        return builder.and(menu.deletedAt.isNull())
            .and(menu.shop.id.eq(shopId));
    }

    /**
     * 조회 조건 - keyword
     */
    private BooleanBuilder getWhereConditions(UUID shopId, PageRequestDto pageRequestDto, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new CustomException(MenuErrorCode.NO_KEYWORD);
        }

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(menu.deletedAt.isNull())
            .and(menu.shop.id.eq(shopId));

        BooleanExpression keywordCondition = menu.name.containsIgnoreCase(keyword)
            .or(menu.description.containsIgnoreCase(keyword));

        builder.and(keywordCondition);

        return builder;
    }

    /**
     * 정렬 조건
     */
    private OrderSpecifier<?> getOrderConditions(PageRequestDto pageRequestDto) {
        final String order = pageRequestDto.getOrder();

        return switch (order) {
            case "latest" -> menu.createdAt.desc();
            case "earliest" -> menu.createdAt.asc();
            default -> throw new CustomException(MenuErrorCode.ORDER_BY_NOT_FOUND);
        };
    }
}
