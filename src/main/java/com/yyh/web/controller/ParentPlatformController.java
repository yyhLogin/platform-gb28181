package com.yyh.web.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yyh.common.utils.CommonResult;
import com.yyh.web.dto.GbDeviceDTO;
import com.yyh.web.dto.PlatformChannelDTO;
import com.yyh.web.entity.ParentPlatform;
import com.yyh.web.service.IPlatformChannelService;
import com.yyh.web.service.IPlatformService;
import com.yyh.web.vo.PlatformChannelVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author: yyh
 * @date: 2021-12-09 18:28
 * @description: ParentPlatformController
 **/
@Slf4j
@RestController
@RequestMapping("api/platform")
@RequiredArgsConstructor
@Api(value = "platform", tags = "上级平台")
public class ParentPlatformController {


    private final IPlatformService platformService;

    private final IPlatformChannelService platformChannelService;


    @GetMapping("page")
    @ApiOperation(value = "分页获取上级平台", notes = "根据ip查询上级平台",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "分页参数", dataTypeClass = Page.class),
            @ApiImplicitParam(name = "ip", value = "sip服务器ip", dataTypeClass = String.class),
            @ApiImplicitParam(name = "sipId", value = "sip服务器id", dataTypeClass = String.class)
    })
    public CommonResult<Page<ParentPlatform>> queryParentPlatformByPage(Page<ParentPlatform> page,
                                                                        String ip,
                                                                        String sipId){
        return CommonResult.success(platformService.queryParentPlatformByPage(page,ip,sipId));
    }

    /**
     * 注册上级平台
     * @param parentPlatform parentPlatform
     * @return CommonResult<Boolean>
     */
    @PostMapping
    @ApiOperation(value = "注册上级平台", notes = "注册上级平台",httpMethod = "POST")
    @ApiImplicitParam(name = "parentPlatform", value = "上级平台参数", dataTypeClass = ParentPlatform.class)
    public CommonResult<Boolean> saveParentPlatform(@Validated @RequestBody ParentPlatform parentPlatform){
        return CommonResult.success(platformService.saveParentPlatform(parentPlatform));
    }


    /**
     * 分页查询级联平台的所有所有通道
     *
     * @param current        当前页
     * @param size       每页条数
     * @param platformId  上级平台ID
     * @param query       查询内容
     * @param online      是否在线
     * @param channelType 通道类型
     * @return
     */
    @ApiOperation("分页查询级联平台的所有所有通道")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "size", value = "每页条数", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "platformId", value = "上级平台ID", dataTypeClass = String.class),
            @ApiImplicitParam(name = "catalogId", value = "目录ID", dataTypeClass = String.class),
            @ApiImplicitParam(name = "query", value = "查询内容", dataTypeClass = String.class),
            @ApiImplicitParam(name = "online", value = "是否在线", dataTypeClass = Boolean.class),
            @ApiImplicitParam(name = "channelType", value = "通道类型", dataTypeClass = Boolean.class),
    })
    @GetMapping("channel/page")
    public CommonResult<Page<PlatformChannelVO>> queryChannelByPage(int current,int size,
                                                                    @RequestParam(required = false) String platformId,
                                                                    @RequestParam(required = false) String catalogId,
                                                                    @RequestParam(required = false) String query,
                                                                    @RequestParam(required = false) Boolean online,
                                                                    @RequestParam(required = false) Boolean channelType){
        if (!StringUtils.hasLength(platformId)) {
            platformId = null;
        }
        if (!StringUtils.hasLength(query)) {
            query = null;
        }
        if (!StringUtils.hasLength(platformId) || !StringUtils.hasLength(catalogId)) {
            catalogId = null;
        }
        return CommonResult.success(platformChannelService.queryChannelByPage(current,size,platformId,catalogId,query,online,channelType));
    }

    /**
     * 将通道推送给上级平台
     * @param channel 通道信息
     * @return CommonResult<Boolean>
     */
    @PostMapping("channel/push")
    public CommonResult<Boolean> pushChannel2Platform(@Validated @RequestBody PlatformChannelDTO channel){
        return CommonResult.success(platformChannelService.pushChannel2Platform(channel));
    }

    /**
     * 将通道从上级平台下线
     * @param channel 通道信息
     * @return CommonResult<Boolean>
     */
    @PutMapping("channel/down")
    public CommonResult<Boolean> downChannel2Platform(@Validated @RequestBody PlatformChannelDTO channel){
        return CommonResult.success(platformChannelService.downChannel2Platform(channel));
    }
}
