package com.fourseason.delivery.domain.payment.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaymentHtmlController {

//    @GetMapping("/checkout")
//    public String checkout(@RequestParam final String orderId,
//                           @RequestParam final int amount
//    ) {
//        return "/checkout";
//    }

    /**
     * 인증성공처리
     * @param request
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/success", method = RequestMethod.GET)
    public String paymentRequest(HttpServletRequest request, Model model) throws Exception {
        return "/success";
    }

    @RequestMapping(value = "/checkout", method = RequestMethod.GET)
    public String index(HttpServletRequest request, Model model) throws Exception {
        return "/checkout";
    }

    /**
     * 인증실패처리
     * @param request
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/fail", method = RequestMethod.GET)
    public String failPayment(HttpServletRequest request, Model model) throws Exception {
        String failCode = request.getParameter("code");
        String failMessage = request.getParameter("message");

        model.addAttribute("code", failCode);
        model.addAttribute("message", failMessage);

        return "/fail";
    }
}
