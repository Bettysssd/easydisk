package com.easypan.controller;
import com.easypan.entity.enums.ResponseCodeEnum;
import com.easypan.entity.vo.ResponseVO;
import com.easypan.exception.BusinessException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class AGlobalExceptionHandlerController extends ABaseController {

    @SuppressWarnings("checkstyle:ConstantName")
    private static final Logger logger = LoggerFactory.getLogger(AGlobalExceptionHandlerController.class);

    @ExceptionHandler(value = Exception.class)
    Object handleException(Exception e, HttpServletRequest request) {
        logger.error("请求错误，请求地址{},错误信息:", request.getRequestURL(), e);

        if (e instanceof NoHandlerFoundException) {
            //404
            return ResponseVO
                    .builder()
                    .code(ResponseCodeEnum.CODE_404.getCode())
                    .info(ResponseCodeEnum.CODE_404.getMsg())
                    .status(STATUC_ERROR)
                    .build();
        } else if (e instanceof BusinessException) {
            //业务错误
            BusinessException biz = (BusinessException) e;
            return ResponseVO
                    .builder()
                    .code(biz.getCode() == null ? ResponseCodeEnum.CODE_600.getCode() : biz.getCode())
                    .info(biz.getMessage())
                    .status(STATUC_ERROR)
                    .build();
        } else if (e instanceof BindException || e instanceof MethodArgumentTypeMismatchException) {
            //参数类型错误
            return ResponseVO
                    .builder()
                    .code(ResponseCodeEnum.CODE_600.getCode())
                    .info(ResponseCodeEnum.CODE_600.getMsg())
                    .status(STATUC_ERROR)
                    .build();
        } else if (e instanceof DuplicateKeyException) {
            //主键冲突
            return ResponseVO
                    .builder()
                    .code(ResponseCodeEnum.CODE_601.getCode())
                    .info(ResponseCodeEnum.CODE_601.getMsg())
                    .status(STATUC_ERROR)
                    .build();
        } else {
            return ResponseVO
                    .builder()
                    .code(ResponseCodeEnum.CODE_500.getCode())
                    .info(ResponseCodeEnum.CODE_500.getMsg())
                    .status(STATUC_ERROR)
                    .build();
        }
    }
}
