package utils;

import entry.BaseWebXml;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.*;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Utils {

    public static Command command = null;

    public static boolean mkDir(String path){
        File file = null;
        try {
            file = new File(path);
            if (!file.exists()) {
                return file.mkdirs();
            }
            else{
                return false;
            }
        } catch (Exception e) {
        } finally {
            file = null;
        }
        return false;
    }

    public static void fileWriter(String filepath, String content) throws IOException {
        try (FileWriter fileWriter = new FileWriter(filepath)) {
            fileWriter.append(content);
        }
    }

    public static String getMD5Str(String str) {
        byte[] digest = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("md5");
            digest  = md5.digest(str.getBytes("utf-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //16是表示转换为16进制数
        String md5Str = new BigInteger(1, digest).toString(16);
        return md5Str;
    }

    public static Map xmlToMap(String xmlStr, boolean needRootKey) throws DocumentException {
        Document doc = DocumentHelper.parseText(xmlStr);
        Element root = doc.getRootElement();
        Map<String, Object> map = (Map<String, Object>) xmlToMap(root);
        if(root.elements().size()==0 && root.attributes().size()==0){
            return map;
        }
        if(needRootKey){
            //在返回的map里加根节点键（如果需要）
            Map<String, Object> rootMap = new HashMap<String, Object>();
            rootMap.put(root.getName(), map);
            return rootMap;
        }
        return map;
    }

    /**
     * xml转map 带属性
     * @param xmlStr
     * @param needRootKey 是否需要在返回的map里加根节点键
     * @return
     * @throws DocumentException
     */
    public static Map xmlToMapWithAttr(String xmlStr, boolean needRootKey) throws DocumentException {
        Document doc = DocumentHelper.parseText(xmlStr);
        Element root = doc.getRootElement();
        Map<String, Object> map = (Map<String, Object>) xmlToMapWithAttr(root);
        if(root.elements().size()==0 && root.attributes().size()==0){
            return map; //根节点只有一个文本内容
        }
        if(needRootKey){
            //在返回的map里加根节点键（如果需要）
            Map<String, Object> rootMap = new HashMap<String, Object>();
            rootMap.put(root.getName(), map);
            return rootMap;
        }
        return map;
    }

    /**
     * xml转map 不带属性
     * @param element
     * @return
     */
    private static Object xmlToMap(Element element) {
        // System.out.println(element.getName());
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        List<Element> elements = element.elements();
        if (elements.size() == 0) {
            map.put(element.getName(), element.getText());
            if (!element.isRootElement()) {
                return element.getText();
            }
        } else if (elements.size() == 1) {
            map.put(elements.get(0).getName(), xmlToMap(elements.get(0)));
        } else if (elements.size() > 1) {
            // 多个子节点的话就得考虑list的情况了，比如多个子节点有节点名称相同的
            // 构造一个map用来去重
            Map<String, Element> tempMap = new LinkedHashMap<String, Element>();
            for (Element ele : elements) {
                tempMap.put(ele.getName(), ele);
            }
            Set<String> keySet = tempMap.keySet();
            for (String string : keySet) {
                Namespace namespace = tempMap.get(string).getNamespace();
                List<Element> elements2 = element.elements(new QName(string,
                        namespace));
                // 如果同名的数目大于1则表示要构建list
                if (elements2.size() > 1) {
                    List<Object> list = new ArrayList<Object>();
                    for (Element ele : elements2) {
                        if(StringUtils.isEmpty(ele.getText())) continue;
                        list.add(xmlToMap(ele));
                    }
                    map.put(string, list);
                } else {
                    // 同名的数量不大于1则直接递归去
                    map.put(string, xmlToMap(elements2.get(0)));
                }
            }
        }

        return map;
    }

    /**
     * xml转map 带属性
     * @param element
     * @return
     */
    public static Object xmlToMapWithAttr(Element element) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        List<Element> elements = element.elements();

        if (elements.size() == 0) {//没有子节点的情况 key - value
            map.put(element.getName(), element.getText());
            if (!element.isRootElement()) {
                return element.getText();
            }
        } else if (elements.size() == 1) {//一个子节点的情况，map
            map.put(elements.get(0).getName(),
                    xmlToMapWithAttr(elements.get(0)));
        } else if (elements.size() > 1) {//多个子节点的情况，key-value、map、list 情况据需要考虑
            // 多个子节点的话就得考虑list的情况了，比如多个子节点有节点名称相同的
            // 构造一个map用来去重
            Map<String, Element> tempMap = new LinkedHashMap<String, Element>();
            for (Element ele : elements) {
                tempMap.put(ele.getName(), ele);
            }
            Set<String> keySet = tempMap.keySet();
            for (String string : keySet) {
                Namespace namespace = tempMap.get(string).getNamespace();
                List<Element> elements2 = element.elements(new QName(string,
                        namespace));
                // 如果同名的数目大于1则表示要构建list
                if (elements2.size() > 1) {
                    List<Object> list = new ArrayList<Object>();
                    for (Element ele : elements2) {
                        if(StringUtils.isEmpty(ele.getText())) continue;
                        list.add(xmlToMapWithAttr(ele));
                    }
                    map.put(string, list);
                } else if (elements2.size() == 1 && "List".equals(elements2.get(0).attributeValue("nodeType"))) {//如果同名的数据只有一个并且属性nodeType为List，则要构建list
                    List<Object> list = new ArrayList<Object>();
                    Element ele = elements2.get(0);
                    if(StringUtils.isEmpty(ele.getText())) continue;
                    list.add(xmlToMapWithAttr(ele));
                    map.put(string, list);
                }else {
                    // 同名的数量不大于1则直接递归去
                    map.put(string, xmlToMapWithAttr(elements2.get(0)));
                }
            }
        }
        return map;
    }

    public static String fileReader(String filePath){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            String ls = System.getProperty("line.separator");
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            reader.close();

            String content = stringBuilder.toString();
            return content;
        }catch (Exception e){

        }
       return "";
    }

    public static Map<?, ?> objectToMap(Object obj) {
        if (obj == null)
            return null;
        return new org.apache.commons.beanutils.BeanMap(obj);
    }

    public static BaseWebXml parseWebXml(String webXmlPath) throws Exception {
        BaseWebXml baseWebXml = new BaseWebXml();
        try {
            String textFromFile = FileUtils.readFileToString(new File(webXmlPath), "UTF-8");
            Document doc = DocumentHelper.parseText(textFromFile);
            List<Attribute> attributes = doc.getRootElement().attributes();
            Map<String, String> stringStringMap = new HashMap<>();
            for (Attribute attr :
                    attributes) {
                stringStringMap.put(attr.getName(), attr.getValue());
            }
            baseWebXml.setAttributes(stringStringMap);
            Map<String, Object> map = (Map<String, Object>) Utils.xmlToMapWithAttr(doc.getRootElement());
            baseWebXml.setWebApp(map);
        } catch (Exception e) {
            throw e;
        }
        return baseWebXml;
    }
}
