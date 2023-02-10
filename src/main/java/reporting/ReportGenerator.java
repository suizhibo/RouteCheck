package reporting;

import exceptions.ReportingException;
import project.entry.Project;
import entry.Settings;
import entry.Fact;

import java.util.Collection;

public interface ReportGenerator {
    void initialize(Project project, Collection<Fact> factChain, Settings settings);
    void write() throws ReportingException;
}
