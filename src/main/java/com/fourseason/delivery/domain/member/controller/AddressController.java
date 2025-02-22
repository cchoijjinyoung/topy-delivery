package com.fourseason.delivery.domain.member.controller;

import com.fourseason.delivery.domain.member.dto.request.AddressAddRequestDto;
import com.fourseason.delivery.domain.member.dto.request.AddressUpdateRequestDto;
import com.fourseason.delivery.domain.member.dto.response.AddressAddResponseDto;
import com.fourseason.delivery.domain.member.dto.response.AddressUpdateResponseDto;
import com.fourseason.delivery.domain.member.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/members/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /**
     * 주소 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<AddressAddResponseDto>> getAddressList() {
        return ResponseEntity.ok(addressService.getAddressList());
    }


    /**
     * 주소 추가
     */
    @PostMapping
    public ResponseEntity<AddressAddResponseDto> addAddress(@Valid @RequestBody AddressAddRequestDto addressAddRequestDto) {
        return ResponseEntity.ok(addressService.addAddress(addressAddRequestDto));
    }


    /**
     * 주소 수정
     */
    @PutMapping("/{address_id}")
    public ResponseEntity<AddressUpdateResponseDto> updateAddress(@PathVariable("address_id") UUID addressId,
                                                                  @Valid @RequestBody AddressUpdateRequestDto addressUpdateRequestDto) {
        return ResponseEntity.ok(addressService.updateAddress(addressId, addressUpdateRequestDto));
    }


    /**
     * 주소 삭제
     */
    @DeleteMapping("/{address_id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable("address_id") UUID addressId) {
        addressService.deleteAddress(addressId);
        return ResponseEntity.ok().build();
    }
}
