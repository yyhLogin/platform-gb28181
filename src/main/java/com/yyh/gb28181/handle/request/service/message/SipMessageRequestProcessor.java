package com.yyh.gb28181.handle.request.service.message;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyh.gb28181.constant.VideoManagerConstant;
import com.yyh.gb28181.entity.DeviceChannel;
import com.yyh.gb28181.handle.request.SipRequestProcessorParent;
import com.yyh.web.entity.GbDevice;
import com.yyh.web.service.IGbDeviceService;
import gov.nist.javax.sip.RequestEventExt;
import gov.nist.javax.sip.address.SipUri;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.address.Address;
import javax.sip.header.FromHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.yyh.gb28181.utils.XmlUtil.getText;

/**
 * @author: yyh
 * @date: 2021-12-09 16:30
 * @description: SipMsgRequestProcessorService
 **/
@Component
public class SipMessageRequestProcessor extends SipRequestProcessorParent{

    private final static Logger logger = LoggerFactory.getLogger(SipMessageRequestProcessor.class);


    private final IGbDeviceService gb28181Service;

    private final RedisTemplate<String,String> redisTemplate;

    private final ObjectMapper mapper;

    public SipMessageRequestProcessor(IGbDeviceService gb28181Service, RedisTemplate<String, String> redisTemplate, ObjectMapper mapper) {
        this.gb28181Service = gb28181Service;
        this.redisTemplate = redisTemplate;
        this.mapper = mapper;
    }



