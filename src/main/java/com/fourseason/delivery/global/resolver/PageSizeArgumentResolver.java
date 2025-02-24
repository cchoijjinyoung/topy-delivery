package com.fourseason.delivery.global.resolver;

import com.fourseason.delivery.global.exception.CustomException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

@Component
public class PageSizeArgumentResolver implements HandlerMethodArgumentResolver {

    private static final List<Integer> ALLOWED_PAGE_SIZES = List.of(10, 20, 30);
    private static final int DEFAULT_SIZE = 10;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(PageSize.class) &&
                parameter.getParameterType().equals(int.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        String paramValue = webRequest.getParameter("size");

        if (paramValue == null) {
            return DEFAULT_SIZE;
        }

        int page = Integer.parseInt(paramValue);
        return ALLOWED_PAGE_SIZES.contains(page) ? page : 10;
    }
}
