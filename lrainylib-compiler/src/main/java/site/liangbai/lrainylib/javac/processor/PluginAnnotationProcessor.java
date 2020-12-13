package site.liangbai.lrainylib.javac.processor;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import site.liangbai.lrainylib.configuration.ConfigurationSection;
import site.liangbai.lrainylib.configuration.file.YamlConfiguration;
import site.liangbai.lrainylib.core.annotation.CommandHandler;
import site.liangbai.lrainylib.core.annotation.Plugin;
import site.liangbai.lrainylib.core.annotation.Service;
import site.liangbai.lrainylib.core.annotation.plugin.Info;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(PluginAnnotationProcessor.class)
@SupportedAnnotationTypes({
        "site.liangbai.lrainylib.core.annotation.*"
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public final class PluginAnnotationProcessor extends AbstractProcessor {
    private Elements elements;
    private TreeMaker treeMaker;
    private JavacTrees javacTrees;
    private Names names;
    private Filer filer;
    private JCTree.JCClassDecl plugin;
    private final String token = genToken();
    private Element element;

    private final java.util.List<String> initBody = new ArrayList<>();

    private final YamlConfiguration yaml = new YamlConfiguration();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();

        treeMaker = TreeMaker.instance(context);
        names = Names.instance(context);
        javacTrees = JavacTrees.instance(processingEnv);
        filer = processingEnv.getFiler();
        elements = processingEnv.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(Plugin.class);

        if (set.size() > 1) {
            throw new IllegalStateException("can not have more than one plugin.");
        }

        set.forEach(element -> {
            PluginAnnotationProcessor.this.element = element;

            JCTree tree = javacTrees.getTree(element);

            Plugin plugin = element.getAnnotation(Plugin.class);
            Info info = plugin.info();

            yaml.set("name", info.name());
            yaml.set("version", info.version());

            tree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                    PluginAnnotationProcessor.this.plugin = jcClassDecl;

                    yaml.set("main", jcClassDecl.sym.className());

                    Set<JCTree.JCMethodDecl> methodDecls = jcClassDecl.defs.stream()
                            .filter(it -> it instanceof JCTree.JCMethodDecl)
                            .map(it -> (JCTree.JCMethodDecl) it)
                            .filter(it -> it.name.toString().equals("onEnable"))
                            .collect(Collectors.toSet());

                    if (methodDecls.size() < 1) {
                        JCTree.JCModifiers modifiers = treeMaker.Modifiers(Flags.PUBLIC);

                        Name name = names.fromString("onEnable");

                        JCTree.JCExpression returnType = treeMaker.Type(new Type.JCVoidType());

                        JCTree.JCBlock body = getRegistryBody(null);

                        JCTree.JCMethodDecl methodDecl = treeMaker.MethodDef(
                                modifiers,
                                name,
                                returnType,
                                List.nil(),
                                List.nil(),
                                List.nil(),
                                body,
                                null
                        );

                        jcClassDecl.defs = jcClassDecl.defs.prepend(methodDecl);
                    }

                    methodDecls.forEach(it -> it.body = getRegistryBody(it.body));

                    super.visitClassDef(jcClassDecl);
                }
            });

            yaml.set("authors", ImmutableList.copyOf(info.authors()));
            if (!isEmpty(info.website())) {
                yaml.set("website", info.website());
            }

            if (!isEmpty(info.description())) {
                yaml.set("description", info.description());
            }

            if (!isEmpty(plugin.apiVersion())) {
                yaml.set("api-version", plugin.apiVersion());
            }

            if (!isEmpty(plugin.prefix())) {
                yaml.set("prefix", plugin.prefix());
            }

            if (!isEmpty(plugin.classLoaderOf())) {
                yaml.set("class-loader-of", plugin.classLoaderOf());
            }

            if (!isEmpty(plugin.load())) {
                yaml.set("load", plugin.load());
            }

            if (!isEmpty(plugin.defaultPermission())) {
                yaml.set("default-permission", plugin.defaultPermission());
            }

            if (!isEmpty(plugin.depend())) {
                yaml.set("depend", ImmutableList.copyOf(plugin.depend()));
            }

            if (!isEmpty(plugin.softDepend())) {
                yaml.set("softDepend", ImmutableList.copyOf(plugin.softDepend()));
            }

            if (!isEmpty(plugin.loadBefore())) {
                yaml.set("loadBefore", ImmutableList.copyOf(plugin.loadBefore()));
            }

            initBody.add(String.format("org.bukkit.plugin.Plugin plugin = org.bukkit.plugin.java.JavaPlugin.getPlugin(%s.class);", PluginAnnotationProcessor.this.plugin.sym.className()));

            processPluginInstance(roundEnv);
            processEventSubscriber(roundEnv);
            processCommandHandler(roundEnv);
            processService(roundEnv);
            processServiceInstance(roundEnv);

            try {
                writeRegistryClass();
            } catch (IOException e) {
                e.printStackTrace();
            }

            writePluginYaml();

        });

        return true;
    }

    private void processCommandHandler(RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(CommandHandler.class);

        elements.forEach(element -> {
            JCTree jcTree = javacTrees.getTree(element);
            CommandHandler commandHandler = element.getAnnotation(CommandHandler.class);

            if (isEmpty(commandHandler.value())) {
                throw new IllegalStateException("command name can not be empty.");
            }
            ConfigurationSection section = new YamlConfiguration();

            section.set("description", commandHandler.description());

            if (!isEmpty(commandHandler.usage())) {
                section.set("usage", commandHandler.usage());
            }

            if (!isEmpty(commandHandler.aliases())) {
                section.set("aliases", ImmutableList.copyOf(commandHandler.aliases()));
            }

            if (!isEmpty(commandHandler.permission())) {
                section.set("permission", commandHandler.permission());
            }

            if (!isEmpty(commandHandler.permissionMessage())) {
                section.set("permission-message", commandHandler.permissionMessage());
            }

            yaml.set("commands." + commandHandler.value(), section);

            jcTree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                    String newHandler = String.format("new %s()", jcClassDecl.sym.className());

                    initBody.add(String.format("org.bukkit.Bukkit.getPluginCommand(\"%s\").setExecutor(%s)", commandHandler.value(), newHandler) + ";");

                    super.visitClassDef(jcClassDecl);
                }
            });
        });
    }

    private void processEventSubscriber(RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Plugin.EventSubscriber.class);

        elements.forEach(element -> {
            JCTree jcTree = javacTrees.getTree(element);

            jcTree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                    String newListener = String.format("new %s()", jcClassDecl.sym.className());

                    initBody.add(String.format("org.bukkit.Bukkit.getPluginManager().registerEvents(%s, plugin)", newListener) + ";");

                    super.visitClassDef(jcClassDecl);
                }
            });

        });

    }

    private void processService(RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Service.class);

        elements.forEach(element -> {
            JCTree jcTree = javacTrees.getTree(element);

            jcTree.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                    Service service = element.getAnnotation(Service.class);
                    String cls = jcClassDecl.sym.className();
                    String body = String.format("org.bukkit.Bukkit.getServicesManager().register(%s.class, new %s(), %s, org.bukkit.plugin.ServicePriority.valueOf(\"%s\"));", cls, cls, "plugin", service.priority());

                    initBody.add(body);

                    super.visitClassDef(jcClassDecl);
                }
            });
        });
    }

    private void processServiceInstance(RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Service.ServiceProviderInstance.class);

        elements.forEach(element -> {
            JCTree jcTree = javacTrees.getTree(element);

            jcTree.accept(new TreeTranslator() {
                @Override
                public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {
                    if(!jcVariableDecl.mods.getFlags().contains(Modifier.STATIC)) {
                        throw new IllegalStateException("service instance field must be static.");
                    }

                    Service.ServiceProviderInstance instance = element.getAnnotation(Service.ServiceProviderInstance.class);

                    String getField = String.format("%s.class.getDeclaredField(\"%s\")", jcVariableDecl.sym.owner.getQualifiedName().toString(), jcVariableDecl.sym.name.toString());

                    String body = String.format(
                            "invokeFieldAndSet(%s, %s, org.bukkit.Bukkit.getServicesManager().getRegistration(%s.class).getProvider());",
                            getField,
                            "null",
                            instance.classFullName()
                    );

                    initBody.add(body);

                    super.visitVarDef(jcVariableDecl);
                }
            });
        });
    }

    private void processPluginInstance(RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Plugin.Instance.class);

        elements.forEach(element -> {
            JCTree jcTree = javacTrees.getTree(element);

            jcTree.accept(new TreeTranslator() {
                @Override
                public void visitVarDef(JCTree.JCVariableDecl jcVariableDecl) {
                    if(!jcVariableDecl.mods.getFlags().contains(Modifier.STATIC)) {
                        throw new IllegalStateException("plugin instance field must be static.");
                    }

                    String getField = String.format("%s.class.getDeclaredField(\"%s\")", jcVariableDecl.sym.owner.getQualifiedName().toString(), jcVariableDecl.sym.name.toString());

                    Plugin.Instance instance = element.getAnnotation(Plugin.Instance.class);

                    String get = isEmpty(instance.plugin()) ? "plugin" : String.format("org.bukkit.Bukkit.getPluginManager().getPlugin(\"%s\")", instance.plugin());

                    initBody.add(String.format("invokeFieldAndSet(%s, %s, %s);", getField, "null", get));

                    super.visitVarDef(jcVariableDecl);
                }
            });
        });
    }

    private String genToken() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        String words = "1234567890qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";

        for (int i = 0; i < 8; i++) {
            sb.append(words.charAt(random.nextInt(words.length())));
        }

        return sb.toString();
    }

    private void writeRegistryClass() throws IOException {
        PackageElement packageElement = elements.getPackageOf(element);

        String packageName = packageElement.getQualifiedName().toString();

        String className = "Lrainy$Registry$" + token;

        StringBuilder bodyBuilder = new StringBuilder();

        initBody.forEach(it -> bodyBuilder.append(it).append("\n"));

        JavaFile cls = JavaFile.builder(packageName, TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(
                        MethodSpec.methodBuilder("init")
                                .addModifiers(Modifier.PUBLIC)
                                .addModifiers(Modifier.STATIC)
                                .addCode(
                                        "try {\n" + bodyBuilder.toString() +
                                                "            \n" +
                                                "        } catch (Throwable e) {\n" +
                                                "            e.printStackTrace();\n" +
                                                "        }"
                                        )
                                .build()
                )
                .addMethod(
                        MethodSpec.methodBuilder("invokeFieldAndSet")
                                .addModifiers(Modifier.PUBLIC)
                                .addModifiers(Modifier.STATIC)
                                .addParameter(Field.class, "field", Modifier.FINAL)
                                .addParameter(Object.class, "obj", Modifier.FINAL)
                                .addParameter(Object.class, "value", Modifier.FINAL)
                                .addCode(
                                        "boolean old = field.isAccessible();\n" +
                                                "\n" +
                                                "        if (!old) {\n" +
                                                "            field.setAccessible(true);\n" +
                                                "        }\n" +
                                                "\n" +
                                                "        try {\n" +
                                                "            java.lang.reflect.Field modifiersField = java.lang.reflect.Field.class.getDeclaredField(\"modifiers\");\n" +
                                                "            modifiersField.setAccessible(true);\n" +
                                                "            modifiersField.setInt(field, field.getModifiers() & ~java.lang.reflect.Modifier.FINAL);\n" +
                                                "\n" +
                                                "            field.set(obj, value);\n" +
                                                "        } catch (Throwable e) {\n" +
                                                "            e.printStackTrace();\n" +
                                                "        } finally {\n" +
                                                "            field.setAccessible(old);\n" +
                                                "        }"
                                )
                                .build()
                )
                .build()
        ).build();

        cls.writeTo(filer);
    }

    private JCTree.JCBlock getRegistryBody(JCTree.JCBlock other) {
        if (other != null) {
            return treeMaker.Block(0, List.of(
                    other, treeMaker.Exec(
                            treeMaker.Apply(List.nil(),
                                    treeMaker.Select(
                                            treeMaker.Ident(names.fromString("Lrainy$Registry$" + token)),
                                            names.fromString("init")
                                    ),
                                    List.nil()
                            )
                    )
            ));
        }

        return treeMaker.Block(0, List.of(
                treeMaker.Exec(
                        treeMaker.Apply(List.nil(),
                                treeMaker.Select(
                                        treeMaker.Ident(names.fromString("Lrainy$Registry$" + token)),
                                        names.fromString("init")
                                ),
                                List.nil()
                        )
                )
        ));
    }

    private void writePluginYaml() {
        FileObject file;
        try {
            file = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "plugin.yml");

            try (BufferedWriter writer = new BufferedWriter(file.openWriter())) {
                writer.write(yaml.saveToString());

                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isEmpty(String str) {
        return str != null && str.equals("");
    }

    private boolean isEmpty(Object[] objects) {
        return objects != null && (objects.length < 1 || objects[0].equals(""));
    }
}
