package com.fourseason.delivery.domain.member.service;

import com.fourseason.delivery.domain.member.MemberErrorCode;
import com.fourseason.delivery.domain.member.dto.request.AddressRequestDto;
import com.fourseason.delivery.domain.member.dto.response.AddressResponseDto;
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
    public List<AddressResponseDto> getAddressList() {
        Member member = getAuthenticatedMember();

        List<Address> addresses = addressRepository.findByMemberAndDeletedAtIsNull(member);

        return addresses.stream()
                .map(AddressResponseDto::of)
                .toList();
    }

    @Transactional
    public AddressResponseDto addAddress(AddressRequestDto addressRequestDto) {
        Member member = getAuthenticatedMember();

        Address address = Address.addOf(addressRequestDto, member);
        Address savedAddress = addressRepository.save(address);

        return AddressResponseDto.of(savedAddress);
    }

    @Transactional
    public AddressResponseDto updateAddress(UUID addressId, AddressRequestDto addressRequestDto) {
        Member member = getAuthenticatedMember();

        Address address = addressRepository.findByIdAndDeletedAtIsNull(addressId)
                .orElseThrow(() -> new CustomException(AddressErrorCode.ADDRESS_NOT_FOUND));

        if (!address.getMember().getId().equals(member.getId())) {
            throw new CustomException(AddressErrorCode.ADDRESS_NOT_BELONG_TO_MEMBER);
        }

        address.updateOf(addressRequestDto);

        return AddressResponseDto.of(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(UUID addressId) {
        Member member = getAuthenticatedMember();

        Address address = addressRepository.findByIdAndDeletedAtIsNull(addressId)
                .orElseThrow(() -> new CustomException(AddressErrorCode.ADDRESS_NOT_FOUND));

        if (!address.getMember().getId().equals(member.getId())) {
            throw new CustomException(AddressErrorCode.ADDRESS_NOT_BELONG_TO_MEMBER);
        }

        address.deleteOf(member.getUsername());
    }


    private Member getAuthenticatedMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return memberRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}
