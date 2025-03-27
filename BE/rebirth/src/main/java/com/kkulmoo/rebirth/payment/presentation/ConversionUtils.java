package com.kkulmoo.rebirth.payment.presentation;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import java.util.HashMap;
import java.util.Map;

public class ConversionUtils {
    public static MultiValueMap<String, String> toQueryParams(Object dto) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        BeanWrapper beanWrapper = new BeanWrapperImpl(dto);

        for (var descriptor : beanWrapper.getPropertyDescriptors()) {
            String propertyName = descriptor.getName();
            Object propertyValue = beanWrapper.getPropertyValue(propertyName);
            if (propertyValue != null) {
                queryParams.add(propertyName, propertyValue.toString());
            }
        }

        return queryParams;
    }
}