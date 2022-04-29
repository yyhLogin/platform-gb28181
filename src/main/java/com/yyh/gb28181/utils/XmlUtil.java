package com.yyh.gb28181.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.sip.RequestEvent;
import javax.sip.message.Request;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.*;

/**
 * @author: yyh
 * @date: 2021-12-01 11:22
 * @description: XmlUtil
 **/
public class XmlUtil {
    /**
     * 日志服务
     */
    private static final Logger logger = LoggerFactory.getLogger(XmlUtil.class);

    /**
     * 解析XML为Document对象
     *
     * @param xml 被解析的XMl
     *
     * @return Document
     */
    public static Element parseXml(String xml) {
        Document document = null;
        //
        StringReader sr = new StringReader(xml);
        SAXReader saxReader = new SAXReader();
        try {
            document = saxReader.read(sr);
        } catch (DocumentException e) {
            logger.error("解析失败", e);
        }
        return null == document ? null : document.getRootElement();
    }

    /**
     * 获取element对象的text的值
     *
     * @param em  节点的对象
     * @param tag 节点的tag
     * @return 节点
     */
    public static String getText(Element em, String tag) {
        if (null == em) {
            return null;
        }
        Element e = em.element(tag);
        //
        return null == e ? null : e.getText();
    }

    /**
     * xml解析为map的方法
     * @param xml xml
     * @return
     */
    public static <K, V> Map<String, Object> xmlToMap(String xml) throws Exception {
        Document document = DocumentHelper.parseText(xml);
        Element elements = document.getRootElement();
        Map<String, Object> maps = new HashMap<>();
        elementMap(elements, maps);
        return maps;

    }

    /**
     * xml解析为map的方法
     *
     * @param element element
     * @param maps maps
     */
    @SuppressWarnings({"unused" })
    private static void elementMap(Element element, Map<String, Object> maps) {
        List<Attribute> list = element.attributes();
        for (Attribute attr : list) {
            maps.put(attr.getName(), attr.getValue());
        }
        if (!("").equals(element.getTextTrim())) {
            maps.put(element.getName(), element.getTextTrim());
        }
        List<Element> elements = element.elements();
        Element elp = null;
        for (Element el : elements) {
            elementMap(el, maps);
        }
    }

    /**
     * 递归解析xml节点，适用于 多节点数据
     *
     * @param node     node
     * @param nodeName nodeName
     * @return List<Map<String, Object>>
     */
    public static List<Map<String, Object>> listNodes(Element node, String nodeName) {
        if (null == node) {
            return null;
        }
        // 初始化返回
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        // 首先获取当前节点的所有属性节点
        List<Attribute> list = node.attributes();

        Map<String, Object> map = null;
        // 遍历属性节点
        for (Attribute attribute : list) {
            if (nodeName.equals(node.getName())) {
                if (null == map) {
                    map = new HashMap<String, Object>();
                    listMap.add(map);
                }
                // 取到的节点属性放到map中
                map.put(attribute.getName(), attribute.getValue());
            }

        }
        // 遍历当前节点下的所有节点 ，nodeName 要解析的节点名称
        // 使用递归
        Iterator<Element> iterator = node.elementIterator();
        while (iterator.hasNext()) {
            Element e = iterator.next();
            listMap.addAll(listNodes(e, nodeName));
        }
        return listMap;
    }

    /**
     * xml转json
     *
     * @param element
     * @param json
     */
    public static void node2Json(Element element, JSONObject json) {
        // 如果是属性
        for (Object o : element.attributes()) {
            Attribute attr = (Attribute) o;
            if (StringUtils.hasText(attr.getValue())) {
                json.put("@" + attr.getName(), attr.getValue());
            }
        }
        List<Element> chdEl = element.elements();
        // 如果没有子元素,只有一个值
        if (chdEl.isEmpty() && !StringUtils.isEmpty(element.getText())) {
            json.put(element.getName(), element.getText());
        }

        for (Element e : chdEl) {   // 有子元素
            if (!e.elements().isEmpty()) {  // 子元素也有子元素
                JSONObject chdjson = new JSONObject();
                node2Json(e, chdjson);
                Object o = json.get(e.getName());
                if (o != null) {
                    JSONArray jsona = null;
                    if (o instanceof JSONObject) {  // 如果此元素已存在,则转为jsonArray
                        JSONObject jsono = (JSONObject) o;
                        json.remove(e.getName());
                        jsona = new JSONArray();
                        jsona.add(jsono);
                        jsona.add(chdjson);
                    }
                    if (o instanceof JSONArray) {
                        jsona = (JSONArray) o;
                        jsona.add(chdjson);
                    }
                    json.put(e.getName(), jsona);
                } else {
                    if (!chdjson.isEmpty()) {
                        json.put(e.getName(), chdjson);
                    }
                }
            } else { // 子元素没有子元素
                for (Object o : element.attributes()) {
                    Attribute attr = (Attribute) o;
                    if (!StringUtils.isEmpty(attr.getValue())) {
                        json.put("@" + attr.getName(), attr.getValue());
                    }
                }
                if (!e.getText().isEmpty()) {
                    json.put(e.getName(), e.getText());
                }
            }
        }
    }
    public static  Element getRootElement(RequestEvent evt) throws DocumentException {
        return getRootElement(evt, "gb2312");
    }

    public static Element getRootElement(RequestEvent evt, String charset) throws DocumentException {
        Request request = evt.getRequest();
        return getRootElement(request.getRawContent(), charset);
    }

    public static Element getRootElement(byte[] content, String charset) throws DocumentException {
        if (charset == null) {
            charset = "gb2312";
        }
        SAXReader reader = new SAXReader();
        reader.setEncoding(charset);
        Document xml = reader.read(new ByteArrayInputStream(content));
        return xml.getRootElement();
    }
}
