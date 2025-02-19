package com.fourseason.delivery.domain.order.repository;

import static com.fourseason.delivery.domain.order.entity.QOrder.order;
import static com.fourseason.delivery.domain.order.entity.QOrderMenu.orderMenu;

import com.fourseason.delivery.domain.menu.exception.MenuErrorCode;
import com.fourseason.delivery.domain.order.dto.response.OrderResponseDto;
import com.fourseason.delivery.domain.order.entity.QOrder;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.exception.CustomException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
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
  public PageResponseDto<OrderResponseDto> findOrderListWithPage(Long memberId,
      PageRequestDto pageRequestDto) {
    List<OrderResponseDto> content = getOrderList(memberId, pageRequestDto);
    long total = getTotalDataCount(memberId);

    return new PageResponseDto<>(content, total);
  }

  /**
   * 페이징 결과 조회 메서드
   */
  private List<OrderResponseDto> getOrderList(Long memberId, PageRequestDto pageRequestDto) {
    return jpaQueryFactory
        .select(
            Projections.constructor(OrderResponseDto.class,
                order.shop.name,
                order.address,
                order.instruction,
                order.totalPrice,
                order.orderStatus,
                order.orderType,
                GroupBy.list(
                    Projections.constructor(OrderResponseDto.MenuDto.class,
                        orderMenu.name,
                        orderMenu.price,
                        orderMenu.quantity
                    )
                )
            )
        )
        .from(order)
        .leftJoin(orderMenu).on(orderMenu.order.id.eq(order.id))
        .where(getWhereConditions(memberId))
        .offset(pageRequestDto.getFirstIndex())
        .limit(pageRequestDto.getSize())
        .orderBy(getOrderConditions(pageRequestDto))
        .fetch();
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
