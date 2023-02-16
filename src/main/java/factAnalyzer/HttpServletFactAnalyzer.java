package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import exceptions.FactAnalyzerException;
import soot.SootClass;
import soot.tagkit.AnnotationArrayElem;
import soot.tagkit.AnnotationStringElem;
import soot.tagkit.AnnotationTag;
import soot.tagkit.VisibilityAnnotationTag;
import utils.Utils;
import entry.Fact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

@FactAnalyzerAnnotations(
        name = "HttpServletFactAnalyzer"
)
public class HttpServletFactAnalyzer extends AbstractFactAnalyzer{
    private final String NAME = "HttpServletFactAnalyzer";

    private final String TYPE = "class";
    private final String DESCRIPTION = "";

    @Override
    public void prepare(Object object) {
        // TODO： nothing
    }

    private boolean hasSuperClass(SootClass sootClass){
        SootClass sc = sootClass.getSuperclass();
        if(sc.getName().equals("javax.servlet.http.HttpServlet")){
            return true;
        }
        if(sc.hasSuperclass()){
            return hasSuperClass(sc);
        }
        return false;
    }


    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        try {
            Fact fact = new Fact();
            SootClass sootClass = (SootClass) object;
            // TODO: 1.判断是否使用注解; 2.判断是否继承HttpServlet
            AtomicBoolean hasWebServlet = new AtomicBoolean(false);
            VisibilityAnnotationTag visibilityAnnotationTag = (VisibilityAnnotationTag) sootClass.getTag("VisibilityAnnotationTag");
            if(visibilityAnnotationTag != null && visibilityAnnotationTag.hasAnnotations()){
                ArrayList<AnnotationTag> annotationTags =  visibilityAnnotationTag.getAnnotations();
                annotationTags.forEach(a -> {
                    if(a.getType().equals("Ljavax/servlet/annotation/WebServlet;")){
                        a.getElems().forEach(e ->{
                            if(e.getClass().toString().contains("AnnotationArrayElem")){
                                if(e.getName().equals("urlPatterns") || e.toString().contains("/")){
                                    fact.setClassNameMD5(Utils.getMD5Str(sootClass.getName()));
                                    AnnotationArrayElem annotationArrayElem = (AnnotationArrayElem) e;
                                    annotationArrayElem.getValues().forEach(v ->{
                                        AnnotationStringElem annotationStringElem = (AnnotationStringElem) v;
                                        String route = annotationStringElem.getValue();
                                        fact.setRoute(route);
                                    });
                                    fact.setClassName(sootClass.getName());
                                    fact.setClassPath(sootClass.getFilePath());
                                    fact.setDescription("类文件中使用注解：" + a.toString());
                                    fact.setCredibility(3);
                                    hasWebServlet.set(true);
                                }
                            }
                        });
                    }
                });
            }

            if(!hasWebServlet.get() && sootClass.hasSuperclass()){
                if(hasSuperClass(sootClass)){
                    fact.setClassNameMD5(Utils.getMD5Str(sootClass.getName()));
                    fact.setClassName(sootClass.getName());
                    fact.setClassPath(sootClass.getFilePath());
                    fact.setCredibility(1);
                    fact.setDescription("类文件继承（直接或间接）javax.servlet.http.HttpServlet");
                }
            }
            factChain.add(fact);
        }catch (Exception e){
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
    public String toString() {
        return getName() + "\n" + getFactDescription();
    }
}
