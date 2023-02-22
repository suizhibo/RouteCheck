package factAnalyzer;

import entry.Fact;
import exceptions.FactAnalyzerException;

import java.util.Collection;

public class SpringFactAnalyzer extends AbstractFactAnalyzer{

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {

    }

    @Override
    public String getName() {
        return "SpringFactAnalyzer";
    }

    @Override
    public String getType() {
        return "";
    }

    @Override
    public String getFactDescription() {
        return "";
    }

    @Override
    public void prepare(Object object) {

    }
}
