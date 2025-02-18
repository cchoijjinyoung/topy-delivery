package com.fourseason.delivery.domain.payment.repository;


import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.payment.dto.response.PaymentResponseDto;
import com.fourseason.delivery.domain.payment.exception.PaymentErrorCode;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.exception.CustomException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.fourseason.delivery.domain.payment.entity.QPayment.payment;

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
     * 사용자 결제 전체 조회
     */
    public PageResponseDto<PaymentResponseDto> findPaymentListByMemberWithPage(final PageRequestDto pageRequestDto, final Member member) {
        List<PaymentResponseDto> content = getPaymentList(pageRequestDto, member);
        long total = getTotalDataCount(member);

        return new PageResponseDto<>(content, total);
    }

    /**
     * 관리자 페이징 조회 메서드
     */
    private List<PaymentResponseDto> getPaymentList(final PageRequestDto pageRequestDto) {
        return jpaQueryFactory
                .select(Projections.constructor(PaymentResponseDto.class,
                        Expressions.stringTemplate("CAST({0} AS string)", payment.id),
                        payment.paymentKey,
                        payment.paymentAmount,
                        payment.paymentMethod,
                        payment.paymentMethod
                        ))
                .from(payment)
                .where(getWhereConditions())
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
                        Expressions.stringTemplate("CAST({0} AS string)", payment.id),
                        payment.paymentKey,
                        payment.paymentAmount,
                        payment.paymentMethod,
                        payment.paymentMethod
                ))
                .from(payment)
                .where(getWhereConditions(member))
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
                        .select(payment.count())
                        .from(payment)
                        .where(getWhereConditions())
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
     * 조회 조건
     */
    // wherecondition이 필요없는 경우나 pageRequestDto가 필요 없는 부분에 대한 의문
    private BooleanBuilder getWhereConditions() {
        BooleanBuilder builder = new BooleanBuilder();

        return builder.and(payment.deletedAt.isNull());

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
