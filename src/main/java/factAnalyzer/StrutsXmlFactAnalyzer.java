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
    private final String NAME = "StrutsXmlFactAnalyzer";

    private final String TYPE = "config";
    private final String DESCRIPTION = "";

    static Map<String, Element> actionMap = new HashMap<>();
    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        try {
            Config config = (Config) object;
            String filePath = config.getFilePath();
            // TODO: 解析struts2.xml
            SAXBuilder saxBuilder = new SAXBuilder();
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
                            fact.setClassNameMD5(Utils.getMD5Str(clazz));
                            fact.setRoute(route);
                            fact.setDescription(String.format("从%s中发现%s", config.getFilePath(),
                                    action.toString()));
                            fact.setCredibility(3);
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
        setEnable(false);
        Config config = (Config) object;
        String suffix = config.getSuffix();
        if (suffix != null && suffix.equals("xml")) {
            String filePath = config.getFilePath();
            // TODO: 解析struts2.xml
            try{
                // TODO: 判断是否包含<struts>标签
                SAXBuilder saxBuilder = new SAXBuilder();
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
