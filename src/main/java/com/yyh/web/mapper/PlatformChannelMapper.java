package com.yyh.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yyh.web.entity.DeviceChannel;
import com.yyh.web.entity.PlatformChannel;
import com.yyh.web.vo.PlatformChannelVO;
import org.apache.ibatis.annotations.Select;

/**
 * @author: yyh
 * @date: 2022-04-24 18:16
 * @description: PlatformChannelMapper
 **/
public interface PlatformChannelMapper extends BaseMapper<PlatformChannel> {

    /**
     * 分页查询级联平台的所有所有通道
     *
     * @param page       分页参数
     * @param platformId  上级平台ID
     * @param catalogId  目录ID
     * @param query       查询内容
     * @param online      是否在线
     * @param hasSubChannel 通道类型
     * @return Page<PlatformChannelVO>
     */
    @Select(value = {" <script>" +
            "SELECT " +
            "    dc.id,\n" +
            "    dc.channel_id,\n" +
            "    dc.device_id,\n" +
            "    dc.name,\n" +
            "    de.manufacturer,\n" +
            "    de.host_address,\n" +
            "    dc.sub_count,\n" +
            "    pgc.platform_id as platformId,\n" +
            "    pgc.catalog_id as catalogId " +
            " FROM gb_device_channel dc " +
            " LEFT JOIN gb_device de ON dc.device_id = de.gb_id " +
            " LEFT JOIN gb_platform_channel pgc on pgc.device_channel_id = dc.id " +
            " WHERE 1=1 " +
            " <if test='query != null'> AND (dc.channel_id LIKE '%${query}%' OR dc.name LIKE '%${query}%' OR dc.name LIKE '%${query}%')</if> " +
            " <if test='online == true' > AND dc.status=1</if> " +
            " <if test='online == false' > AND dc.status=0</if> " +
            " <if test='hasSubChannel!= null and hasSubChannel == true' >  AND dc.sub_count > 0</if> " +
            " <if test='hasSubChannel!= null and hasSubChannel == false' >  AND dc.sub_count = 0</if> " +
            " <if test='catalogId == null ' >  AND dc.id not in (select device_channel_id from gb_platform_channel where platform_id=#{platformId} ) </if> " +
            " <if test='catalogId != null ' >  AND pgc.platform_id = #{platformId} and pgc.catalog_id=#{catalogId} </if> " +
            " ORDER BY dc.device_id, dc.channel_id ASC" +
            " </script>"})
    Page<PlatformChannelVO> queryChannelByPage(Page<PlatformChannelVO> page, String platformId, String catalogId, String query, Boolean online, Boolean hasSubChannel);


    /**
     * 根据平台id和通道id查询通道信息
     * @param platformId 平台id
     * @param channelId 通道id
     * @return DeviceChannel
     */
    @Select(value = {"<script>" +
            "SELECT dc.id, channel_id, device_id, parent_id, name, manufacture," +
            "model, owner, civil_code, block, address, parental, safety_way," +
            "register_way, cert_num, certifiable, err_code, end_time, secrecy, " +
            "ip_address, port, password, ptz_type, ptz_type_text, create_time," +
            "update_time, status, longitude, sub_count, stream_id, has_audio" +
            " FROM gb_device_channel dc" +
            " LEFT JOIN gb_platform_channel pc ON pc.device_channel_id = dc.id" +
            " WHERE 1 = 1" +
            " AND dc.channel_id = #{channelId}" +
            " AND pc.platform_id = #{platformId}" +
            "</script>"})
    DeviceChannel queryChannelById(String platformId, String channelId);
}
