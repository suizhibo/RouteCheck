package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import entry.Fact;
import exceptions.FactAnalyzerException;
import project.entry.Jar;
import soot.SootClass;
import soot.tagkit.VisibilityAnnotationTag;

import java.util.Collection;
import java.util.Map;

@FactAnalyzerAnnotations(
        name = "RestletFactAnalyzer"
)
public class RestletFactAnalyzer extends JAXRSFactAnalyzer{
    private final String NAME = "RestletFactAnalyzer";

    private final String TYPE = "class";
    private final String DESCRIPTION = "";

    @Override
    public void prepare(Object object) {
        Map<String, Jar> jarMap = this.getProject().getJarMap();
        SootClass sootClass = (SootClass) object;
        VisibilityAnnotationTag visibilityAnnotationTag = (VisibilityAnnotationTag) sootClass.getTag("VisibilityAnnotationTag");
        if(jarMap.containsKey("org.restlet") && visibilityAnnotationTag != null){
            this.setEnable(true);
        }else{
            this.setEnable(false);
        }
    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        super.analysis(object, factChain);

    }

    @Override
    public String getName() {
        return this.NAME;
    }

    @Override
    public String getType() {
        return this.TYPE;
    }

    @Override
    public String getFactDescription() {
        return this.DESCRIPTION;
    }
}
