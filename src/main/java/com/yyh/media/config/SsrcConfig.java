package com.yyh.media.config;

import com.yyh.media.constants.MediaConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * @author: yyh
 * @date: 2021-12-28 10:50
 * @description: SsrcConfig
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SsrcConfig {

    /**
     * zlm流媒体服务器Id
     */
    private String mediaServerId;

    /**
     * ssrc前缀
     */
    private String ssrcPrefix;
    /**
     * zlm流媒体服务器已用会话句柄
     */
    private List<String> isUsed;
    /**
     * zlm流媒体服务器可用会话句柄
     */
    private List<String> notUsed;

    public SsrcConfig(String mediaServerId, Set<String> usedSet, String sipDomain) {
        this.mediaServerId = mediaServerId;
        this.isUsed = new ArrayList<>();
        this.ssrcPrefix = sipDomain.substring(3, 8);
        this.notUsed = new ArrayList<>();
        for (int i = 1; i < MediaConstant.MAX_STREAM_COUNT+1; i++) {
            String ssrc;
            if (i < 10) {
                ssrc = "000" + i;
            } else if (i < 100) {
                ssrc = "00" + i;
            } else if (i < 1000) {
                ssrc = "0" + i;
            } else {
                ssrc = String.valueOf(i);
            }
            if (null == usedSet || !usedSet.contains(ssrc)) {
                this.notUsed.add(ssrc);
            } else {
                this.isUsed.add(ssrc);
            }
        }
    }


    /**
     * 获取视频预览的SSRC值,第一位固定为0
     * @return ssrc
     */
    public String getPlaySsrc() {
        return "0" + getSsrcPrefix() + getSN();
    }

    /**
     * 获取录像回放的SSRC值,第一位固定为1
     *
     */
    public String getPlayBackSsrc() {
        return "1" + getSsrcPrefix() + getSN();
    }

    /**
     * 释放ssrc，主要用完的ssrc一定要释放，否则会耗尽
     * @param ssrc 需要重置的ssrc
     */
    public void releaseSsrc(String ssrc) {
        if (ssrc == null) {
            return;
        }
        String sn = ssrc.substring(6);
        try {
            isUsed.remove(sn);
            notUsed.add(sn);
        }catch (NullPointerException e){
            System.out.printf("11111");
        }
    }

    /**
     * 获取后四位数SN,随机数
     *
     */
    private String getSN() {
        String sn = null;
        int index = 0;
        if (notUsed.size() == 0) {
            throw new RuntimeException("ssrc已经用完");
        } else if (notUsed.size() == 1) {
            sn = notUsed.get(0);
        } else {
            index = new Random().nextInt(notUsed.size() - 1);
            sn = notUsed.get(index);
        }
        notUsed.remove(index);
        isUsed.add(sn);
        return sn;
    }
}
