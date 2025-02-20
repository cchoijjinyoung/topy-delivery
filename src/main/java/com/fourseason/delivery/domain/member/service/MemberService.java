package com.fourseason.delivery.domain.member.service;

import com.fourseason.delivery.domain.member.MemberErrorCode;
import com.fourseason.delivery.domain.member.dto.request.MemberRequestDto;
import com.fourseason.delivery.domain.member.dto.response.MemberResponseDto;
import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.repository.MemberRepository;
import com.fourseason.delivery.domain.order.repository.OrderRepository;
import com.fourseason.delivery.domain.review.dto.response.ReviewResponseDto;
import com.fourseason.delivery.domain.review.entity.Review;
import com.fourseason.delivery.domain.review.entity.ReviewImage;
import com.fourseason.delivery.domain.review.repository.ReviewImageRepository;
import com.fourseason.delivery.domain.review.repository.ReviewRepository;
import com.fourseason.delivery.domain.shop.dto.response.ShopResponseDto;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.domain.shop.entity.ShopImage;
import com.fourseason.delivery.domain.shop.repository.ShopImageRepository;
import com.fourseason.delivery.domain.shop.repository.ShopRepository;
import com.fourseason.delivery.global.auth.JwtUtil;
import com.fourseason.delivery.global.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;


    @Transactional(readOnly = true)
    public MemberResponseDto getMemberInfo() {
        Member member = getAuthenticatedMember();

        return MemberResponseDto.of(member);
    }

    @Transactional
    public MemberResponseDto updateInfo(MemberRequestDto memberRequestDto) {
        Member member = getAuthenticatedMember();

        member.updateOf(memberRequestDto);

        return MemberResponseDto.of(member);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewList() {
        Member member = getAuthenticatedMember();

        List<Review> reviews = reviewRepository.findByMemberAndDeletedAtIsNull(member);

        List<ReviewResponseDto> responseList = reviews.stream()
                .map(review -> {
                    List<ReviewImage> images = reviewImageRepository.findByReviewIdAndDeletedAtIsNull(review.getId());
                    return ReviewResponseDto.of(review, images);
                })
                .toList();

        return responseList;
    }

    @Transactional
    public void deleteMember() {
        Member member = getAuthenticatedMember();

        member.deleteOf(member.getUsername());
    }



    private Member getAuthenticatedMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return memberRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}
