package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import exceptions.FactAnalyzerException;
import entry.Fact;

import java.util.*;

@FactAnalyzerAnnotations(
        name = "UnionFactAnalyzer"
)
public class UnionFactAnalyzer extends AbstractFactAnalyzer {

    private final String NAME = "UnionFactAnalyzer";

    private final String TYPE = "Union";
    private final String DESCRIPTION = "";

    @Override
    public void prepare(Object object) {

    }

    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        // TODO: sort factChain
        try {
            List<Fact> facts = new ArrayList<>();
            facts.addAll(factChain);
            Collections.sort(facts, new Comparator<Fact>() {
                @Override
                public int compare(Fact o1, Fact o2) {
                    try {
                        return o1.getClassNameMD5().compareTo(o2.getClassNameMD5());
                    } catch (Exception e) {
                        return 0;
                    }
                }
            });
            factChain.clear();
            factChain.addAll(facts);
        } catch (Exception e) {
            e.printStackTrace();
            throw new FactAnalyzerException(e.getMessage());
        }
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
}
