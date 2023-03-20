package xxxx.routercheck.factAnalyzer;

import xxxx.routercheck.annotations.FactAnalyzerAnnotations;
import xxxx.routercheck.entry.Fact;
import xxxx.routercheck.exceptions.FactAnalyzerException;

import java.util.*;

//https://www.cnblogs.com/lyh233/p/12047942.html
@FactAnalyzerAnnotations(
        name = "SpringMVCAnnotationFactAnalyzer"
)
public class SpringMVCAnnotationFactAnalyzer extends SpringAnnotationFactAnalyzer{

    private final String PATTERN = "Lorg/springframework/stereotype/Controller;";
    private final String PATTERNMAP = "Lorg/springframework/web/bind/annotation";

    public SpringMVCAnnotationFactAnalyzer(){
        super(SpringMVCAnnotationFactAnalyzer.class.getName(), "class", "");
    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        super.analysis(object, factChain);
    }

    @Override
    public void prepare(Object object) {

    }
}
