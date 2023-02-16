package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import entry.Fact;
import exceptions.FactAnalyzerException;
import org.jdom.Element;
import utils.Utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@FactAnalyzerAnnotations(
        filterName = "SOAPUnionFactAnalyzer"
)
public class SOAPUnionFactAnalyzer extends UnionFactAnalyzer {
    private final String NAME = "SOAPUnionFactAnalyzer";

    private final String TYPE = "Union";
    private final String DESCRIPTION = "";

    @Override
    public void prepare(Object object) {
        super.prepare(object);
    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        try {
            Set<Element> servletMappings = WebXmlFactAnalyzer.servletMappings.get("AxisServlet");

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
                                if (param.getName().equals("parameter") && param.getAttributeValue("name").equals("className")) {
                                    String className = param.getAttributeValue("value");
                                    fact.setClassName(className);
                                    fact.setClassNameMD5(Utils.getMD5Str(className));
                                    fact.setCredibility(2);
                                    fact.setDescription(String.format("从Web.xml中发现servlet-name:AxisServlet,url-pattern:%s，WSDL中发现service name:%s, className: %s",
                                            urlPattern, serviceName, fact.getClassName()));
                                    factChain.add(fact);
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
}
