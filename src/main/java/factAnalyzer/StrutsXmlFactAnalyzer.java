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
        name = "StrutsXmlFactAnalyzer"
)
public class StrutsXmlFactAnalyzer extends AbstractFactAnalyzer{

    static Map<String, Element> actionMap = new HashMap<>();

    public StrutsXmlFactAnalyzer() {
        super(StrutsXmlFactAnalyzer.class.getName(), "config", "");
    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        try {
            Config config = (Config) object;
            String filePath = config.getFilePath();
            // TODO: 解析struts2.xml
            SAXBuilder saxBuilder = new SAXBuilder();
            saxBuilder.setEntityResolver(new NoOpEntityResolver());
            InputStream is = new FileInputStream(new File(filePath));
            Document document = saxBuilder.build(is);
            Element rootElement = document.getRootElement();
            List<Element> packages = rootElement.getChildren();
            packages.forEach(pg ->{
                if(pg.getName().equals("package")){
                    List<Element> actions = pg.getChildren();
                    String nameSpace = pg.getAttributeValue("namespace");
                    actions.forEach(action -> {
                        try {
                            String actionName = action.getAttributeValue("name");
                            actionMap.put(actionName, action);
                            String route = nameSpace + actionName + ".action";
                            String clazz = action.getAttributeValue("class");
                            if (clazz == null) {
                                clazz = "ActionSupport";
                            }
                            String method = action.getAttributeValue("method");
                            Fact fact = new Fact();
                            fact.setMethod(method);
                            fact.setClassName(clazz);
                            fact.setRoute(route);
                            fact.setDescription(String.format("从%s中发现action标签，其name属性值为%s，其class属性值为%s", config.getFilePath(),
                                    actionName, clazz));
                            fact.setCredibility(3);
                            fact.setFactName(getName());
                            factChain.add(fact);
                        }catch (Exception e){

                        }

                    });
                }
            });
        } catch (Exception e) {
            throw new FactAnalyzerException(e.getMessage());
        }

    }

    @Override
    public void prepare(Object object) {
        setEnable(false);
        Config config = (Config) object;
        String suffix = config.getSuffix();
        if (suffix != null && suffix.equals("xml")) {
            String filePath = config.getFilePath();
            // TODO: 解析struts2.xml
            try{
                // TODO: 判断是否包含<struts>标签
                SAXBuilder saxBuilder = new SAXBuilder();
                saxBuilder.setEntityResolver(new NoOpEntityResolver());
                InputStream is = new FileInputStream(new File(filePath));
                Document document = saxBuilder.build(is);
                Element rootElement = document.getRootElement();
                if(rootElement.getName().equals("struts")){
                    this.setEnable(true);
                }
            }catch (Exception ex){

            }
        }

    }

    public static void main(String[] args) throws Exception {
        SAXBuilder saxBuilder = new SAXBuilder();
        InputStream is = new FileInputStream(new File("D:\\工作\\专项工具\\RouteCheck\\config\\struts2.xml"));
        Document document = saxBuilder.build(is);
        Element rootElement = document.getRootElement();
        List<Element> packages = rootElement.getChildren();
        packages.forEach(pg ->{
            if(pg.getName().equals("package")){
                List<Element> actions = pg.getChildren();
                String nameSpace = pg.getAttributeValue("namespace");
                actions.forEach(action -> {
                    String actionName = action.getAttributeValue("name");
                    String route = nameSpace + actionName + ".action";
                    String clazz = action.getAttributeValue("class");
                    if(clazz == null){
                        clazz = "ActionSupport";
                    }
                    String method = action.getAttributeValue("method");



                });
            }
        });
    }
}
