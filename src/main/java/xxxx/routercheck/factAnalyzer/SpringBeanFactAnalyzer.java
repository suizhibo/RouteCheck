package xxxx.routercheck.factAnalyzer;

import xxxx.routercheck.annotations.FactAnalyzerAnnotations;
import xxxx.routercheck.entry.Fact;
import xxxx.routercheck.exceptions.FactAnalyzerException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import xxxx.routercheck.project.entry.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@FactAnalyzerAnnotations(
        name = "SpringBeanFactAnalyzer"
)
public class SpringBeanFactAnalyzer extends SpringFactAnalyzer{
    private Map<String, Element> idToBean = new HashMap<>();
    private Element simpleUrlHandlerMappingBean;
    public SpringBeanFactAnalyzer(){
        super(SpringBeanFactAnalyzer.class.getName(), "config", "");
    }

    private void analysisRemoting(Element child, Collection<Fact> factChain){
        Fact fact = new Fact();
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
                    fact.setFactName(getName());
                    factChain.add(fact);
                }
            }
        });
    }
    private void analysisMultiActionController(Collection<Fact> factChain){
        // TODO
    }
    private void analysisAbstractController(List<Element> beans, String configPath, Collection<Fact> factChain) {
        beans.forEach(child -> {
            try{
            String clazz = child.getAttributeValue("class");
            String oldClazz = clazz.substring(clazz.lastIndexOf(".") + 1);
            String name = child.getAttributeValue("name");
            if (name != null && oldClazz.endsWith("Controller")) {
                Fact fact = new Fact();
                fact.setDescription(String.format("从文件%s中解析到bean标签，其name属性为%s，class属性为%s",
                        configPath, name, oldClazz));
                if (name.startsWith("/")) {
                    fact.setCredibility(3);
                } else {
                    fact.setCredibility(1);
                }
                fact.setMethod("handleRequest(#)");
                fact.setRoute(name);
                fact.setClassName(clazz);
                fact.setFactName(getName());
                factChain.add(fact);
            }}catch (Exception e){

            }
        });
    }
    private void analysisOtherController(Collection<Fact> factChain){}

    public void analysis(String configPath, Collection<Fact> factChain) {
        /*
         * bean标签 https://blog.csdn.net/ZixiangLi/article/details/87937819
         * */
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            saxBuilder.setEntityResolver(new NoOpEntityResolver());
            InputStream is = new FileInputStream(new File(configPath));
            Document document = saxBuilder.build(is);
            Element rootElement = document.getRootElement();
            List<Element> children = rootElement.getChildren();
            AtomicInteger flag = new AtomicInteger();
            List<Element> beans = new ArrayList<>();
            children.forEach(child -> {
                try {
                    if (child.getName().equals("bean")) {
                        beans.add(child);
                        String clazz = child.getAttributeValue("class");
                        // https://developer.aliyun.com/article/574556
                        if (clazz.equals("org.springframework.remoting.rmi.RmiServiceExporter") ||
                                clazz.equals("org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter")) {
                            analysisRemoting(child, factChain);
                        }
                        // https://blog.csdn.net/q3498233/article/details/6703101
                        else if (clazz.equals("org.springframework.web.servlet.handler.SimpleUrlHandlerMapping")){
                            simpleUrlHandlerMappingBean = child;
                            flag.set(1);
                        }
                    }
                } catch (Exception e) {

                }
            });
            switch (flag.get()){
                case 0:
                    analysisAbstractController(beans, configPath, factChain);
                    break;
                case 1:
                    analysisMultiActionController(factChain);
                    break;
                default:
                    analysisOtherController(factChain);
                    break;
            }
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
    public void prepare(Object object) {
        // TODO:判断xml文件中是否包含：beans
        setEnable(false);
        Config config = (Config) object;
        String suffix = config.getSuffix();
        if (suffix != null && suffix.equals("xml")) {
            String filePath = config.getFilePath();
            // TODO: 解析.xml
            try{
                // TODO: 判断是否包含<beans>标签
                SAXBuilder saxBuilder = new SAXBuilder();
                saxBuilder.setEntityResolver(new NoOpEntityResolver());
                InputStream is = new FileInputStream(new File(filePath));
                Document document = saxBuilder.build(is);
                Element rootElement = document.getRootElement();
                if(rootElement.getName().equals("beans")){
                    this.setEnable(true);
                }
            }catch (Exception ex){

            }
        }

    }

    public static void main(String[] args) throws Exception {
        SpringBeanFactAnalyzer springBeanFactAnalyzer = new SpringBeanFactAnalyzer();
        springBeanFactAnalyzer.analysis("D:\\工作\\专项工具\\RouteCheck\\config\\spring\\springmvc.xml", new ArrayList<>());
    }
}
