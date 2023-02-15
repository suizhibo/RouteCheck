package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import project.entry.Jar;
import soot.SootClass;
import soot.tagkit.VisibilityAnnotationTag;

import java.util.Map;

@FactAnalyzerAnnotations(
        filterName = "RESTEasyFactAnalyzer"
)
public class RESTEasyFactAnalyzer extends JAXRSFactAnalyzer{
    private final String NAME = "RESTEasyFactAnalyzer";

    private final String TYPE = "class";
    private final String DESCRIPTION = "";

    @Override
    public void prepare(Object object) {
        Map<String, Jar> jarMap = this.getProject().getJarMap();
        SootClass sootClass = (SootClass) object;
        VisibilityAnnotationTag visibilityAnnotationTag = (VisibilityAnnotationTag) sootClass.getTag("VisibilityAnnotationTag");
//        if(jarMap.containsKey("resteasy-jaxrs") && visibilityAnnotationTag != null){
//            this.setEnable(true);
//        }else{
//            this.setEnable(false);
//        }
    }
}
