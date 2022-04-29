package com.yyh.media.callback;

/**
 * @author: yyh
 * @date: 2022-04-11 15:07
 * @description: CmdHandle
 **/
public interface CmdHandle {

    /**
     * 处理单个handle
     * @param key key
     * @param sn sn
     * @param object object
     */
    void handleResult(String key,String sn,Object object);

    /**
     * 处理所有的handle
     * @param key key
     * @param object object
     */
    void handleAllResult(String key,Object object);

    /**
     * 当前handle是否存在
     * @param key key
     * @param sn sn
     * @return boolean
     */
    boolean isExist(String key,String sn);
}
