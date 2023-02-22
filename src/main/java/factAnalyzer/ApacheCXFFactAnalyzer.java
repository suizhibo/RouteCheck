package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import project.entry.Jar;
import soot.SootClass;
import soot.tagkit.VisibilityAnnotationTag;

import java.util.Map;

@FactAnalyzerAnnotations(
        name = "ApacheCXFFactAnalyzer"
)
public class ApacheCXFFactAnalyzer extends JAXRSFactAnalyzer{

    @Override
    public void prepare(Object object) {
        Map<String, Jar> jarMap = this.getProject().getJarMap();
        SootClass sootClass = (SootClass) object;
        VisibilityAnnotationTag visibilityAnnotationTag = (VisibilityAnnotationTag) sootClass.getTag("VisibilityAnnotationTag");
        if(jarMap.containsKey("cfx-rt") && visibilityAnnotationTag != null){
            this.setEnable(true);
        }else{
            this.setEnable(false);
        }
    }

    @Override
    public String getName() {
        return "ApacheCXFFactAnalyzer";
    }

    @Override
    public String getType() {
        return "class";
    }

    @Override
    public String getFactDescription() {
        return "";
    }
}
