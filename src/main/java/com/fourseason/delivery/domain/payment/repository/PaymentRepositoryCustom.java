package com.fourseason.delivery.domain.payment.repository;


import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.payment.dto.response.PaymentResponseDto;
import com.fourseason.delivery.domain.payment.exception.PaymentErrorCode;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.domain.shop.exception.ShopErrorCode;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.exception.CustomException;
import com.querydsl.core.BooleanBuilder;
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

import static com.fourseason.delivery.domain.payment.entity.QPayment.payment;
import static com.fourseason.delivery.domain.order.entity.QOrder.order;
import static com.fourseason.delivery.domain.shop.entity.QShop.shop;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    /**
     * 관리자 결제 전체 조회
     */
    public PageResponseDto<PaymentResponseDto> findPaymentListWithPage(final PageRequestDto pageRequestDto) {
        List<PaymentResponseDto> content = getPaymentList(pageRequestDto);
        long total = getTotalDataCount();

        return new PageResponseDto<>(content, total);
    }

    /**
     * 관리자 결제 검색
     */
    public PageResponseDto<PaymentResponseDto> searchPaymentListWithPage(final PageRequestDto pageRequestDto, final String keyword) {
        List<PaymentResponseDto> content = getPaymentList(pageRequestDto, keyword);
        long total = getTotalDataCount(keyword);

        return new PageResponseDto<>(content, total);
    }

    /**
     * 사용자 결제 전체 조회
     */
    public PageResponseDto<PaymentResponseDto> findPaymentListByMemberWithPage(final PageRequestDto pageRequestDto, final Member member) {
        List<PaymentResponseDto> content = getPaymentList(pageRequestDto, member);
        long total = getTotalDataCount(member);

        return new PageResponseDto<>(content, total);
    }

    /**
     * 가게주인 결제 전체 조회
     */
    public PageResponseDto<PaymentResponseDto> findPaymentListByShopWithPage(final PageRequestDto pageRequestDto, final Shop shop) {
        List<PaymentResponseDto> content = getPaymentList(pageRequestDto, shop);
        long total = getTotalDataCount(shop);

        return new PageResponseDto<>(content, total);
    }

    /**
     * 관리자 페이징 조회 메서드
     */
    private List<PaymentResponseDto> getPaymentList(final PageRequestDto pageRequestDto) {
        return jpaQueryFactory
                .select(Projections.constructor(PaymentResponseDto.class,
                        payment.id,
                        payment.paymentKey,
                        payment.paymentAmount,
                        payment.paymentMethod,
                        payment.paymentStatus,
                        payment.cancelReason,
                        payment.balanceAmount
                        ))
                .from(payment)
                .where(getWhereConditions())
                .offset(pageRequestDto.getFirstIndex())
                .limit(pageRequestDto.getSize())
                .orderBy(getOrderConditions(pageRequestDto))
                .fetch();
    }

    /**
     * 관리자 검색 메서드
     */
    private List<PaymentResponseDto> getPaymentList(final PageRequestDto pageRequestDto, final String keyword) {
        return jpaQueryFactory
                .select(Projections.constructor(PaymentResponseDto.class,
                        payment.id,
                        payment.paymentKey,
                        payment.paymentAmount,
                        payment.paymentMethod,
                        payment.paymentStatus,
                        payment.cancelReason,
                        payment.balanceAmount
                ))
                .from(payment)
                .where(getWhereConditions(keyword))
                .offset(pageRequestDto.getFirstIndex())
                .limit(pageRequestDto.getSize())
                .orderBy(getOrderConditions(pageRequestDto))
                .fetch();
    }

    /**
     * 사용자 페이징 조회 메서드
     */
    private List<PaymentResponseDto> getPaymentList(final PageRequestDto pageRequestDto, final Member member) {
        return jpaQueryFactory
                .select(Projections.constructor(PaymentResponseDto.class,
                        payment.id,
                        payment.paymentKey,
                        payment.paymentAmount,
                        payment.paymentMethod,
                        payment.paymentStatus,
                        payment.cancelReason,
                        payment.balanceAmount
                ))
                .from(payment)
                .where(getWhereConditions(member))
                .offset(pageRequestDto.getFirstIndex())
                .limit(pageRequestDto.getSize())
                .orderBy(getOrderConditions(pageRequestDto))
                .fetch();
    }

    /**
     * 가게주인 페이징 조회 메서드
     */
    private List<PaymentResponseDto> getPaymentList(final PageRequestDto pageRequestDto, final Shop ownerShop) {
        return jpaQueryFactory
                .select(Projections.constructor(PaymentResponseDto.class,
                        payment.id,
                        payment.paymentKey,
                        payment.paymentAmount,
                        payment.paymentMethod,
                        payment.paymentStatus,
                        payment.cancelReason,
                        payment.balanceAmount
                ))
                .from(payment)
                .join(order).on(order.id.eq(payment.order.id))  // Payment와 Order 조인
                .join(shop).on(shop.id.eq(order.shop.id))  // Order와 Shop 조인
                .where(getWhereConditions(ownerShop))  // Shop 기준으로 필터링
                .offset(pageRequestDto.getFirstIndex())
                .limit(pageRequestDto.getSize())
                .orderBy(getOrderConditions(pageRequestDto))
                .fetch();
    }

    /**
     * 전체 데이터 수 조회 admin
     */
    private long getTotalDataCount() {
        return Optional.ofNullable(jpaQueryFactory
                        .select(payment.count())
                        .from(payment)
                        .where(getWhereConditions())
                        .fetchOne()
                )
                .orElse(0L);
    }

    /**
     * 전체 데이터 수 조회 admin-search
     */
    private long getTotalDataCount(final String keyword) {
        return Optional.ofNullable(jpaQueryFactory
                        .select(payment.count())
                        .from(payment)
                        .where(getWhereConditions(keyword))
                        .fetchOne()
                )
                .orElse(0L);
    }

    /**
     * 전체 데이터 수 조회 - member
     */
    private long getTotalDataCount(final Member member) {
        return Optional.ofNullable(jpaQueryFactory
                        .select(payment.count())
                        .from(payment)
                        .where(getWhereConditions(member))
                        .fetchOne()
                )
                .orElse(0L);
    }

    /**
     * 전체 데이터 수 조회 - shop
     */
    private long getTotalDataCount(final Shop ownerShop) {
        return Optional.ofNullable(jpaQueryFactory
                        .select(payment.count())
                        .from(payment)
                        .join(order).on(order.id.eq(payment.order.id))  // Payment와 Order 조인
                        .join(shop).on(shop.id.eq(order.shop.id))  // Order와 Shop 조인
                        .where(getWhereConditions(ownerShop))
                        .fetchOne()
                )
                .orElse(0L);
    }

    /**
     * 조회 조건 admin
     */
    private BooleanBuilder getWhereConditions() {
        BooleanBuilder builder = new BooleanBuilder();

        return builder.and(payment.deletedAt.isNull());

    }

    /**
     * 조회 조건 admin search
     */
    private BooleanBuilder getWhereConditions(final String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new CustomException(PaymentErrorCode.NO_KEYWORD);
        }

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(payment.deletedAt.isNull());

        BooleanExpression keywordCondition = payment.paymentKey.containsIgnoreCase(keyword);

        builder.and(keywordCondition);

        return builder;

    }

    /**
     * 조회 조건 - member
     */
    private BooleanBuilder getWhereConditions(final Member member) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(payment.member.id.eq(member.getId()));
        builder.and(payment.deletedAt.isNull());

        return builder;
    }

    /**
     * 조회 조건 - shop
     */
    private BooleanBuilder getWhereConditions(final Shop ownerShop) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(shop.id.eq(ownerShop.getId()));
        builder.and(payment.deletedAt.isNull());

        return builder;
    }

    /**
     * 정렬 조건
     */
    private OrderSpecifier<?> getOrderConditions(PageRequestDto pageRequestDto) {
        final String order = pageRequestDto.getOrder();

        return switch (order) {
            case "latest" -> payment.createdAt.desc();
            case "earliest" -> payment.createdAt.asc();
            default -> throw new CustomException(PaymentErrorCode.ORDER_BY_NOT_FOUND);
        };
    }
}
