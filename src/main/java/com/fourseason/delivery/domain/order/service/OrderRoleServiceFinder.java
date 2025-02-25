package com.fourseason.delivery.domain.order.service;

import com.fourseason.delivery.domain.member.entity.Role;
import com.fourseason.delivery.domain.order.exception.OrderErrorCode;
import com.fourseason.delivery.global.exception.CustomException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderRoleServiceFinder {

  private final List<OrderRoleService> orderRoleServiceList;

  public OrderRoleService find(String role) {
    return orderRoleServiceList.stream()
        .filter(orderRoleService -> orderRoleService.supports().equals(Role.valueOf(role)))
        .findFirst()
        .orElseThrow(() -> new CustomException(OrderErrorCode.NOT_EXIST_MEMBER_ROLE));
  }
}
