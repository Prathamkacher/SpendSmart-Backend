package com.spendsmart.payment.controller;

import com.spendsmart.shared.exception.BaseGlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseGlobalExceptionHandler {
}
