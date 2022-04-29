package com.yyh.web.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yyh.common.utils.CommonResult;
import com.yyh.web.entity.MediaServer;
import com.yyh.web.service.IMediaServerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author: yyh
 * @date: 2022-03-31 15:33
 * @description: MediaServerController
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/mediaServer")
@Api(value = "mediaServer", tags = "媒体服务模块")
public class MediaServerController {

    private final IMediaServerService mediaServerService;

    /**
     * 分页查询媒体服务器
     * @param page 分页参数
     * @param ip 媒体服务器ip
     * @return CommonResult<Page<MediaServer>>
     */
    @GetMapping("page")
    @ApiOperation(value = "分页获取媒体服务器", notes = "根据ip查看媒体服务器",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "分页参数", dataTypeClass = Page.class),
            @ApiImplicitParam(name = "ip", value = "服务器ip", dataTypeClass = String.class)
    })
    public CommonResult<Page<MediaServer>> queryMediaServerByPage(Page<MediaServer> page,
                                                                  @RequestParam(required = false) String ip){
        LambdaQueryWrapper<MediaServer> eq = Wrappers.<MediaServer>lambdaQuery().eq(StrUtil.isNotBlank(ip), MediaServer::getIp, ip);
        return CommonResult.success(mediaServerService.page(page,eq));
    }


    /**
     * 检测媒体服务器是否存在
     * @param ip 媒体服务器ip
     * @param port 媒体服务器port
     * @param secret 媒体服务器秘钥
     * @return CommonResult<MediaServer>
     */
    @GetMapping(value = "/check")
    @ApiOperation(value = "测试流媒体服务", notes = "检测媒体服务器是否可用",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name="ip", value = "流媒体服务IP", dataTypeClass = String.class),
            @ApiImplicitParam(name="port", value = "流媒体服务HTT端口", dataTypeClass = Integer.class),
            @ApiImplicitParam(name="secret", value = "流媒体服务secret", dataTypeClass = String.class)
    })
    public CommonResult<MediaServer> checkMediaServer(@RequestParam String ip,
                                                  @RequestParam Integer port,
                                                  @RequestParam String secret){
        return mediaServerService.checkMediaServer(ip,port,secret);
    }


    /**
     * 新增媒体服务器
     * @param mediaServer 媒体服务器参数
     * @return CommonResult<Boolean>
     */
    @PostMapping
    @ApiOperation(value = "新增媒体服务器", notes = "新增媒体服务器",httpMethod = "POST")
    @ApiImplicitParam(name = "mediaServer", value = "媒体服务器参数", dataTypeClass = MediaServer.class)
    public CommonResult<Boolean> saveMediaServer(@RequestBody MediaServer mediaServer){
        return CommonResult.success(mediaServerService.saveMediaServer(mediaServer));
    }

    /**
     * 根据id修改媒体服务器参数
     * @param mediaServer mediaServer
     * @param mediaServerId mediaServerId
     * @return CommonResult<Boolean>
     */
    @PutMapping("{mediaServerId}")
    @ApiOperation(value = "修改媒体服务器参数", notes = "根据id修改媒体服务器参数",httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mediaServerId", value = "媒体服务器ID(主键ID)", dataTypeClass = String.class),
            @ApiImplicitParam(name = "mediaServer", value = "媒体服务器参数", dataTypeClass = MediaServer.class)
    })
    public CommonResult<Boolean> updateMediaServer(@RequestBody MediaServer mediaServer, @PathVariable String mediaServerId){
        mediaServer.setId(mediaServerId);
        return CommonResult.success(mediaServerService.updateMediaServerById(mediaServer));
    }


    /**
     * 移除媒体服务器
     * @param id 主键key
     * @return CommonResult<Boolean>
     */
    @DeleteMapping("{id}")
    @ApiOperation(value = "删除媒体服务",notes = "根据id删除媒体服务(id为主键)",httpMethod = "DELETE")
    @ApiImplicitParam(name = "id", value = "媒体服务器主键id", dataTypeClass = String.class)
    public CommonResult<Boolean> removeMediaServerById(@PathVariable String id){
        return CommonResult.success(mediaServerService.removeById(id));
    }
}
