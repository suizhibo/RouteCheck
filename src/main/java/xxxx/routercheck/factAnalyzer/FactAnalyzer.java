package xxxx.routercheck.factAnalyzer;


import xxxx.routercheck.exceptions.FactAnalyzerException;
import xxxx.routercheck.entry.Settings;
import xxxx.routercheck.project.entry.Project;
import xxxx.routercheck.entry.Fact;

import java.util.Collection;

public interface FactAnalyzer {
    void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException;
    String getName();
    String getType();
    String getFactDescription();
    void initialize(Project project, Settings settings);
    void prepare(Object object);
    boolean isEnable();
}
