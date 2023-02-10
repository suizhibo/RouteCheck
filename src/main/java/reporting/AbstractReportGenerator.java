package reporting;

import exceptions.ReportingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import project.entry.Project;
import entry.Settings;
import entry.Fact;

import java.util.Collection;

public class AbstractReportGenerator implements ReportGenerator{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractReportGenerator.class);

    public Settings settings;
    public Project project;
    public Collection<Fact> factChain;



    @Override
    public void initialize(Project project, Collection<Fact> factChain, Settings settings) {
        this.project = project;
        this.factChain = factChain;
        this.settings = settings;
    }

    @Override
    public void write() throws ReportingException {

    }
}
