package com.fourseason.delivery.domain.payment.service;

import com.fourseason.delivery.domain.member.entity.Member;
import com.fourseason.delivery.domain.member.exception.MemberErrorCode;
import com.fourseason.delivery.domain.member.repository.MemberRepository;
import com.fourseason.delivery.domain.order.entity.Order;
import com.fourseason.delivery.domain.order.entity.OrderStatus;
import com.fourseason.delivery.domain.order.exception.OrderErrorCode;
import com.fourseason.delivery.domain.order.repository.OrderRepository;
import com.fourseason.delivery.domain.payment.dto.request.CancelPaymentRequestDto;
import com.fourseason.delivery.domain.payment.dto.request.CreatePaymentRequestDto;
import com.fourseason.delivery.domain.payment.dto.response.PaymentResponseDto;
import com.fourseason.delivery.domain.payment.entity.Payment;
import com.fourseason.delivery.domain.payment.exception.PaymentErrorCode;
import com.fourseason.delivery.domain.payment.repository.PaymentRepository;
import com.fourseason.delivery.domain.payment.repository.PaymentRepositoryCustom;
import com.fourseason.delivery.domain.shop.entity.Shop;
import com.fourseason.delivery.domain.shop.exception.ShopErrorCode;
import com.fourseason.delivery.domain.shop.repository.ShopRepository;
import com.fourseason.delivery.global.auth.CustomPrincipal;
import com.fourseason.delivery.global.dto.PageRequestDto;
import com.fourseason.delivery.global.dto.PageResponseDto;
import com.fourseason.delivery.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentRepositoryCustom paymentRepositoryCustom;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final PaymentExternalService paymentExternalService;
    private final ShopRepository shopRepository;
    private final PaymentCompensatingService paymentCompensatingService;

    /**
     * 사용자 결제 전체 조회
     */
    @Transactional(readOnly = true)
    public PageResponseDto<PaymentResponseDto> findPaymentListForCustomer(final PageRequestDto pageRequestDto, final CustomPrincipal customPrincipal) {

        return paymentRepositoryCustom.findPaymentListByMemberWithPage(pageRequestDto, checkMember(customPrincipal.getId()));
    }

    /**
     * 사용자 결제 상세 조회
     */
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentForCustomer(final UUID paymentId, final CustomPrincipal customPrincipal) {
        Payment payment = checkPayment(paymentId, checkMember(customPrincipal.getId()));

        return PaymentResponseDto.of(payment);
    }

    /**
     * 가게주인 결제 전체 조회
     */
    @Transactional(readOnly = true)
    public PageResponseDto<PaymentResponseDto> findPaymentListByShop(final PageRequestDto pageRequestDto, final UUID shopId, final CustomPrincipal customPrincipal) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new CustomException(ShopErrorCode.SHOP_NOT_FOUND));
        if (!shop.getMember().getId().equals(customPrincipal.getId())) {
            throw new CustomException(PaymentErrorCode.PAYMENT_FORBIDDEN);
        }

        return paymentRepositoryCustom.findPaymentListByShopWithPage(pageRequestDto, shop);
    }

    /**
     * 가게주인 결제 상세 조회
     */
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentListForOwner(final UUID paymentId, final CustomPrincipal customPrincipal) {
        Payment payment = checkOwner(paymentId, checkMember(customPrincipal.getId()));

        return PaymentResponseDto.of(payment);
    }

    /**
     * 결제 승인, 등록
     */
    public CompletableFuture<URI> confirmAndRegister(final CreatePaymentRequestDto createPaymentRequestDto, final CustomPrincipal customPrincipal) {
        // 결제전 검증할 것
        Member member = checkMember(customPrincipal.getId());
        Order order = checkOrder(createPaymentRequestDto.orderId(), member);
        checkPaymentAmount(createPaymentRequestDto, order);
        // 결제 승인 진행
        String paymentResult = paymentExternalService.confirmPayment(createPaymentRequestDto);
        System.out.println(paymentResult);
        // 승인 결과 mapping후 저장
        return paymentCompensatingService.registerPaymentWithRetry(paymentResult, order, member);
    }

    /**
     * 결제 취소 고객
     */
    @Transactional
    public URI cancelPaymentForCustomer(final UUID paymentId, final CancelPaymentRequestDto cancelPaymentRequestDto, final CustomPrincipal customPrincipal) {
        // 취소전 검증할 것
        Member member = checkMember(customPrincipal.getId());
        Payment payment = checkPayment(paymentId, member);
        checkCancelCondition(payment);
        return cancelPayment(cancelPaymentRequestDto, payment);
    }

    /**
     * 결제 취소 가게주인
     */
    @Transactional
    public URI cancelPaymentForOwner(final UUID paymentId, final CancelPaymentRequestDto cancelPaymentRequestDto, final CustomPrincipal customPrincipal) {
        // 취소전 검증할 것
        Member owner = checkMember(customPrincipal.getId());
        Payment payment = checkOwner(paymentId, owner);
        return cancelPayment(cancelPaymentRequestDto, payment);
    }

    /**
     * 결제 취소
     */
    @Transactional
    public URI cancelPayment(final CancelPaymentRequestDto cancelPaymentRequestDto, Payment payment) {
        // 취소로 변경
        payment.cancelOf(cancelPaymentRequestDto);
        // 취소 요청
        String paymentResult = paymentExternalService.cancelPayment(cancelPaymentRequestDto, payment.getPaymentKey());
        System.out.println(paymentResult);

        return ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(payment.getId())
                .toUri();
    }

    /**
     * 관리자 결제 전체 조회
     */
    @Transactional(readOnly = true)
    public PageResponseDto<PaymentResponseDto> findPaymentListForCustomer(final PageRequestDto pageRequestDto) {

        return paymentRepositoryCustom.findPaymentListWithPage(pageRequestDto);
    }

    /**
     * 관리자 결제 상세 조회
     */
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentForCustomer(final UUID paymentId) {
        Payment payment = checkPayment(paymentId);

        return PaymentResponseDto.of(payment);
    }

    /**
     * 결제 삭제
     */
    @Transactional
    public void deletePayment(final UUID paymentId, final CustomPrincipal customPrincipal) {
        Payment payment = checkPayment(paymentId);

        payment.deleteOf(customPrincipal.getName());
    }

    /**
     * 검증 및 에러처리
     */
    private void checkPaymentAmount(final CreatePaymentRequestDto createPaymentRequestDto, final Order order){
        if (order.getTotalPrice() != createPaymentRequestDto.amount()) {
            throw new CustomException(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    private void checkCancelCondition(final Payment payment) {
        boolean checkOrderStatus = !payment.getOrder().getOrderStatus().equals(OrderStatus.PENDING);
        boolean checkPaymentCreatedAt = Duration.between(payment.getCreatedAt(), LocalDateTime.now()).toMinutes() >= 5;
        if (checkOrderStatus && checkPaymentCreatedAt) {
            throw new CustomException(PaymentErrorCode.PAYMENT_CANCEL_TIMEOUT);
        }
    }


    private Order checkOrder(final UUID orderId, final Member member) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new CustomException(OrderErrorCode.ORDER_NOT_FOUND));
        if (order.getMember().getDeletedAt() != null) {
            throw new CustomException(MemberErrorCode.MEMBER_NOT_FOUND);
        }
        if (!order.getMember().getId().equals(member.getId())) {
            throw new CustomException(OrderErrorCode.NOT_ORDERED_BY_CUSTOMER);
        }
        return order;
    }

    private Payment checkPayment(final UUID paymentId, final Member member) {
        Payment payment = paymentRepository.findByIdAndDeletedAtIsNull(paymentId)
                .orElseThrow(() -> new CustomException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        if (!payment.getMember().getId().equals(member.getId())) {
            throw new CustomException(PaymentErrorCode.PAYMENT_FORBIDDEN);
        }
        return payment;
    }

    private Payment checkOwner(final UUID paymentId, final Member owner) {
        Payment payment = paymentRepository.findByIdAndDeletedAtIsNull(paymentId)
                .orElseThrow(() -> new CustomException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        if (!payment.getOrder().getShop().getMember().getId().equals(owner.getId())) {
            throw new CustomException(PaymentErrorCode.PAYMENT_FORBIDDEN);
        }
        return payment;
    }

    private Payment checkPayment(final UUID paymentId) {
        return paymentRepository.findByIdAndDeletedAtIsNull(paymentId)
                .orElseThrow(() -> new CustomException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }

    private Member checkMember(final Long memberId) {
        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));
        return member;
    }
}