    /**
     * 通道信息
     * @param requestEvent requestEvent
     */
    public void messageResponseCatalog(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
///        logger.info("{}",request);
        FromHeader header = (FromHeader) request.getHeader(FromHeader.NAME);
        Address address = header.getAddress();
        SipUri uri = (SipUri) address.getURI();
        String deviceId = uri.getUser();
        GbDevice byId = gb28181Service.getById(deviceId);
        try {
            RequestEventExt evt = (RequestEventExt)requestEvent;
            if (byId==null){
                logger.info("设备不存在:{}",deviceId);
                responseAck(evt, Response.NOT_FOUND, "device id not found");
                return;
            }
            String charSet = StrUtil.isBlank(byId.getCharset())? VideoManagerConstant.DEVICE_DEFAULT_CHARSET:byId.getCharset();
            Element rootElement = getRootElement(evt, charSet);
            Element deviceListElement = rootElement.element("DeviceList");
            Iterator<Element> deviceListIterator = deviceListElement.elementIterator();
            List<DeviceChannel> channelList = new ArrayList<>();
            if (deviceListIterator != null) {
                // 遍历DeviceList
                while (deviceListIterator.hasNext()) {
                    Element itemDevice = deviceListIterator.next();
                    Element channelDeviceElement = itemDevice.element("DeviceID");
                    if (channelDeviceElement == null) {
                        continue;
                    }
                    String channelDeviceId = channelDeviceElement.getText();
                    Element channelNameElement = itemDevice.element("Name");
                    String channelName = channelNameElement != null ? channelNameElement.getTextTrim().toString() : "";
                    Element statusElement = itemDevice.element("Status");
                    String status = statusElement != null ? statusElement.getText().toString() : "ON";
                    DeviceChannel deviceChannel = new DeviceChannel();
                    deviceChannel.setName(channelName);
                    deviceChannel.setChannelId(channelDeviceId);
                    // ONLINE OFFLINE  HIKVISION DS-7716N-E4 NVR的兼容性处理
                    if (status.equals("ON") || status.equals("On") || status.equals("ONLINE")) {
                        deviceChannel.setStatus(1);
                    }
                    if (status.equals("OFF") || status.equals("Off") || status.equals("OFFLINE")) {
                        deviceChannel.setStatus(0);
                    }

                    deviceChannel.setManufacture(getText(itemDevice, "Manufacturer"));
                    deviceChannel.setModel(getText(itemDevice, "Model"));
                    deviceChannel.setOwner(getText(itemDevice, "Owner"));
                    deviceChannel.setCivilCode(getText(itemDevice, "CivilCode"));
                    deviceChannel.setBlock(getText(itemDevice, "Block"));
                    deviceChannel.setAddress(getText(itemDevice, "Address"));
                    if (getText(itemDevice, "Parental") == null || getText(itemDevice, "Parental") == "") {
                        deviceChannel.setParental(0);
                    } else {
                        deviceChannel.setParental(Integer.parseInt(getText(itemDevice, "Parental")));
                    }
                    deviceChannel.setParentId(getText(itemDevice, "ParentID"));
                    if (getText(itemDevice, "SafetyWay") == null || getText(itemDevice, "SafetyWay") == "") {
                        deviceChannel.setSafetyWay(0);
                    } else {
                        deviceChannel.setSafetyWay(Integer.parseInt(getText(itemDevice, "SafetyWay")));
                    }
                    if (getText(itemDevice, "RegisterWay") == null || getText(itemDevice, "RegisterWay") == "") {
                        deviceChannel.setRegisterWay(1);
                    } else {
                        deviceChannel.setRegisterWay(Integer.parseInt(getText(itemDevice, "RegisterWay")));
                    }
                    deviceChannel.setCertNum(getText(itemDevice, "CertNum"));
                    if (getText(itemDevice, "Certifiable") == null || getText(itemDevice, "Certifiable") == "") {
                        deviceChannel.setCertifiable(0);
                    } else {
                        deviceChannel.setCertifiable(Integer.parseInt(getText(itemDevice, "Certifiable")));
                    }
                    if (getText(itemDevice, "ErrCode") == null || getText(itemDevice, "ErrCode") == "") {
                        deviceChannel.setErrCode(0);
                    } else {
                        deviceChannel.setErrCode(Integer.parseInt(getText(itemDevice, "ErrCode")));
                    }
                    deviceChannel.setEndTime(getText(itemDevice, "EndTime"));
                    deviceChannel.setSecrecy(getText(itemDevice, "Secrecy"));
                    deviceChannel.setIpAddress(getText(itemDevice, "IPAddress"));
                    if (getText(itemDevice, "Port") == null || getText(itemDevice, "Port") == "") {
                        deviceChannel.setPort(0);
                    } else {
                        deviceChannel.setPort(Integer.parseInt(getText(itemDevice, "Port")));
                    }
                    deviceChannel.setPassword(getText(itemDevice, "Password"));
                    if (NumberUtil.isDouble(getText(itemDevice, "Longitude"))) {
                        deviceChannel.setLongitude(Double.parseDouble(getText(itemDevice, "Longitude")));
                    } else {
                        deviceChannel.setLongitude(0.00);
                    }
                    if (NumberUtil.isDouble(getText(itemDevice, "Latitude"))) {
                        deviceChannel.setLatitude(Double.parseDouble(getText(itemDevice, "Latitude")));
                    } else {
                        deviceChannel.setLatitude(0.00);
                    }
                    if (getText(itemDevice, "PTZType") == null || getText(itemDevice, "PTZType") == "") {
                        deviceChannel.setPtzType(0);
                    } else {
                        deviceChannel.setPtzType(Integer.parseInt(getText(itemDevice, "PTZType")));
                    }
                    /// 默认含有音频，播放时再检查是否有音频及是否AAC
                    deviceChannel.setHasAudio(true);
                    /// storager.updateChannel(device.getDeviceId(), deviceChannel);
                    channelList.add(deviceChannel);
                }
                if (channelList.size()!=0){
                    String cacheKey = VideoManagerConstant.DEVICE_CHANEL_28181 + deviceId;
                    Boolean hasKey = redisTemplate.hasKey(cacheKey);
                    if (BooleanUtil.isTrue(hasKey)){
                        redisTemplate.delete(cacheKey);
                    }
                    channelList.forEach(item->{
                        try {
                            redisTemplate.opsForHash().put(cacheKey,item.getChannelId(),mapper.writeValueAsString(item));
                        } catch (JsonProcessingException e) {
                            logger.error("messageCatalog update cache has error {} | {}",deviceId,e.getMessage(),e);
                        }
                    });
                }else {
                    logger.info("messageCatalog no catalog ");
                }
//                RequestMessage msg = new RequestMessage();
//                msg.setKey(key);
//                msg.setData(device);
//                deferredResultHolder.invokeAllResult(msg);
                // 回复200 OK
                responseAck(evt, Response.OK);
            }
        } catch (DocumentException | InvalidArgumentException | ParseException | SipException e) {
            logger.error("messageCatalog handle has error {} | {}",deviceId,e.getMessage(),e);
        }
    }

}
