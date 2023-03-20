package xxxx.routercheck.factAnalyzer;

import xxxx.routercheck.annotations.FactAnalyzerAnnotations;
import xxxx.routercheck.project.entry.Jar;
import soot.SootClass;
import soot.tagkit.VisibilityAnnotationTag;

import java.util.Map;

@FactAnalyzerAnnotations(
        name = "ApacheWinkFactAnalyzer"
)
public class ApacheWinkFactAnalyzer extends JAXRSFactAnalyzer{

    public ApacheWinkFactAnalyzer(){
        super(ApacheWinkFactAnalyzer.class.getName(), "class", "");
    }
    @Override
    public void prepare(Object object) {
        Map<String, Jar> jarMap = this.getProject().getJarMap();
        SootClass sootClass = (SootClass) object;
        VisibilityAnnotationTag visibilityAnnotationTag = (VisibilityAnnotationTag) sootClass.getTag("VisibilityAnnotationTag");
        if(jarMap.containsKey("wink") && visibilityAnnotationTag != null){
            this.setEnable(true);
        }else{
            this.setEnable(false);
        }
    }
}
