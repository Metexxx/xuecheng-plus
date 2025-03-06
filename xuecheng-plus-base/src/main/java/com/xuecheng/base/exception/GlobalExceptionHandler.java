package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description 全局异常处理器
 * @date 2022/9/6 11:29
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse methodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        List<String> msgList = new ArrayList<>();
        //将错误信息放在msgList
        bindingResult.getFieldErrors().stream().forEach(item -> msgList.add(item.getDefaultMessage()));
        //拼接错误信息
        String msg = StringUtils.join(msgList, ",");
        log.error("【系统异常】{}", msg);
        return new RestErrorResponse(msg);
    }

    @ResponseBody
    @ExceptionHandler(XueChengPlusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse customException(XueChengPlusException e) {
        log.error("【系统异常】{}", e.getErrMessage(), e);
        return new RestErrorResponse(e.getErrMessage());

    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse exception(Exception e) {
        log.error("【系统异常】{}", e.getMessage(), e);
        e.printStackTrace();
        if (e.getMessage().equals("不允许访问")) {
            return new RestErrorResponse("没有操作此功能的权限");
        }
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }

//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    public RestErrorResponse doMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
//        BindingResult bindingResult = exception.getBindingResult();
//        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
//        StringBuffer stringBuffer = new StringBuffer();
//        fieldErrors.forEach(fieldError -> stringBuffer.append(fieldError.getDefaultMessage()).append(","));
//        log.error(stringBuffer.toString());
//        return new RestErrorResponse(stringBuffer.toString());
//    }
}

