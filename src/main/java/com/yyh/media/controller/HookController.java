package com.yyh.media.controller;

import com.yyh.media.entity.HookResult;
import com.yyh.media.event.OnHookEvent;
import com.yyh.media.subscribe.HookType;
import com.yyh.media.subscribe.ZlmHttpHookSubscribe;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;


/**
 * @author: yyh
 * @date: 2021-12-21 17:09
 * @description: HookController
 **/
@Slf4j
@RestController
@RequestMapping("index/hook")
@Api(value = "hook", tags = "hook监听")
public class HookController {

    @Resource
    private ZlmHttpHookSubscribe subscribe;

    @Resource
    private ApplicationEventPublisher publisher;

    /**
     * 服务器定时上报时间，上报间隔可配置，默认10s上报一次
     *
     */
    @ApiOperation(value = "on_server_keepalive", notes = "服务器心跳",httpMethod = "POST")
    @PostMapping(value = "on_server_keepalive", produces = "application/json;charset=UTF-8")
    public HookResult<String> onServerKeepalive(@RequestBody Map<String,Object> map){
        log.info("[ ZLM HOOK ]:on_server_keepalive API调用，参数:-> {}",map);
        publisher.publishEvent(new OnHookEvent("object",HookType.on_server_keepalive,map));
        return HookResult.success();
    }

    @ApiOperation(value = "on_server_started", notes = "服务器启动",httpMethod = "POST")
    @PostMapping(value = "on_server_started", produces = "application/json;charset=UTF-8")
    public HookResult<String> onServerStarted(HttpServletRequest request,@RequestBody Map<String,Object> map){
        log.info("[ ZLM HOOK ]on_server_started API调用，参数:-> {}",map);
        String remoteAddr = request.getRemoteAddr();
        map.put("ip",remoteAddr);
        publisher.publishEvent(new OnHookEvent("object",HookType.on_server_started,map));
        return HookResult.success();
    }

    /**
     * rtsp/rtmp流注册或注销时触发此事件；此事件对回复不敏感
     * @param map map
     * @return HookResult<String>
     */
    @ApiOperation(value = "on_stream_changed", notes = "媒体流变化",httpMethod = "POST")
    @PostMapping(value = "on_stream_changed", produces = "application/json;charset=UTF-8")
    public HookResult<String> onStreamChanged(@RequestBody Map<String,Object> map){
        log.info("[ ZLM HOOK ]on_stream_changed API调用，参数:-> {}",map);
        publisher.publishEvent(new OnHookEvent("object",HookType.on_stream_changed,map));
        return HookResult.success();
    }

    /**
     * shell登录鉴权，ZLMediaKit提供简单的telnet调试方式
     * @param map map
     * @return HookResult<String>
     */
    @ApiOperation(value = "on_shell_login", notes = "shell",httpMethod = "POST")
    @PostMapping(value = "on_shell_login", produces = "application/json;charset=UTF-8")
    public HookResult<String> onShellLogin(@RequestBody Map<String,Object> map){
        log.info("[ ZLM HOOK ]on_shell_login API调用，参数:-> {}",map);
        publisher.publishEvent(new OnHookEvent("object",HookType.on_shell_login,map));
        return HookResult.success();
    }

    /**
     * 流无人观看时事件，用户可以通过此事件选择是否关闭无人看的流
     * @param map map
     * @return HookResult<String>
     */
    @ApiOperation(value = "on_stream_none_reader", notes = "无人拉流",httpMethod = "POST")
    @PostMapping(value = "on_stream_none_reader", produces = "application/json;charset=UTF-8")
    public HookResult<String> onStreamNoneReader(@RequestBody Map<String,Object> map){
        log.info("[ ZLM HOOK ]on_stream_none_reader API调用，参数:-> {}",map);
        publisher.publishEvent(new OnHookEvent("object",HookType.on_stream_none_reader,map));
        return HookResult.success();
    }

}
