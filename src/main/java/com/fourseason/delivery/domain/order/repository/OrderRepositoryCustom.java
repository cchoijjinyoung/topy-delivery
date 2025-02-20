package com.fourseason.delivery.domain.order.repository;

import static com.fourseason.delivery.domain.order.entity.QOrder.order;

import com.fourseason.delivery.domain.menu.exception.MenuErrorCode;
import com.fourseason.delivery.domain.order.dto.response.OrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.dto.response.QOrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.entity.QOrder;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.exception.CustomException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryCustom {

  private final JPAQueryFactory jpaQueryFactory;

  /**
   * 주문 목록 조회
   */
  public PageResponseDto<OrderSummaryResponseDto> findOrderListWithPage(Long memberId,
      PageRequestDto pageRequestDto) {
    List<OrderSummaryResponseDto> content = getOrderList(memberId, pageRequestDto);
    long total = getTotalDataCount(memberId);

    return new PageResponseDto<>(content, total);
  }

  /**
   * 페이징 결과 조회 메서드
   */
  private List<OrderSummaryResponseDto> getOrderList(Long memberId, PageRequestDto pageRequestDto) {
    JPAQuery<OrderSummaryResponseDto> query = jpaQueryFactory
        .select(new QOrderSummaryResponseDto(order))
        .from(order)
        .where(getWhereConditions(memberId))
        .offset(pageRequestDto.getFirstIndex())
        .limit(pageRequestDto.getSize())
        .orderBy(getOrderConditions(pageRequestDto));

    return query.fetch();
  }

  /**
   * 전체 데이터 수 조회
   */
  private long getTotalDataCount(Long memberId) {
    return Optional.ofNullable(jpaQueryFactory
            .select(order.count())
            .from(order)
            .where(getWhereConditions(memberId))
            .fetchOne()
        )
        .orElse(0L);
  }

  /**
   * 조회 조건
   */
  private BooleanBuilder getWhereConditions(Long memberId) {
    BooleanBuilder builder = new BooleanBuilder();

    return builder.and(order.deletedAt.isNull())
        .and(order.member.id.eq(memberId));
  }

  /**
   * 정렬 조건
   */
  private OrderSpecifier<?> getOrderConditions(PageRequestDto pageRequestDto) {
    final String order = pageRequestDto.getOrder();

    return switch (order) {
      case "latest" -> QOrder.order.createdAt.desc();
      case "earliest" -> QOrder.order.createdAt.asc();
      default -> throw new CustomException(MenuErrorCode.ORDER_BY_NOT_FOUND);
    };
  }
}
