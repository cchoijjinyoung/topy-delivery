package com.fourseason.delivery.domain.payment.service;

import com.fourseason.delivery.domain.member.entity.Member;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentRepositoryCustom paymentRepositoryCustom;

    /**
     * 사용자 결제 전체 조회
     */
    @Transactional(readOnly = true)
    public PageResponseDto<PaymentResponseDto> findPaymentList(final PageRequestDto pageRequestDto, final Member member) {

        return paymentRepositoryCustom.findPaymentListByMemberWithPage(pageRequestDto, member);
    }

    /**
     * 사용자 결제 상세 조회
     */
    @Transactional(readOnly = true)
    public PaymentResponseDto getPayment(final UUID paymentId, final Member member) {
        Payment payment = checkPayment(paymentId, member);

        return PaymentResponseDto.of(payment);
    }

    /**
     * 결제 생성
     */

    /**
     * Todo:
     * payment객체 생성(결제 요청 과정) 기존에는 client쪽에서 전부 일임하여 진행하는 부분
     * 0. 테스트 html코드를 적용해보기 위해서 혹시 추가적인 dependency가 필요하려나?
     * 1. orderId를 담은 client의 결제 요청페이지 location 전달(임시로 html로 만들어진 주소로 orderId값만 받아서)
     *      (상세하게 할 경우 order객체를 보내서 id, 결제명, 금액까지)
     * 2. client에서 테스트 결제 요청진행 이후 받은 payment 객체로 그 결제 승인을 다시 server의 요청을 보냄(주소만 적절히 수정해 주면 될것 같음)
     */

    /**
     * payment 요청을 위한 order값 전달
     */
    public URI checkoutPayment(UUID orderId) {
        // 원래는 orderId로 order 조회후 값을 넣는다
        URI location = URI.create("/static/checkout?orderId="+ orderId + "&amount=" + 20000);
        return location;
    }


    /**
     * 결제 승인 과정
     * 1. 현재 order가 존재하는지 확인
     * 2. 확인된 orderId를 담아서 결제 승인처리 (paymentRestService 참조)
     * 3. 성공 or 실패 처리에 맞게 결제 정보 db에 저장
     * 4. response로 돌려줌
     *
     */
//    @Transactional
//    public URI registerPayment(final CreatePaymentRequestDto createPaymentRequestDto, final Member member) {
//
//        // Todo: 임시 order 객체 생성 실제로는 order객체 조회, 확인 필요
//        Order order = Order.builder().build();
//        // Todo: pb사에 결제 승인처리, 결제 성공확인, 받은 객체에서 status 값 적용, 현재는 임시로 "DONE" 사용
//        //
//        Payment newPayment = Payment.addOf(createPaymentRequestDto, "DONE", order, member);
//        paymentRepository.save(newPayment);
//
//        return ServletUriComponentsBuilder
//                .fromCurrentRequest()
//                .path("/{id}")
//                .buildAndExpand(newPayment.getId())
//                .toUri();
//    }

    /**
     * 결제 취소
     */
    @Transactional
    public URI cancelPayment(final UUID paymentId, final Member member) {
        Payment payment = checkPayment(paymentId, member);
        // Todo: pb사에 결제 취소처리, 취소 성공확인, 받은 객체에서 status 값 적용, 현재는 임시로 "CANCELED" 사용
        payment.cancelOf("CANCELED");

        return ServletUriComponentsBuilder
                .fromCurrentRequest()
                .build()
                .toUri();
    }

    /**
     * 결제 삭제
     */
    @Transactional
    public void deletePayment(final UUID paymentId, final Member member) {
        Payment payment = checkPayment(paymentId, member);

        payment.deleteOf(member.getUsername());
    }

    /**
     * 관리자 결제 전체 조회
     */
    @Transactional(readOnly = true)
    public PageResponseDto<PaymentResponseDto> findPaymentList(final PageRequestDto pageRequestDto) {

        return paymentRepositoryCustom.findPaymentListWithPage(pageRequestDto);
    }

    /**
     * 관리자 결제 상세 조회
     */
    @Transactional(readOnly = true)
    public PaymentResponseDto getPayment(final UUID paymentId) {
        Payment payment = checkPayment(paymentId);

        return PaymentResponseDto.of(payment);
    }

    /**
     * 승인처리관련
     */

    /**
     * 에러처리
     */
    private Payment checkPayment(final UUID paymentId, final Member member) {
        Payment payment = paymentRepository.findByIdAndDeletedAtIsNotNull(paymentId)
                .orElseThrow(() -> new CustomException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        if (!payment.getMember().getId().equals(member.getId())) {
            throw new CustomException(PaymentErrorCode.PAYMENT_NOT_UNAUTHORIZED);
        }

        return payment;
    }

    private Payment checkPayment(final UUID paymentId) {

        return paymentRepository.findByIdAndDeletedAtIsNotNull(paymentId)
                .orElseThrow(() -> new CustomException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }
}
