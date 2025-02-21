package com.fourseason.delivery.domain.payment.service;


import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.entity.Role;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.payment.dto.request.CreatePaymentRequestDto;
import com.fourseason.delivery.domain.payment.dto.response.PaymentResponseDto;
import com.fourseason.delivery.domain.payment.entity.Payment;
import com.fourseason.delivery.domain.payment.exception.PaymentErrorCode;
import com.fourseason.delivery.domain.payment.repository.PaymentRepository;
import com.fourseason.delivery.domain.payment.repository.PaymentRepositoryCustom;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentRepositoryCustom paymentRepositoryCustom;

    @InjectMocks
    private PaymentService paymentService;

    private Member mockMember;
    private Payment mockPayment;
    private UUID paymentId;

    @BeforeEach
    void setUp() {
        // Given: mock Member 객체 생성
        mockMember = Member.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password("password123")
                .phoneNumber("010-1234-5678")
                .role(Role.CUSTOMER) // 가정한 Role 값 설정
                .build();
    }

    @Test
    void testFindPaymentList() {
        // Given
        PageRequestDto pageRequestDto = new PageRequestDto();
        PageResponseDto<PaymentResponseDto> mockResponse = new PageResponseDto<>();
        when(paymentRepositoryCustom.findPaymentListByMemberWithPage(pageRequestDto, mockMember))
                .thenReturn(mockResponse);

        // When
        PageResponseDto<PaymentResponseDto> result = paymentService.findPaymentList(pageRequestDto, mockMember);

        // Then
        assertEquals(mockResponse, result);
        verify(paymentRepositoryCustom, times(1)).findPaymentListByMemberWithPage(pageRequestDto, mockMember);
    }

    @Test
    void testGetPayment() {
        // Given
        when(paymentRepository.findByIdAndDeletedAtIsNotNull(paymentId)).thenReturn(Optional.of(mockPayment));

        // When
        PaymentResponseDto result = paymentService.getPayment(paymentId, mockMember);

        // Then
        assertNotNull(result);
        verify(paymentRepository, times(1)).findByIdAndDeletedAtIsNotNull(paymentId);
    }

    @Test
    void testRegisterPayment() {
        // Given
        CreatePaymentRequestDto requestDto = new CreatePaymentRequestDto();
        Order order = new Order(); // 실제 서비스에서는 Order가 DB에서 조회될 것이지만, 여기서는 간단히 mock 처리
        Payment payment = Payment.addOf(requestDto, "DONE", order, mockMember);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        URI resultUri = paymentService.registerPayment(requestDto, mockMember);

        // Then
        assertNotNull(resultUri);
        assertEquals(ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(payment.getId()).toUri(), resultUri);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testCancelPayment() {
        // Given
        when(paymentRepository.findByIdAndDeletedAtIsNotNull(paymentId)).thenReturn(Optional.of(mockPayment));

        // When
        URI resultUri = paymentService.cancelPayment(paymentId, mockMember);

        // Then
        assertNotNull(resultUri);
        verify(paymentRepository, times(1)).findByIdAndDeletedAtIsNotNull(paymentId);
    }

    @Test
    void testDeletePayment() {
        // Given
        when(paymentRepository.findByIdAndDeletedAtIsNotNull(paymentId)).thenReturn(Optional.of(mockPayment));

        // When
        paymentService.deletePayment(paymentId, mockMember);

        // Then
        verify(paymentRepository, times(1)).findByIdAndDeletedAtIsNotNull(paymentId);
    }

    @Test
    void testCheckPayment_ThrowsException_WhenPaymentNotFound() {
        // Given
        when(paymentRepository.findByIdAndDeletedAtIsNotNull(paymentId)).thenReturn(Optional.empty());

        // When
        CustomException exception = assertThrows(CustomException.class,
                () -> paymentService.getPayment(paymentId, mockMember));

        // Then
        assertEquals(PaymentErrorCode.PAYMENT_NOT_FOUND, exception.getErrorCode());
        verify(paymentRepository, times(1)).findByIdAndDeletedAtIsNotNull(paymentId);
    }

    @Test
    void testCheckPayment_ThrowsException_WhenUnauthorized() {
        // Given
        Member anotherMember = new Member();
        anotherMember.setId(UUID.randomUUID());
        mockPayment.setMember(anotherMember);
        when(paymentRepository.findByIdAndDeletedAtIsNotNull(paymentId)).thenReturn(Optional.of(mockPayment));

        // When
        CustomException exception = assertThrows(CustomException.class,
                () -> paymentService.getPayment(paymentId, mockMember));

        // Then
        assertEquals(PaymentErrorCode.PAYMENT_NOT_UNAUTHORIZED, exception.getErrorCode());
    }
}

