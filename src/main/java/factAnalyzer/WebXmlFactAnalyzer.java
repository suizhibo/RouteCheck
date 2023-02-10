package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import entry.BaseWebXml;
import entry.Fact;
import exceptions.FactAnalyzerException;
import org.apache.commons.io.FileUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import project.entry.Config;
import utils.Utils;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public BaseWebXml parseWebXml(String webXmlPath) throws Exception {
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

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        try {
            Config config = (Config) object;
            String filePath = config.getFilePath();
            // TODO: 解析web.xml
            BaseWebXml baseWebXml = parseWebXml(filePath);
            Map<String, Object> webApp = baseWebXml.getWebApp();
            List<Object> servlets = (List<Object>) webApp.get("servlet");
            List<Object> servletMappings = (List<Object>) webApp.get("servlet-mapping");
            if (servlets.size() > 0 && servletMappings.size() > 0) {
                for (Object ob :
                        servlets) {
                    Fact fact = new Fact();
                    Map<String, String> servlet = (Map<String, String>) ob;
                    String servletClass = servlet.get("servlet-class");
                    String servletName = servlet.get("servlet-name");
                    fact.setClassNameMD5(Utils.getMD5Str(servletClass));
                    fact.setClassName(servletClass);

                    for (Object o :
                            servletMappings) {
                        Map<String, String> servletMapping = (Map<String, String>) o;
                        if (servletMapping.get("servlet-name").equals(servletName)) {
                            fact.setRoute(servletMapping.get("url-pattern"));
                        }
                    }
                    fact.setDescription(String.format("从文件%s中提取出servlet和servlet-mapping", config.getFilePath()));
                    fact.setCredibility(3);
                    factChain.add(fact);
                }
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
        WebXmlFactAnalyzer webXmlFactAnalyzer = new WebXmlFactAnalyzer();
        webXmlFactAnalyzer.parseWebXml("D:\\工作\\javaproject\\ServletTest1\\web\\WEB-INF\\web.xml");
    }
}
