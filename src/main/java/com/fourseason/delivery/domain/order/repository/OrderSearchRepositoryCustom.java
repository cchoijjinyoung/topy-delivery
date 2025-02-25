package com.fourseason.delivery.domain.order.repository;

import static com.fourseason.delivery.domain.order.entity.QOrder.order;

import com.fourseason.delivery.domain.order.dto.response.impl.CustomerOrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.dto.response.impl.ManagerOrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.dto.response.impl.OwnerOrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.dto.response.impl.QCustomerOrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.dto.response.impl.QManagerOrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.dto.response.impl.QOwnerOrderSummaryResponseDto;
import com.fourseason.delivery.domain.order.entity.QOrder;
import com.fourseason.delivery.domain.order.exception.OrderErrorCode;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.exception.CustomException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderSearchRepositoryCustom {

  private final JPAQueryFactory jpaQueryFactory;

  public PageResponseDto<CustomerOrderSummaryResponseDto> findByCustomerWithPage(
      String customerUsername,
      PageRequestDto pageRequestDto,
      String keyword
  ) {
    BooleanBuilder whereCondition = getDefaultSearchCondition(customerUsername, keyword);

    List<CustomerOrderSummaryResponseDto> content = getContentByCustomer(
        pageRequestDto, whereCondition);

    long total = getTotalDataCount(whereCondition);

    return new PageResponseDto<>(content, total);
  }

  public PageResponseDto<OwnerOrderSummaryResponseDto> findByOwnerWithPage(
      String customerUsername,
      PageRequestDto pageRequestDto,
      String keyword
  ) {
    BooleanBuilder whereCondition = getDefaultSearchCondition(customerUsername, keyword);

    List<OwnerOrderSummaryResponseDto> content = getContentByOwner(
        pageRequestDto, whereCondition);

    long total = getTotalDataCount(whereCondition);

    return new PageResponseDto<>(content, total);
  }

  public PageResponseDto<ManagerOrderSummaryResponseDto> findByManagerWithPage(
      String customerUsername,
      PageRequestDto pageRequestDto,
      String keyword
  ) {
    BooleanBuilder whereCondition = getDefaultSearchCondition(customerUsername, keyword);

    List<ManagerOrderSummaryResponseDto> content = getContentByManager(
        pageRequestDto, whereCondition);

    long total = getTotalDataCount(whereCondition);

    return new PageResponseDto<>(content, total);
  }


  public PageResponseDto<OwnerOrderSummaryResponseDto> searchByOwnerWithPage(
      Long ownerId,
      UUID shopId,
      PageRequestDto pageRequestDto,
      String keyword
  ) {
    List<OwnerOrderSummaryResponseDto> content = getContentByOwner(
        pageRequestDto, getOwnerSearchCondition(ownerId, shopId, keyword));

    long total = getTotalDataCount(getOwnerSearchCondition(ownerId, shopId, keyword));

    return new PageResponseDto<>(content, total);
  }

  public PageResponseDto<ManagerOrderSummaryResponseDto> searchByManagerWithPage(
      String customerUsername,
      UUID shopId,
      PageRequestDto pageRequestDto,
      String keyword
  ) {
    List<ManagerOrderSummaryResponseDto> content = getContentByManager(
        pageRequestDto, getManagerSearchCondition(customerUsername, shopId, keyword));

    long total = getTotalDataCount(getManagerSearchCondition(customerUsername, shopId, keyword));

    return new PageResponseDto<>(content, total);
  }

  /**
   * 페이징 결과 조회 메서드
   */
  private List<CustomerOrderSummaryResponseDto> getContentByCustomer(
      PageRequestDto pageRequestDto,
      BooleanBuilder whereCond
  ) {
    return jpaQueryFactory
        .select(new QCustomerOrderSummaryResponseDto(order))
        .from(order)
        .where(whereCond)
        .offset(pageRequestDto.getFirstIndex())
        .limit(pageRequestDto.getSize())
        .orderBy(getOrderConditions(pageRequestDto))
        .fetch();
  }

  private List<OwnerOrderSummaryResponseDto> getContentByOwner(
      PageRequestDto pageRequestDto,
      BooleanBuilder whereCond
  ) {
    return jpaQueryFactory
        .select(new QOwnerOrderSummaryResponseDto(order))
        .from(order)
        .where(whereCond)
        .offset(pageRequestDto.getFirstIndex())
        .limit(pageRequestDto.getSize())
        .orderBy(getOrderConditions(pageRequestDto))
        .fetch();
  }

  private List<ManagerOrderSummaryResponseDto> getContentByManager(
      PageRequestDto pageRequestDto,
      BooleanBuilder whereCond
  ) {
    return jpaQueryFactory
        .select(new QManagerOrderSummaryResponseDto(order))
        .from(order)
        .where(whereCond)
        .offset(pageRequestDto.getFirstIndex())
        .limit(pageRequestDto.getSize())
        .orderBy(getOrderConditions(pageRequestDto))
        .fetch();
  }

  /**
   * 조회 조건
   */
  private BooleanBuilder getDefaultSearchCondition(String customerUsername, String keyword) {
    return new BooleanBuilder()
        .and(deletedAtIsNull())
        .and(customerUsernameEq(customerUsername))
        .and(shopNameOrOrderMenuNameEq(keyword));
  }

  private BooleanBuilder getOwnerSearchCondition(Long ownerId, UUID shopId, String keyword) {
    return new BooleanBuilder()
        .and(deletedAtIsNull())
        .and(ownerIdEq(ownerId))
        .and(shopIdEq(shopId))
        .and(shopNameOrOrderMenuNameEq(keyword));
  }

  private BooleanBuilder getManagerSearchCondition(
      String customerUsername,
      UUID shopId,
      String keyword
  ) {
    return new BooleanBuilder()
        .and(deletedAtIsNull())
        .and(customerUsernameEq(customerUsername))
        .and(shopIdEq(shopId))
        .and(shopNameOrOrderMenuNameEq(keyword));
  }

  /**
   * 전체 데이터 수 조회
   */
  private long getTotalDataCount(BooleanBuilder whereCondition) {
    return Optional.ofNullable(
            jpaQueryFactory
                .select(order.count())
                .from(order)
                .where(whereCondition)
                .fetchOne()
        )
        .orElse(0L);
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

  private BooleanExpression deletedAtIsNull() {
    return order.deletedAt.isNull();
  }

  private BooleanExpression customerUsernameEq(String customerUsername) {
    return customerUsername == null ? null : order.member.username.eq(customerUsername);
  }

  private BooleanExpression shopIdEq(UUID shopId) {
    return shopId == null ? null : order.shop.id.eq(shopId);
  }

  private BooleanExpression ownerIdEq(Long ownerId) {
    return ownerId == null ? null : order.shop.member.id.eq(ownerId);
  }

  private BooleanExpression shopNameOrOrderMenuNameEq(String keyword) {
    return keyword == null ? null : shopNameEq(keyword).or(orderMenuNameEq(keyword));
  }

  private BooleanExpression shopNameEq(String keyword) {
    return keyword == null ? null : order.shop.name.containsIgnoreCase(keyword);
  }

  private BooleanExpression orderMenuNameEq(String keyword) {
    return keyword == null ? null : order.orderMenuList.any().name.containsIgnoreCase(keyword);
  }
}
