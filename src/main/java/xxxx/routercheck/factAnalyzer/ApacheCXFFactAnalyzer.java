package xxxx.routercheck.factAnalyzer;

import xxxx.routercheck.annotations.FactAnalyzerAnnotations;
import xxxx.routercheck.project.entry.Jar;
import soot.SootClass;
import soot.tagkit.VisibilityAnnotationTag;

import java.util.Map;

@FactAnalyzerAnnotations(
        name = "ApacheCXFFactAnalyzer"
)
public class ApacheCXFFactAnalyzer extends JAXRSFactAnalyzer{

    public ApacheCXFFactAnalyzer(){
        super(ApacheCXFFactAnalyzer.class.getName(), "class", "");
    }
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
}
