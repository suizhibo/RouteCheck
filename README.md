该项目的整体设计请查看：[路由分析引擎总体设计](https://github.com/suizhibo/RouteCheck/blob/master/%E8%B7%AF%E7%94%B1%E5%88%86%E6%9E%90%E5%BC%95%E6%93%8E%E6%80%BB%E4%BD%93%E8%AE%BE%E8%AE%A1.pptx
)
# 如何新增一个FactAnalyzer？

1. 实现一个类，名称形式如下XXFactAnalyzer，该类需继承AbstractFactAnalyzer，并添加注解FactAnalyzerAnnotations；

2. 为上述类添加私有属性NAME，TYPE（class, config, union）以及DESCRIPTION；
3. 可以添加static 属性保存该FactAnalyzer分析的相关结果；

4. 实现public void prepare(Object object) {}、public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {}、public String getName() {}、public String getType() {}、public String getFactDescription() {}以及public String toString() {}方法。

5. prepare方法做一些前置条件的筛选，并根据筛选结果判断是否启用该FactAnalyzer（所有新增FactAnalyzer默认启用）；

6. analysis方法完成具体的事实分析。

```java
package factAnalyzer;

import annotations.FactAnalyzerAnnotations;
import exceptions.FactAnalyzerException;
import soot.SootClass;
import soot.tagkit.AnnotationArrayElem;
import soot.tagkit.AnnotationStringElem;
import soot.tagkit.AnnotationTag;
import soot.tagkit.VisibilityAnnotationTag;
import utils.Utils;
import entry.Fact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

@FactAnalyzerAnnotations(
        filterName = "HttpServletFactAnalyzer"
)
public class HttpServletFactAnalyzer extends AbstractFactAnalyzer{
    private final String NAME = "HttpServletFactAnalyzer";

    private final String TYPE = "class";
    private final String DESCRIPTION = "";

    @Override
    public void prepare(Object object) {
        // TODO： nothing
    }

    private boolean hasSuperClass(SootClass sootClass){
        SootClass sc = sootClass.getSuperclass();
        if(sc.getName().equals("javax.servlet.http.HttpServlet")){
            return true;
        }
        if(sc.hasSuperclass()){
            return hasSuperClass(sc);
        }
        return false;
    }


    @Override
    public void analysis(Object object, Collection<Fact> factChain) throws FactAnalyzerException {
        try {
            Fact fact = new Fact();
            SootClass sootClass = (SootClass) object;
            // TODO: 1.判断是否使用注解; 2.判断是否继承HttpServlet
            AtomicBoolean hasWebServlet = new AtomicBoolean(false);
            VisibilityAnnotationTag visibilityAnnotationTag = (VisibilityAnnotationTag) sootClass.getTag("VisibilityAnnotationTag");
            if(visibilityAnnotationTag != null && visibilityAnnotationTag.hasAnnotations()){
                ArrayList<AnnotationTag> annotationTags =  visibilityAnnotationTag.getAnnotations();
                annotationTags.forEach(a -> {
                    if(a.getType().equals("Ljavax/servlet/annotation/WebServlet;")){
                        a.getElems().forEach(e ->{
                            if(e.getClass().toString().contains("AnnotationArrayElem")){
                                if(e.getName().equals("urlPatterns") || e.toString().contains("/")){
                                    fact.setClassNameMD5(Utils.getMD5Str(sootClass.getName()));
                                    AnnotationArrayElem annotationArrayElem = (AnnotationArrayElem) e;
                                    annotationArrayElem.getValues().forEach(v ->{
                                        AnnotationStringElem annotationStringElem = (AnnotationStringElem) v;
                                        String route = annotationStringElem.getValue();
                                        fact.setRoute(route);
                                    });
                                    fact.setClassName(sootClass.getName());
                                    fact.setClassPath(sootClass.getFilePath());
                                    fact.setDescription("类文件中使用注解：" + a.toString());
                                    fact.setCredibility(3);
                                    hasWebServlet.set(true);
                                }
                            }
                        });
                    }
                });
            }

            if(!hasWebServlet.get() && sootClass.hasSuperclass()){
                if(hasSuperClass(sootClass)){
                    fact.setClassNameMD5(Utils.getMD5Str(sootClass.getName()));
                    fact.setClassName(sootClass.getName());
                    fact.setClassPath(sootClass.getFilePath());
                    fact.setCredibility(1);
                    fact.setDescription("类文件继承（直接或间接）javax.servlet.http.HttpServlet");
                }
            }
            factChain.add(fact);
        }catch (Exception e){
            throw new FactAnalyzerException(e.getMessage());
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getFactDescription() {
        return DESCRIPTION;
    }

    @Override
    public String toString() {
        return getName() + "\n" + getFactDescription();
    }
}

```
