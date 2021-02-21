package site.liangbai.lrainylib.javac.processor.subprocessor.impl

import com.sun.tools.javac.api.JavacTrees
import com.sun.tools.javac.tree.JCTree.JCClassDecl
import com.sun.tools.javac.tree.TreeMaker
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.Names
import site.liangbai.lrainylib.annotation.Plugin.EventSubscriber
import site.liangbai.lrainylib.configuration.file.YamlConfiguration
import site.liangbai.lrainylib.javac.processor.subprocessor.ISubProcessor
import java.util.*
import javax.annotation.processing.Filer
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.util.Elements

class EventSubscriberProcessor : ISubProcessor {
    override fun process(
        elementUtil: Elements,
        treeMaker: TreeMaker,
        javacTrees: JavacTrees,
        names: Names,
        filer: Filer,
        plugin: JCClassDecl,
        token: String,
        element: Element,
        initBody: ArrayList<String>,
        yaml: YamlConfiguration,
        roundEnv: RoundEnvironment
    ) {
        val elements = roundEnv.getElementsAnnotatedWith(
            EventSubscriber::class.java
        )

        elements.forEach {
            val jcTree = javacTrees.getTree(it)
            jcTree.accept(object : TreeTranslator() {
                override fun visitClassDef(jcClassDecl: JCClassDecl) {
                    val newListener = String.format("new %s()", jcClassDecl.sym.className())
                    initBody.add(
                        String.format(
                            "org.bukkit.Bukkit.getPluginManager().registerEvents(%s, plugin)",
                            newListener
                        ) + ";"
                    )
                    super.visitClassDef(jcClassDecl)
                }
            })
        }
    }
}