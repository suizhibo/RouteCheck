package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import entry.Fact;
import exceptions.FactAnalyzerException;
import org.jdom.Element;
import utils.Utils;

import java.util.*;

@FactAnalyzerAnnotations(
        name = "SOAPUnionFactAnalyzer"
)
public class SOAPUnionFactAnalyzer extends UnionFactAnalyzer {

    @Override
    public void prepare(Object object) {
        super.prepare(object);
    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        try {
            Set<Element> servletMappings = WebXmlFactAnalyzer.servletMappings.getOrDefault("AxisServlet", new HashSet<>());

            if (servletMappings.size() < 1) {
                super.analysis(object, factChain);
                return;
            }
            Map<String, Element> services = WSDDFactAnalyzer.services;
            servletMappings.forEach(servletMapping -> {
                String urlPattern = servletMapping.getChildText("url-pattern", servletMapping.getNamespace());
                if(urlPattern.endsWith("*")){
                    services.forEach((key, service) -> {
                        try {
                            Fact fact = new Fact();
                            String serviceName = service.getAttributeValue("name");
                            String route = urlPattern.replace("*", serviceName);
                            fact.setRoute(route);
                            List<Element> params = service.getChildren();
                            params.forEach(param -> {
                                if (param.getName().equals("parameter")) {
                                    String paramName = param.getAttributeValue("name");
                                    if(paramName.equals("className")){
                                        String className = param.getAttributeValue("value");
                                        fact.setClassName(className);
                                        fact.setClassNameMD5(Utils.getMD5Str(className));
                                        fact.setCredibility(2);
                                        fact.setFactName(getName());
                                        factChain.add(fact);
                                        fact.setDescription(String.format("从Web.xml中发现servlet-name:AxisServlet,url-pattern:%s，WSDD中发现service name:%s, className: %s",
                                                urlPattern, serviceName, fact.getClassName()));
                                    }else if(paramName.equals("allowedMethods")){
                                        String allowedMethods = param.getAttributeValue("value");
                                        if(allowedMethods.equals("*")){
                                            allowedMethods = "* (all methods)";
                                        }
                                        fact.setMethod(allowedMethods);
                                    }
                                }
                            });
                        } catch (Exception ex) {
                        }

                    });
                }
            });
            super.analysis(object, factChain);
        } catch (Exception e) {
            e.printStackTrace();
            throw new FactAnalyzerException(e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "SOAPUnionFactAnalyzer";
    }

    @Override
    public String getType() {
        return "union";
    }

    @Override
    public String getFactDescription() {
        return "";
    }
}
