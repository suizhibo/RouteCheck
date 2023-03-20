package xxxx.routercheck.reporting;

import xxxx.routercheck.exceptions.ReportingException;
import xxxx.routercheck.project.entry.Project;
import xxxx.routercheck.entry.Settings;
import xxxx.routercheck.entry.Fact;

import java.util.Collection;

public interface ReportGenerator {
    void initialize(Project project, Collection<Fact> factChain, Settings settings);
    void write() throws ReportingException;
}
