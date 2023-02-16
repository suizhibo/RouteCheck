该项目的整体设计请查看：[路由分析引擎总体设计](https://github.com/suizhibo/RouteCheck/blob/master/%E8%B7%AF%E7%94%B1%E5%88%86%E6%9E%90%E5%BC%95%E6%93%8E%E6%80%BB%E4%BD%93%E8%AE%BE%E8%AE%A1.pptx
)
# 如何新增一个FactAnalyzer？

1. 实现一个类，名称形式如下XXFactAnalyzer，该类需继承AbstractFactAnalyzer，并添加注解FactAnalyzerAnnotations；

2. 为上述类添加私有属性NAME，TYPE（class, config, union）以及DESCRIPTION；
3. 可以添加static 属性保存该FactAnalyzer分析的相关结果；

4. 实现public void prepare(Object object) {}、public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {}、public String getName() {}、public String getType() {}、public String getFactDescription() {}以及public String toString() {}方法。

5. prepare方法做一些前置条件的筛选，并根据筛选结果判断是否启用该FactAnalyzer（所有新增FactAnalyzer默认启用）；

6. analysis方法完成具体的事实分析。

```java
package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import entry.BaseWebXml;
import entry.Fact;
import exceptions.FactAnalyzerException;
import org.apache.commons.io.FileUtils;
import org.dom4j.Attribute;
import org.dom4j.DocumentHelper;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import project.entry.Config;
import utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

@FactAnalyzerAnnotations(
        filterName = "WebXmlFactAnalyzer"
)

/*
https://docs.oracle.com/cd/E13222_01/wls/docs81/webapp/web_xml.html
*/
public class WebXmlFactAnalyzer extends AbstractFactAnalyzer {
    private final String NAME = "WebXmlFactAnalyzer";
    private final String TYPE = "config";
    private final String DESCRIPTION = "";

    static Map<String, Element> servlets = new HashMap<>();
    static Map<String, Set<Element>> servletMappings = new HashMap<>();

    @Override
    public void prepare(Object object) {
        Config config = (Config) object;
        String suffix = config.getSuffix();
        if (suffix != null && suffix.equals("xml")) {
            setEnable(true);
        } else {
            setEnable(false);
        }
        // TODO: 判断是否包含<web-app>标签
    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        try {
            Config config = (Config) object;
            String filePath = config.getFilePath();
            // TODO: 解析web.xml
            SAXBuilder saxBuilder = new SAXBuilder();
            InputStream is = new FileInputStream(new File(filePath));
            Document document = saxBuilder.build(is);
            Element rootElement = document.getRootElement();
            List<Element> children = rootElement.getChildren();
            children.forEach(child ->{
                if(child.getName().equals("servlet")){
                    String servletName = child.getChildText("servlet-name", child.getNamespace());
                    servlets.put(servletName, child);
                }else if(child.getName().equals("servlet-mapping")){
                    String servletName = child.getChildText("servlet-name", child.getNamespace());
                    Set<Element> values = servletMappings.getOrDefault(servletName, new HashSet<Element>());
                    values.add(child);
                    servletMappings.put(servletName, values);
                }
            });
            if (servlets.size() > 0 && servletMappings.size() > 0) {
                servlets.forEach((name, servlet) -> {
                    Set<Element> servletMapping = servletMappings.get(name);
                    servletMapping.forEach(sm ->{
                        Fact fact = new Fact();
                        String servletClass = servlet.getChildText("servlet-class", servlet.getNamespace());
                        fact.setClassNameMD5(Utils.getMD5Str(servletClass));
                        fact.setClassName(servletClass);
                        fact.setRoute(sm.getChildText("url-pattern", sm.getNamespace()));
                        fact.setDescription(String.format("从文件%s中提取出servlet和servlet-mapping", config.getFilePath()));
                        fact.setCredibility(3);
                        factChain.add(fact);
                    });
                });
            }
        } catch (Exception e) {
            throw new FactAnalyzerException(e.getMessage());
        }


    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getFactDescription() {
        return DESCRIPTION;
    }

    @Override
    public String toString() {
        return getName() + "\n" + getFactDescription();
    }

    public static void main(String[] args) throws Exception {
    }
}

```
