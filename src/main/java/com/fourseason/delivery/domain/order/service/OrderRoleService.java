package com.fourseason.delivery.domain.order.service;

import com.fourseason.delivery.domain.member.entity.Role;
import com.fourseason.delivery.domain.order.dto.response.OrderDetailsResponseDto;
import com.fourseason.delivery.domain.order.dto.response.OrderSummaryResponseDto;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import java.util.UUID;

public interface OrderRoleService {

  Role supports();

  OrderDetailsResponseDto getOrder(UUID orderId, Long loginMemberId);

  PageResponseDto<? extends OrderSummaryResponseDto> getOrderList(
      String username, String loginMemberUsername, PageRequestDto pageRequestDto, String keyword);

  void cancelOrder(UUID orderId, Long loginMemberId);
}
