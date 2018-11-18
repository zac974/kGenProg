package jp.kusumotolab.kgenprog;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jp.kusumotolab.kgenprog.fl.FaultLocalization;
import jp.kusumotolab.kgenprog.ga.Crossover;
import jp.kusumotolab.kgenprog.ga.Mutation;
import jp.kusumotolab.kgenprog.ga.SourceCodeGeneration;
import jp.kusumotolab.kgenprog.ga.SourceCodeValidation;
import jp.kusumotolab.kgenprog.ga.Variant;
import jp.kusumotolab.kgenprog.ga.VariantSelection;
import jp.kusumotolab.kgenprog.ga.VariantStore;
import jp.kusumotolab.kgenprog.output.PatchGenerator;
import jp.kusumotolab.kgenprog.output.PatchStore;
import jp.kusumotolab.kgenprog.project.jdt.JDTASTConstruction;
import jp.kusumotolab.kgenprog.project.test.TestExecutor;

public class KGenProgMain {

  @SuppressWarnings("unused")
  private static Logger log = LoggerFactory.getLogger(KGenProgMain.class);

  private final Configuration config;
  private final FaultLocalization faultLocalization;
  private final Mutation mutation;
  private final Crossover crossover;
  private final SourceCodeGeneration sourceCodeGeneration;
  private final SourceCodeValidation sourceCodeValidation;
  private final VariantSelection variantSelection;
  private final TestExecutor testExecutor;
  private final PatchGenerator patchGenerator;
  private final JDTASTConstruction astConstruction;

  public KGenProgMain(final Configuration config, final FaultLocalization faultLocalization,
      final Mutation mutation, final Crossover crossover,
      final SourceCodeGeneration sourceCodeGeneration,
      final SourceCodeValidation sourceCodeValidation, final VariantSelection variantSelection, final TestExecutor testExecutor,
      final PatchGenerator patchGenerator) {

    this.config = config;
    this.faultLocalization = faultLocalization;
    this.mutation = mutation;
    this.crossover = crossover;
    this.sourceCodeGeneration = sourceCodeGeneration;
    this.sourceCodeValidation = sourceCodeValidation;
    this.variantSelection = variantSelection;
    this.testExecutor = testExecutor;
    this.astConstruction = new JDTASTConstruction();
    this.patchGenerator = patchGenerator;
  }

  public List<Variant> run() {
    final Strategies strategies = new Strategies(faultLocalization, astConstruction,
        sourceCodeGeneration, sourceCodeValidation, testExecutor, variantSelection);
    final VariantStore variantStore = new VariantStore(config, strategies);
    final Variant initialVariant = variantStore.getInitialVariant();

    sourceCodeGeneration.initialize(initialVariant);
    mutation.setCandidates(initialVariant.getGeneratedSourceCode()
        .getProductAsts());

    final StopWatch stopwatch = new StopWatch(config.getTimeLimitSeconds());
    stopwatch.start();

    while (true) {

      // 変異プログラムを生成
      variantStore.addGeneratedVariants(mutation.exec(variantStore));
      variantStore.addGeneratedVariants(crossover.exec(variantStore));

      // しきい値以上の completedVariants が生成された場合は，GA を抜ける
      if (areEnoughCompletedVariants(variantStore.getFoundSolutions())) {
        break;
      }

      // 制限時間に達した場合には GA を抜ける
      if (stopwatch.isTimeout()) {
        break;
      }

      // 最大世代数に到達した場合には GA を抜ける
      if (reachedMaxGeneration(variantStore.getGenerationNumber())) {
        break;
      }

      // 次世代に向けての準備
      variantStore.proceedNextGeneration();
    }

    // 生成されたバリアントのパッチ出力
    logPatch(variantStore);

    return variantStore.getFoundSolutions(config.getRequiredSolutionsCount());
  }

  private boolean reachedMaxGeneration(final OrdinalNumber generation) {
    return config.getMaxGeneration() <= generation.get();
  }

  private boolean areEnoughCompletedVariants(final List<Variant> completedVariants) {
    return config.getRequiredSolutionsCount() <= completedVariants.size();
  }

  private void logPatch(final VariantStore variantStore) {
    final PatchStore patchStore = new PatchStore();
    final List<Variant> completedVariants =
        variantStore.getFoundSolutions(config.getRequiredSolutionsCount());

    for (final Variant completedVariant : completedVariants) {
      patchStore.add(patchGenerator.exec(completedVariant));
    }

    patchStore.writeToLogger();

    if (!config.needNotOutput()) {
      patchStore.writeToFile(config.getOutDir());
    }
  }
}
