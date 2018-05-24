package de.peeeq.wurstio.hotdoc;

import com.google.common.collect.Lists;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.File;
import java.io.StringWriter;
import java.util.List;

public class HotDocWithThyme {
    private List<String> files;
    private File outputfolder;

    public HotDocWithThyme(List<String> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        this.files = Lists.newArrayList(files);
        this.outputfolder = new File(this.files.remove(files.size() - 1));

        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode("HTML");
        templateEngine.setTemplateResolver(templateResolver);

        Context context = new Context();
        context.setVariable("name", "World");
        StringWriter stringWriter = new StringWriter();
        templateEngine.process("HelloWorld.html", context, stringWriter);

    }

    public void generateDoc() {

    }
}
