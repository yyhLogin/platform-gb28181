package com.yyh.common.handle;

import com.yyh.common.exception.PlatformException;
import com.yyh.common.utils.CommonResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: yyh
 * @date: 2022-01-11 15:02
 * @description: PlatformHandleException
 **/
@Slf4j
@AllArgsConstructor
@RestControllerAdvice()
public class PlatformHandleException {

    @ResponseBody
    @ExceptionHandler(value = PlatformException.class)
    public CommonResult<Object> handlePlatformException(PlatformException e) {
        return CommonResult.fail(e.getCode(),e.getData(),e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public CommonResult<String> handleValidException(MethodArgumentNotValidException e) {
        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
        log.warn("参数绑定异常,ex = {}", allErrors.get(0).getDefaultMessage());
        String data = allErrors.get(0).getObjectName()+" "+allErrors.get(0).getDefaultMessage();
        /*return CommonResult.fail(I18nUtil.getMessage(defaultMessage),"g-0005");*/
        return CommonResult.fail(data,"invalid parameter");
    }

    @ResponseBody
    @ExceptionHandler(value = BindException.class)
    public CommonResult<String> bindException(BindException e) {
        return CommonResult.fail(e.getMessage(),"bind exception");
    }

    /**
     * 避免 404 重定向到 /error 导致NPE ,ignore-url 需要配置对应端点
     * @return CommonResult
     */
    @DeleteMapping("/error")
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public CommonResult<String> noHandlerFoundException() {
        return CommonResult.fail(HttpStatus.NOT_FOUND.getReasonPhrase());
    }
}
