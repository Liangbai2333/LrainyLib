package site.liangbai.lrainylib.javac.processor.subprocessor

import com.sun.tools.javac.api.JavacTrees
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.TreeMaker
import com.sun.tools.javac.util.Names
import site.liangbai.lrainylib.configuration.file.YamlConfiguration
import java.util.ArrayList
import javax.annotation.processing.Filer
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.util.Elements

interface ISubProcessor {
    fun process(
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
    )
}