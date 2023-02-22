package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import entry.Fact;
import exceptions.FactAnalyzerException;

import java.util.Collection;

public class SOAPFactAnalyzer extends AbstractFactAnalyzer {

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {

    }

    @Override
    public String getName() {
        return "SOAPFactAnalyzer";
    }

    @Override
    public String getType() {
        return "config";
    }

    @Override
    public String getFactDescription() {
        return "";
    }

    @Override
    public void prepare(Object object) {

    }
}
