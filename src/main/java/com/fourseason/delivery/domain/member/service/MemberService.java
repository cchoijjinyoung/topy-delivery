package com.fourseason.delivery.domain.member.service;

import com.fourseason.delivery.domain.member.dto.request.MemberRequestDto;
import com.fourseason.delivery.domain.member.dto.response.MemberResponseDto;
import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.review.dto.response.ReviewResponseDto;
import com.fourseason.delivery.domain.review.entity.Review;
import com.fourseason.delivery.domain.review.entity.ReviewImage;
import com.fourseason.delivery.domain.review.repository.ReviewImageRepository;
import com.fourseason.delivery.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberQueryService memberQueryService;
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;


    @Transactional(readOnly = true)
    public MemberResponseDto getMemberInfo(Long memberId) {
        Member member = memberQueryService.findActiveMember(memberId);
        return MemberResponseDto.of(member);
    }

    @Transactional
    public MemberResponseDto updateInfo(Long memberId, MemberRequestDto memberRequestDto) {
        Member member = memberQueryService.findActiveMember(memberId);
        member.updateOf(memberRequestDto);
        return MemberResponseDto.of(member);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getReviewList(Long memberId) {
        Member member = memberQueryService.findActiveMember(memberId);

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
    public void deleteMember(Long memberId) {
        Member member = memberQueryService.findActiveMember(memberId);
        member.deleteOf(member.getUsername());
    }
}
