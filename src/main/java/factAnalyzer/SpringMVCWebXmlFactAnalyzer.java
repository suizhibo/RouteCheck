package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import entry.BaseWebXml;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

//https://www.cnblogs.com/lyh233/p/12047942.html
@FactAnalyzerAnnotations(
        filterName = "SpringMVCWebXmlFactAnalyzer"
)
public class SpringMVCWebXmlFactAnalyzer extends AbstractFactAnalyzer {
    private final String NAME = "SpringMVCWebXmlFactAnalyzer";

    private final String TYPE = "config";
    private final String DESCRIPTION = "";

    public void analysis(String dispatchXmlPath, Collection<Fact> factChain) {
        /*
         * bean标签 https://blog.csdn.net/ZixiangLi/article/details/87937819
         * */
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            InputStream is = new FileInputStream(new File(dispatchXmlPath));
            Document document = saxBuilder.build(is);
            Element rootElement = document.getRootElement();
            List<Element> children = rootElement.getChildren();
            children.forEach(child -> {
                try {
                    if (child.getName().equals("bean")) {
                        String clazz = child.getAttributeValue("class");
                        String oldClazz = clazz.substring(clazz.lastIndexOf(".") + 1);
                        String name = child.getAttributeValue("name");
                        if (name != null && oldClazz.endsWith("Controller")) {
                            Fact fact = new Fact();
                            fact.setDescription(child.toString());
                            if (name.startsWith("/")) {
                                fact.setCredibility(3);
                            } else {
                                fact.setCredibility(1);
                            }
                            fact.setRoute(name);
                            fact.setClassName(oldClazz);
                            fact.setClassPath(clazz);
                            fact.setDescription(child.toString());
                            factChain.add(fact);
                        }
                    }
                } catch (Exception e) {

                }
            });
        } catch (Exception e) {
        }
    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        try {
            String classPath = Utils.command.getClassPath();
            String projectPath = Utils.command.getProjectPath();
            Config config = (Config) object;
            String filePath = config.getFilePath();
            // TODO: 解析web.xml获取dispatcher-servlet.xml路径
            SAXBuilder saxBuilder = new SAXBuilder();
            InputStream is = new FileInputStream(new File(filePath));
            Document document = saxBuilder.build(is);
            Element rootElement = document.getRootElement();
            List<Element> children = rootElement.getChildren();
            children.forEach(child -> {
                try {
                    if (child.getName().equals("servlet") &&
                            child.getChildText("servlet-class", child.getNamespace()).
                                    equals("org.springframework.web.servlet.DispatcherServlet")) {
                        String servletName = child.getChildText("servlet-name", child.getNamespace());
                        String dispatcherServletFilePath = String.format("%s%sWEB-INF%sclasses%sdispatcher-%s.xml", projectPath, File.separator,
                                File.separator, File.separator, servletName);
                        Element initParam = child.getChild("init-param", child.getNamespace());
                        if (initParam != null) {
                            String paramValue = initParam.getChildText("param-value", initParam.getNamespace());
                            if (paramValue != null) {
                                if (paramValue.contains("classpath\\*:") || paramValue.contains("classpath:")) {
                                    paramValue = paramValue.replace("classpath:", "").replace("classpath\\*", "");
                                    dispatcherServletFilePath = classPath + File.separator + paramValue;
                                } else {
                                    paramValue = projectPath + File.separator + paramValue;
                                    dispatcherServletFilePath = paramValue;
                                }
                                // TODO param-value包含多个值
                            }
                        }
                        analysis(dispatcherServletFilePath, factChain);
                    }
                } catch (Exception e) {

                }
            });
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
    public void prepare(Object object) {

    }

    public static void main(String[] args) throws Exception {
        SpringMVCWebXmlFactAnalyzer springMVCWebXmlFactAnalyzer = new SpringMVCWebXmlFactAnalyzer();
        springMVCWebXmlFactAnalyzer.analysis((Object) null, null);
        springMVCWebXmlFactAnalyzer.analysis("D:\\工作\\专项工具\\RouteCheck\\config\\springmvc.xml", new ArrayList<>());
    }
}
