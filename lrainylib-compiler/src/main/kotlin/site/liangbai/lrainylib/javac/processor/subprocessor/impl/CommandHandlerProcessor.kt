package site.liangbai.lrainylib.javac.processor.subprocessor.impl

import com.google.common.collect.ImmutableList
import com.sun.tools.javac.api.JavacTrees
import com.sun.tools.javac.tree.JCTree.JCClassDecl
import com.sun.tools.javac.tree.TreeMaker
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.Names
import site.liangbai.lrainylib.annotation.command.CommandHandler
import site.liangbai.lrainylib.configuration.ConfigurationSection
import site.liangbai.lrainylib.configuration.file.YamlConfiguration
import site.liangbai.lrainylib.javac.processor.subprocessor.ISubProcessor
import site.liangbai.lrainylib.util.isEmpty
import java.util.*
import javax.annotation.processing.Filer
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.util.Elements

class CommandHandlerProcessor : ISubProcessor {
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
            CommandHandler::class.java
        )

        elements.forEach {
            val jcTree = javacTrees.getTree(it)
            val commandHandler =
                it!!.getAnnotation(CommandHandler::class.java)
            check(!commandHandler.value.isEmpty()) { "command name can not be empty." }
            val section: ConfigurationSection =
                YamlConfiguration()
            section["description"] = commandHandler.description
            if (!commandHandler.usage.isEmpty()) {
                section["usage"] = commandHandler.usage
            }
            if (!commandHandler.aliases.isEmpty()) {
                section["aliases"] = ImmutableList.copyOf(commandHandler.aliases)
            }
            if (!commandHandler.permission.isEmpty()) {
                section["permission"] = commandHandler.permission
            }
            if (!commandHandler.permissionMessage.isEmpty()) {
                section["permission-message"] = commandHandler.permissionMessage
            }
            yaml["commands.${commandHandler.value}"] = section
            jcTree.accept(object : TreeTranslator() {
                override fun visitClassDef(jcClassDecl: JCClassDecl) {
                    val newHandler = String.format("new %s()", jcClassDecl.sym.className())
                    initBody.add(
                        String.format(
                            "org.bukkit.Bukkit.getPluginCommand(\"%s\").setExecutor(%s)",
                            commandHandler.value,
                            newHandler
                        ) + ";"
                    )
                    super.visitClassDef(jcClassDecl)
                }
            })
        }
    }
}