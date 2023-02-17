package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import entry.Fact;
import exceptions.FactAnalyzerException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import project.entry.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

@FactAnalyzerAnnotations(
        name = "SpringBeanFactAnalyzer"
)
public class SpringBeanFactAnalyzer extends SpringFactAnalyzer{
    private final String NAME = "SpringBeanFactAnalyzer";

    private final String TYPE = "config";
    private final String DESCRIPTION = "";

    public void analysis(String configPath, Collection<Fact> factChain) {
        /*
         * bean标签 https://blog.csdn.net/ZixiangLi/article/details/87937819
         * */
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            InputStream is = new FileInputStream(new File(configPath));
            Document document = saxBuilder.build(is);
            Element rootElement = document.getRootElement();
            List<Element> children = rootElement.getChildren();
            children.forEach(child -> {
                try {
                    if (child.getName().equals("bean")) {
                        Fact fact = new Fact();
                        String clazz = child.getAttributeValue("class");
                        // https://developer.aliyun.com/article/574556
                        if (clazz.equals("org.springframework.remoting.rmi.RmiServiceExporter") ||
                                clazz.equals("org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter")) {
                            List<Element> properties = child.getChildren();
                            properties.forEach(property ->{
                                if(property.getName().equals("property")){
                                    if(property.getAttributeValue("name").equals("serviceName")){
                                        String route = property.getChildText("value", property.getNamespace());
                                        fact.setCredibility(3);
                                        fact.setDescription(child.toString());
                                        fact.setRoute(route);
                                    }
                                    if(property.getAttributeValue("name").equals("service")){
                                        String route = property.getAttributeValue("ref");
                                        fact.setCredibility(3);
                                        fact.setDescription(child.toString());
                                        fact.setRoute(route);
                                    }
                                    if(property.getAttributeValue("name").equals("serviceInterface")){
                                        String clazzName = "";
                                        try{
                                            clazzName = property.getChildText("value", property.getNamespace());
                                        }catch (Exception ex){
                                            clazzName = property.getAttributeValue("value");
                                        }
                                        fact.setCredibility(3);
                                        fact.setDescription(child.toString());
                                        fact.setClassName(clazzName);
                                    }
                                }
                            });
                        }
                        String oldClazz = clazz.substring(clazz.lastIndexOf(".") + 1);
                        String name = child.getAttributeValue("name");
                        if (name != null && oldClazz.endsWith("Controller")) {
                            fact.setDescription(child.toString());
                            if (name.startsWith("/")) {
                                fact.setCredibility(3);
                            } else {
                                fact.setCredibility(1);
                            }
                            fact.setMethod("handleRequest");
                            fact.setRoute(name);
                            fact.setClassName(clazz);
                        }
                        factChain.add(fact);
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
           Config config = (Config) object;
           String configPath = config.getFilePath();
           analysis(configPath, factChain);
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
        // TODO:判断xml文件中是否包含：xmlns="http://www.springframework.org/schema/beans"
    }

    public static void main(String[] args) throws Exception {
        }
}
