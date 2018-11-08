package jp.kusumotolab.kgenprog.ga;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jp.kusumotolab.kgenprog.OrdinalNumber;
import jp.kusumotolab.kgenprog.project.PatchGenerator;
import jp.kusumotolab.kgenprog.project.Patches;

public class EvolutionLogger {

  private static Logger log = LoggerFactory.getLogger(EvolutionLogger.class);

  public void log(final List<Variant> currentVariants) {
    double max = 0.0d;
    double min = 1.0d;

    int maxCounter = 0;
    int minCounter = 0;

    Variant maxVariant = null;
    Variant minVariant = null;

    for (final Variant currentVariant : currentVariants) {
      final Fitness fitness = currentVariant.getFitness();

      if (max < fitness.getValue()) {
        maxCounter = 0;
        max = fitness.getValue();
        maxVariant = currentVariant;
      }

      if (min > fitness.getValue()) {
        minCounter = 0;
        min = fitness.getValue();
        minVariant = currentVariant;
      }

      if (isEqual(max, fitness.getValue())) {
        maxCounter += 1;
        maxVariant = isInitialVariant(currentVariant) ? maxVariant : currentVariant;
      }

      if (isEqual(min, fitness.getValue())) {
        minCounter += 1;
        minVariant =
            isInitialVariant(currentVariant) || maxVariant.equals(currentVariant) ? minVariant
                : currentVariant;
      }
    }

    log.info("max fitness({}): {}", maxCounter, max);
    log.info("min fitness({}): {}", minCounter, min);
    writeToLogger(maxVariant);
    //writeToLogger(minVariant);
  }

  private boolean isEqual(final double a, final double b) {
    return Math.abs(a - b) < 0.000000000001;
  }

  private void writeToLogger(final Variant variant) {
    if (isInitialVariant(variant)) {
      log.info("NO DIFF");
    } else {
      final PatchGenerator patchGenerator = new PatchGenerator();
      final Patches patches = patchGenerator.exec(variant);
      patches.writeToLogger();
    }
  }

  private boolean isInitialVariant(final Variant variant) {
    final OrdinalNumber generationNumber = variant.getGenerationNumber();
    final int rawValue = generationNumber.get();
    return rawValue == 0;
  }
}
