package com.fourseason.delivery.fixture;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.entity.Role;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.test.util.ReflectionTestUtils;

public class MemberFixture {

  private static final AtomicLong idCounter = new AtomicLong(1);

  public static long nextId() {
    return idCounter.getAndIncrement();
  }

  public static Member createMember(Role role) {
    Member member = Member.builder()
        .role(role)
        .build();

    ReflectionTestUtils.setField(member, "id", nextId());
    return member;
  }
}
