package com.fourseason.delivery.domain.order.repository;

import static com.fourseason.delivery.domain.order.entity.QOrder.order;

import com.fourseason.delivery.domain.menu.exception.MenuErrorCode;
import com.fourseason.delivery.domain.order.dto.response.OrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.dto.response.OwnerOrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.dto.response.QOrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.dto.response.QOwnerOrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.entity.QOrder;
import com.fourseason.delivery.domain.order.exception.OrderErrorCode;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.exception.CustomException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OwnerOrderRepositoryCustom {

  private final JPAQueryFactory jpaQueryFactory;

  /**
   * 가게 주문 목록 조회
   */
  public PageResponseDto<OwnerOrderSummaryResponseDto> findOrderListWithPage(
      Long memberId,
      UUID shopId,
      PageRequestDto pageRequestDto
  ) {
    List<OwnerOrderSummaryResponseDto> content = getOrderList(memberId, shopId, pageRequestDto);
    long total = getTotalDataCount(memberId, shopId);

    return new PageResponseDto<>(content, total);
  }

  /**
   * 페이징 결과 조회 메서드
   */
  private List<OwnerOrderSummaryResponseDto> getOrderList(
      Long memberId,
      UUID shopId,
      PageRequestDto pageRequestDto
  ) {
    JPAQuery<OwnerOrderSummaryResponseDto> query = jpaQueryFactory
        .select(new QOwnerOrderSummaryResponseDto(order))
        .from(order)
        .where(getWhereConditions(memberId, shopId))
        .offset(pageRequestDto.getFirstIndex())
        .limit(pageRequestDto.getSize())
        .orderBy(getOrderConditions(pageRequestDto));

    return query.fetch();
  }

  /**
   * 전체 데이터 수 조회
   */
  private long getTotalDataCount(Long memberId, UUID shopId) {
    return Optional.ofNullable(jpaQueryFactory
            .select(order.count())
            .from(order)
            .where(getWhereConditions(memberId, shopId))
            .fetchOne()
        )
        .orElse(0L);
  }

  /**
   * 조회 조건
   */
  private BooleanBuilder getWhereConditions(Long memberId, UUID shopId) {
    BooleanBuilder builder = new BooleanBuilder();

    if (shopId != null) {
      builder.and(order.shop.id.eq(shopId));
    }

    return builder.and(order.deletedAt.isNull())
        .and(order.shop.member.id.eq(memberId));
  }

  /**
   * 정렬 조건
   */
  private OrderSpecifier<?> getOrderConditions(PageRequestDto pageRequestDto) {
    final String order = pageRequestDto.getOrder();

    return switch (order) {
      case "latest" -> QOrder.order.createdAt.desc();
      case "earliest" -> QOrder.order.createdAt.asc();
      default -> throw new CustomException(OrderErrorCode.ORDER_BY_NOT_FOUND);
    };
  }
}
