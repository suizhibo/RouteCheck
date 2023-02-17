package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import entry.Fact;
import exceptions.FactAnalyzerException;
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
        name = "WebXmlFactAnalyzer"
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
                        fact.setMethod("do*");
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
