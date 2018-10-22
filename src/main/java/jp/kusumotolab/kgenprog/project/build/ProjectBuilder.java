package jp.kusumotolab.kgenprog.project.build;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jp.kusumotolab.kgenprog.project.GeneratedAST;
import jp.kusumotolab.kgenprog.project.GeneratedSourceCode;
import jp.kusumotolab.kgenprog.project.SourcePath;
import jp.kusumotolab.kgenprog.project.factory.TargetProject;

/**
 * @author shin
 *
 */
public class ProjectBuilder {

  private static Logger log = LoggerFactory.getLogger(ProjectBuilder.class);

  private final TargetProject targetProject;
  private final BinaryStore binaryStore;

  public ProjectBuilder(final TargetProject targetProject) {
    this.targetProject = targetProject;
    this.binaryStore = new BinaryStore();
  }

  /**
   * @param generatedSourceCode
   * @return
   */
  public BuildResults build(final GeneratedSourceCode generatedSourceCode) {
    log.debug("enter build(GeneratedSourceCode)");

    final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    final StandardJavaFileManager standardFileManager =
        compiler.getStandardFileManager(null, null, null);
    final InMemoryClassManager inMemoryClassManager =
        new InMemoryClassManager(standardFileManager, binaryStore);

    final List<String> compilationOptions = createDefaultCompilationOptions();
    final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

    // コンパイル対象の JavaFileObject を生成
    final Set<JavaFileObject> javaSourceObjects =
        generateJavaSourceObjects(generatedSourceCode.getAllAsts());

    if (javaSourceObjects.isEmpty()) { // xxxxxxxxxxxx
      log.debug("exit build(GeneratedSourceCode, Path) -- build failed.");
      try {
        inMemoryClassManager.close();
      } catch (IOException e) {
        // TODO 自動生成された catch ブロック
        e.printStackTrace();
      }
      return EmptyBuildResults.instance;
    }

    final Set<JavaBinaryObject> resusedBinaries = extractBinaries(generatedSourceCode.getAllAsts());
    inMemoryClassManager.setClassPathBinaries(resusedBinaries);

    // コンパイルの進捗状況を得るためのWriterを生成
    final StringWriter buildProgressWriter = new StringWriter();

    // コンパイルのタスクを生成
    final CompilationTask task = compiler.getTask(buildProgressWriter, inMemoryClassManager,
        diagnostics, compilationOptions, null, javaSourceObjects);

    System.out.println("-----------------------------------------");
    System.out.println("build:        " + javaSourceObjects);
    System.out.println("   reused:    " + resusedBinaries); // xxxxxxxxxxxxxxxxx
    System.out.println("   all-cache: " + binaryStore.getAll()); // xxxxxxxxxxxxxxxxx

    // コンパイルを実行
    final boolean isBuildFailed = !task.call();

    if (isBuildFailed) {
      log.debug("exit build(GeneratedSourceCode, Path) -- build failed.");
      return EmptyBuildResults.instance;
    }

    final String buildProgressText = buildProgressWriter.toString();

    final Set<JavaBinaryObject> compiledBinaries =
        extractBinaries(generatedSourceCode.getAllAsts());
    final BinaryStore generatedBinaryStore = new BinaryStore();
    generatedBinaryStore.addAll(compiledBinaries);

    final BuildResults buildResults = new BuildResults(generatedSourceCode, false, diagnostics,
        buildProgressText, generatedBinaryStore);

    log.debug("exit build(GeneratedSourceCode, Path) -- build succeeded.");
    return buildResults;
  }


  /**
   * 指定astに対応するJavaBinaryObjectをbinaryStoreから取得する．
   * 
   * @param asts
   * @return
   */
  private Set<JavaBinaryObject> extractBinaries(List<GeneratedAST<? extends SourcePath>> asts) {
    return asts.stream()
        .map(BinaryStoreKey::new)
        .map(key -> binaryStore.get(key))
        .flatMap(Set::stream)
        .collect(Collectors.toSet());
  }

  /**
   * 指定astからコンパイル元となるJavaSourceObjectを生成する．<br>
   * ただしbinaryStoreに保持されているキャッシュがある場合はスキップ．
   * 
   * @param asts
   * @return
   */
  private Set<JavaFileObject> generateJavaSourceObjects(
      List<GeneratedAST<? extends SourcePath>> asts) {
    return asts.stream()
        .filter(ast -> !binaryStore.exists(new BinaryStoreKey(ast)))
        .map(JavaSourceObject::new)
        .collect(Collectors.toSet());
  }

  /**
   * デフォルトのコンパイルオプションを生成する．
   * 
   * @return
   */
  private List<String> createDefaultCompilationOptions() {
    final String classpaths = String.join(File.pathSeparator, this.targetProject.getClassPaths()
        .stream()
        .map(cp -> cp.path.toString())
        .collect(Collectors.toList()));

    final List<String> options = new ArrayList<>();
    options.add("-encoding");
    options.add("UTF-8");
    options.add("-classpath");
    options.add(classpaths);
    options.add("-verbose");
    return options;
  }

}
