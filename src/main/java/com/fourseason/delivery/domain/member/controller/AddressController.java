package com.fourseason.delivery.domain.member.controller;

import com.fourseason.delivery.domain.member.dto.request.AddressRequestDto;
import com.fourseason.delivery.domain.member.dto.response.AddressResponseDto;
import com.fourseason.delivery.domain.member.service.AddressService;
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
    public ResponseEntity<List<AddressResponseDto>> getAddressList() {
        return ResponseEntity.ok(addressService.getAddressList());
    }


    /**
     * 주소 추가
     */
    @PostMapping
    public ResponseEntity<AddressResponseDto> addAddress(@RequestBody AddressRequestDto addressRequestDto) {
        return ResponseEntity.ok(addressService.addAddress(addressRequestDto));
    }


    /**
     * 주소 수정
     */
    @PutMapping("/{address_id}")
    public ResponseEntity<AddressResponseDto> updateAddress(@PathVariable UUID addressId,
                                                            @RequestBody AddressRequestDto addressRequestDto) {
        return ResponseEntity.ok(addressService.updateAddress(addressId, addressRequestDto));
    }


    /**
     * 주소 삭제
     */
    @DeleteMapping("/{address_id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable UUID addressId) {
        addressService.deleteAddress(addressId);
        return ResponseEntity.ok().build();
    }
}
