package com.yyh.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yyh.web.entity.ParentPlatform;

/**
 * @author: yyh
 * @date: 2021-12-09 18:31
 * @description: IPlatformService
 **/
public interface IPlatformService extends IService<ParentPlatform> {

    /**
     * 分页查询上级平台
     * @param page 分页参数
     * @param ip sip服务器ip
     * @param sipId sip服务器id
     * @return Page<ParentPlatform>
     */
    Page<ParentPlatform> queryParentPlatformByPage(Page<ParentPlatform> page, String ip, String sipId);

    /**
     * 根据上级sip服务id查询上级平台
     * @param serverGbId 上级平台sip id
     * @return ParentPlatform
     */
    ParentPlatform queryParentPlatformByServerGbId(String serverGbId);

    /**
     * 添加上级平台
     * @param parentPlatform 平台参数
     * @return boolean
     */
    boolean saveParentPlatform(ParentPlatform parentPlatform);

    /**
     * 根据上级平台的国标id查询上级平台信息
     * @param platformGbId platformGbId
     * @return ParentPlatform
     */
    ParentPlatform queryParentPlatByServerGbId(String platformGbId);

    /**
     * 更新上级平台状态
     * @param platformGbId 上级平台国标id
     * @param register 注册/注销
     */
    void updateParentPlatformStatus(String platformGbId, boolean register);
}
