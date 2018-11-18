package jp.kusumotolab.kgenprog.project.test;

import jp.kusumotolab.kgenprog.ga.Gene;
import jp.kusumotolab.kgenprog.project.GeneratedSourceCode;

public interface TestExecutor {
  public TestResults exec(Gene gene, GeneratedSourceCode generatedSourceCode);
}
