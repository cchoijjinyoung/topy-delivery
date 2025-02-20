package com.fourseason.delivery.domain.member.controller;

import com.fourseason.delivery.domain.member.dto.request.MemberRequestDto;
import com.fourseason.delivery.domain.member.dto.response.MemberResponseDto;
import com.fourseason.delivery.domain.member.service.MemberService;
import com.fourseason.delivery.domain.review.dto.response.ReviewResponseDto;
import com.fourseason.delivery.domain.review.entity.Review;
import com.fourseason.delivery.domain.shop.dto.response.ShopResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<MemberResponseDto> getMemberInfo() {
        return ResponseEntity.ok(memberService.getMemberInfo());
    }


    /**
     * 마이페이지 수정
     */
    @PutMapping
    public ResponseEntity<MemberResponseDto> updateInfo(@RequestBody  MemberRequestDto memberRequestDto) {
        return ResponseEntity.ok(memberService.updateInfo(memberRequestDto));
    }


    /**
     * 리뷰 내역 조회
     */
    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewResponseDto>> getReviewList() {
        return ResponseEntity.ok(memberService.getReviewList());
    }



    /**
     * 회원 탈퇴
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteMember() {
        memberService.deleteMember();
        return ResponseEntity.ok().build();
    }
}
