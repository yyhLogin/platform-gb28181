package com.yyh.web.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyh.common.constant.CommonResultConstants;
import com.yyh.common.exception.PlatformException;
import com.yyh.gb28181.event.platform.OnPlatformEnum;
import com.yyh.gb28181.event.platform.OnPlatformEvent;
import com.yyh.web.entity.ParentPlatform;
import com.yyh.web.mapper.PlatformMapper;
import com.yyh.web.service.IPlatformService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: yyh
 * @date: 2021-12-09 18:32
 * @description: PlatformServiceImpl
 **/
@Slf4j
@Service
public class PlatformServiceImpl extends ServiceImpl<PlatformMapper, ParentPlatform> implements IPlatformService {


    private final ApplicationEventPublisher publisher;

    public PlatformServiceImpl(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }


    /**
     * 分页查询上级平台
     *
     * @param page  分页参数
     * @param ip    sip服务器ip
     * @param sipId sip服务器id
     * @return Page<ParentPlatform>
     */
    @Override
    public Page<ParentPlatform> queryParentPlatformByPage(Page<ParentPlatform> page, String ip, String sipId) {

        LambdaQueryWrapper<ParentPlatform> like = Wrappers.<ParentPlatform>lambdaQuery()
                .eq(StrUtil.isNotBlank(ip), ParentPlatform::getServerIp, ip)
                .like(StrUtil.isNotBlank(sipId), ParentPlatform::getServerGbId, sipId);
        return this.page(page,like);
    }

    /**
     * 根据上级sip服务id查询上级平台
     *
     * @param serverGbId 上级平台sip id
     * @return ParentPlatform
     */
    @Override
    public ParentPlatform queryParentPlatformByServerGbId(String serverGbId) {
        LambdaQueryWrapper<ParentPlatform> eq = Wrappers.<ParentPlatform>lambdaQuery().eq(ParentPlatform::getServerGbId, serverGbId);
        List<ParentPlatform> list = this.list(eq);
        if (list==null){
            return null;
        }
        return list.get(0);
    }

    /**
     * 添加上级平台
     *
     * @param parentPlatform 平台参数
     * @return boolean
     */
    @Override
    public boolean saveParentPlatform(ParentPlatform parentPlatform) {
        parentPlatform.setCharacterSet(parentPlatform.getCharacterSet().toUpperCase());
        List<ParentPlatform> list = this.list(Wrappers.<ParentPlatform>lambdaQuery()
                .eq(ParentPlatform::getServerGbId, parentPlatform.getServerGbId()));
        if (list!=null && list.size()>0){
            log.warn("当前上级平台已经存在|{}",parentPlatform.getServerGbId());
            throw new PlatformException(CommonResultConstants.FAIL,"平台已存在:"+parentPlatform.getServerGbId());
        }else {
            if (parentPlatform.getCatalogGroup()==0){
                parentPlatform.setCatalogGroup(1);
            }
            this.save(parentPlatform);
            OnPlatformEvent event = new OnPlatformEvent("register", OnPlatformEnum.REGISTER,parentPlatform);
            publisher.publishEvent(event);
        }
        return true;
    }

    /**
     * 根据上级平台的国标id查询上级平台信息
     *
     * @param platformGbId platformGbId
     * @return ParentPlatform
     */
    @Override
    public ParentPlatform queryParentPlatByServerGbId(String platformGbId) {
        List<ParentPlatform> list = this.list(Wrappers.<ParentPlatform>lambdaQuery()
                .eq(ParentPlatform::getServerGbId, platformGbId));
        if (list==null||list.size()==0){
            return null;
        }
        return list.get(0);
    }

    /**
     * 更新上级平台状态
     *
     * @param platformGbId 上级平台国标id
     * @param register     注册/注销
     */
    @Override
    public void updateParentPlatformStatus(String platformGbId, boolean register) {
        this.update(Wrappers.<ParentPlatform>lambdaUpdate()
                .set(ParentPlatform::getStatus,register).eq(ParentPlatform::getServerGbId,platformGbId));
    }
}
