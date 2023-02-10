package project.entry;

import soot.SootClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Project {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;
    private Set<SootClass> classes;

    private Collection<Config> configs;

    private String service;

    private String frameWork;

    public Collection<Jar> getJars() {
        return jars.size() >0 ? jars : new ArrayList<>();
    }

    public void addJar(Jar jar){
        this.jars.add(jar);
    }

    public void setJars(Collection<Jar> jars) {
        this.jars = jars;
    }

    private Collection<Jar> jars;

    public Project(){
        this.configs = new ArrayList<>();
        this.classes = new HashSet<>();
        this.service = "";
        this.frameWork = "";
        this.jars = new ArrayList<>();
    }

    public Project(String name, Set<SootClass> classes, Collection<Config> configs, String service, String frameWork) {
        this.name = name;
        this.classes = classes;
        this.configs = configs;
        this.service = service;
        this.frameWork = frameWork;
    }

    public Set<SootClass> getClasses() {
        return classes.size() > 0 ? classes : new HashSet<>();
    }

    public void setClasses(Set<SootClass> classes) {
        this.classes = classes;
    }

    public Collection<Config> getConfigs() {
        return configs.size()> 0 ? configs:  new ArrayList<>();
    }

    public void addConfig(Config config){
        this.configs.add(config);
    }

    public void setConfigs(Collection<Config> configs) {
        this.configs = configs;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getFrameWork() {
        return frameWork;
    }

    public void setFrameWork(String frameWork) {
        this.frameWork = frameWork;
    }
}
