package com.fourseason.delivery.domain.member.service;

import com.fourseason.delivery.domain.member.MemberErrorCode;
import com.fourseason.delivery.domain.member.dto.request.AddressAddRequestDto;
import com.fourseason.delivery.domain.member.dto.request.AddressUpdateRequestDto;
import com.fourseason.delivery.domain.member.dto.response.AddressAddResponseDto;
import com.fourseason.delivery.domain.member.dto.response.AddressUpdateResponseDto;
import com.fourseason.delivery.domain.member.entity.Address;
import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.exception.AddressErrorCode;
import com.fourseason.delivery.domain.member.repository.AddressRepository;
import com.fourseason.delivery.domain.member.repository.MemberRepository;
import com.fourseason.delivery.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final MemberRepository memberRepository;
    private final AddressRepository addressRepository;


    @Transactional(readOnly = true)
    public List<AddressAddResponseDto> getAddressList(Long memberId) {
        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        List<Address> addresses = addressRepository.findByMemberAndDeletedAtIsNull(member);

        return addresses.stream()
                .map(AddressAddResponseDto::of)
                .toList();
    }

    @Transactional
    public AddressAddResponseDto addAddress(Long memberId, AddressAddRequestDto addressAddRequestDto) {
        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));


        Address address = Address.addOf(addressAddRequestDto, member);
        Address savedAddress = addressRepository.save(address);

        return AddressAddResponseDto.of(savedAddress);
    }

    @Transactional
    public AddressUpdateResponseDto updateAddress(Long memberId, UUID addressId, AddressUpdateRequestDto addressUpdateRequestDto) {
        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));


        Address address = addressRepository.findByIdAndDeletedAtIsNull(addressId)
                .orElseThrow(() -> new CustomException(AddressErrorCode.ADDRESS_NOT_FOUND));

        if (!address.getMember().getId().equals(member.getId())) {
            throw new CustomException(AddressErrorCode.ADDRESS_NOT_BELONG_TO_MEMBER);
        }

        address.updateOf(addressUpdateRequestDto);

        return AddressUpdateResponseDto.of(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(Long memberId, UUID addressId) {
        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));


        Address address = addressRepository.findByIdAndDeletedAtIsNull(addressId)
                .orElseThrow(() -> new CustomException(AddressErrorCode.ADDRESS_NOT_FOUND));

        if (!address.getMember().getId().equals(member.getId())) {
            throw new CustomException(AddressErrorCode.ADDRESS_NOT_BELONG_TO_MEMBER);
        }

        address.deleteOf(member.getUsername());
    }
}
