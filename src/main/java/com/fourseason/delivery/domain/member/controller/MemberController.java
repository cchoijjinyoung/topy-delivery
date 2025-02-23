package com.fourseason.delivery.domain.member.controller;

import com.fourseason.delivery.domain.member.dto.request.MemberRequestDto;
import com.fourseason.delivery.domain.member.dto.response.MemberResponseDto;
import com.fourseason.delivery.domain.member.service.MemberService;
import com.fourseason.delivery.domain.review.dto.response.ReviewResponseDto;
import com.fourseason.delivery.domain.review.entity.Review;
import com.fourseason.delivery.domain.shop.dto.response.ShopResponseDto;
import com.fourseason.delivery.global.auth.CustomPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members/me")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 마이페이지 조회
     */
    @GetMapping
    public ResponseEntity<MemberResponseDto> getMemberInfo(@AuthenticationPrincipal CustomPrincipal principal) {
        return ResponseEntity.ok(memberService.getMemberInfo(principal.getId()));
    }


    /**
     * 마이페이지 수정
     */
    @PutMapping
    public ResponseEntity<MemberResponseDto> updateInfo(@AuthenticationPrincipal CustomPrincipal principal,
                                                        @RequestBody  MemberRequestDto memberRequestDto) {
        return ResponseEntity.ok(memberService.updateInfo(principal.getId(), memberRequestDto));
    }


    /**
     * 리뷰 내역 조회
     */
    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewResponseDto>> getReviewList(@AuthenticationPrincipal CustomPrincipal principal) {
        return ResponseEntity.ok(memberService.getReviewList(principal.getId()));
    }



    /**
     * 회원 탈퇴
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteMember(@AuthenticationPrincipal CustomPrincipal principal) {
        memberService.deleteMember(principal.getId());
        return ResponseEntity.ok().build();
    }
}
