package factAnalyzer;

import entry.Fact;
import exceptions.FactAnalyzerException;

import java.util.Collection;

public class SpringFactAnalyzer extends AbstractFactAnalyzer{
    private final String NAME = "SpringFactAnalyzer";

    private final String TYPE = "";
    private final String DESCRIPTION = "";
    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {

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

    @Override
    public void prepare(Object object) {

    }
}
