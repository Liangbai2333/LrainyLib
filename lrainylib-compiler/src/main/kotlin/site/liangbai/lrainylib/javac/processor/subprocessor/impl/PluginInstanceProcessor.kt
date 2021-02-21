package site.liangbai.lrainylib.javac.processor.subprocessor.impl

import com.sun.tools.javac.api.JavacTrees
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.JCTree.JCVariableDecl
import com.sun.tools.javac.tree.TreeMaker
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.Names
import site.liangbai.lrainylib.annotation.Plugin
import site.liangbai.lrainylib.configuration.file.YamlConfiguration
import site.liangbai.lrainylib.javac.processor.subprocessor.ISubProcessor
import site.liangbai.lrainylib.util.isEmpty
import java.util.*
import javax.annotation.processing.Filer
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.util.Elements

class PluginInstanceProcessor : ISubProcessor {
    override fun process(
        elementUtil: Elements,
        treeMaker: TreeMaker,
        javacTrees: JavacTrees,
        names: Names,
        filer: Filer,
        plugin: JCTree.JCClassDecl,
        token: String,
        element: Element,
        initBody: ArrayList<String>,
        yaml: YamlConfiguration,
        roundEnv: RoundEnvironment
    ) {
        val elements = roundEnv.getElementsAnnotatedWith(
            Plugin.Instance::class.java
        )

        elements.forEach {
            val jcTree = javacTrees.getTree(it)
            jcTree.accept(object : TreeTranslator() {
                override fun visitVarDef(jcVariableDecl: JCVariableDecl) {
                    check(
                        jcVariableDecl.mods.getFlags().contains(Modifier.STATIC)
                    ) { "plugin instance field must be static." }
                    val getField = String.format(
                        "%s.class.getDeclaredField(\"%s\")",
                        jcVariableDecl.sym.owner.qualifiedName.toString(),
                        jcVariableDecl.sym.name.toString()
                    )
                    val instance =
                        it!!.getAnnotation(Plugin.Instance::class.java)
                    val get = if (instance.plugin.isEmpty()) "plugin" else String.format(
                        "org.bukkit.Bukkit.getPluginManager().getPlugin(\"%s\")",
                        instance.plugin
                    )
                    initBody.add(String.format("invokeFieldAndSet(%s, %s, %s);", getField, "null", get))
                    super.visitVarDef(jcVariableDecl)
                }
            })
        }
    }
}