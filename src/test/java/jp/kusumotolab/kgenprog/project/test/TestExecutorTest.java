package jp.kusumotolab.kgenprog.project.test;

import static jp.kusumotolab.kgenprog.project.test.Coverage.Status.COVERED;
import static jp.kusumotolab.kgenprog.project.test.Coverage.Status.EMPTY;
import static jp.kusumotolab.kgenprog.project.test.Coverage.Status.NOT_COVERED;
import static org.assertj.core.api.Assertions.assertThat;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.junit.Test;
import jp.kusumotolab.kgenprog.ga.Variant;
import jp.kusumotolab.kgenprog.project.ClassPath;
import jp.kusumotolab.kgenprog.project.GeneratedSourceCode;
import jp.kusumotolab.kgenprog.project.ProjectBuilder;
import jp.kusumotolab.kgenprog.project.factory.TargetProject;
import jp.kusumotolab.kgenprog.project.factory.TargetProjectFactory;

public class TestExecutorTest {

  final static String bc = "example.BuggyCalculator";
  final static String bct = "example.BuggyCalculatorTest";
  final static FullyQualifiedName buggyCalculator = new TargetFullyQualifiedName(bc);
  final static FullyQualifiedName buggyCalculatorTest = new TestFullyQualifiedName(bct);

  final static String ut = "example.Util";
  final static String utt = "example.UtilTest";
  final static FullyQualifiedName util = new TargetFullyQualifiedName(ut);
  final static FullyQualifiedName utilTest = new TestFullyQualifiedName(utt);

  final static FullyQualifiedName inner = new TargetFullyQualifiedName(bc + "$InnerClass");
  final static FullyQualifiedName stInner = new TargetFullyQualifiedName(bc + "$StaticInnerClass");
  final static FullyQualifiedName anonymous = new TargetFullyQualifiedName(bc + "$1");
  final static FullyQualifiedName outer = new TargetFullyQualifiedName("example.OuterClass");

  final static FullyQualifiedName test01 = new TestFullyQualifiedName(bct + ".test01");
  final static FullyQualifiedName test02 = new TestFullyQualifiedName(bct + ".test02");
  final static FullyQualifiedName test03 = new TestFullyQualifiedName(bct + ".test03");
  final static FullyQualifiedName test04 = new TestFullyQualifiedName(bct + ".test04");

  final static FullyQualifiedName plusTest01 = new TestFullyQualifiedName(utt + ".plusTest01");
  final static FullyQualifiedName plusTest02 = new TestFullyQualifiedName(utt + ".plusTest02");
  final static FullyQualifiedName minusTest01 = new TestFullyQualifiedName(utt + ".minusTest01");
  final static FullyQualifiedName minusTest02 = new TestFullyQualifiedName(utt + ".minusTest02");
  final static FullyQualifiedName dummyTest01 = new TestFullyQualifiedName(utt + ".dummyTest01");

  private TestResults generateTestResultsForExample01() throws Exception {
    final Path rootPath = Paths.get("example/BuildSuccess01");
    final Path workPath = rootPath.resolve("bin");
    final TargetProject targetProject = TargetProjectFactory.create(rootPath);
    final Variant variant = targetProject.getInitialVariant();
    final GeneratedSourceCode generatedSourceCode = variant.getGeneratedSourceCode();
    new ProjectBuilder(targetProject).build(generatedSourceCode, workPath);

    final TestExecutor executor = new TestExecutor();
    return executor.exec(new ClassPath(workPath), Arrays.asList(buggyCalculator),
        Arrays.asList(buggyCalculatorTest));
  }

  private TestResults generateTestResultsForExample02() throws Exception {
    final Path rootPath = Paths.get("example/BuildSuccess02");
    final Path workPath = rootPath.resolve("bin");
    final TargetProject targetProject = TargetProjectFactory.create(rootPath);
    final Variant variant = targetProject.getInitialVariant();
    final GeneratedSourceCode generatedSourceCode = variant.getGeneratedSourceCode();
    new ProjectBuilder(targetProject).build(generatedSourceCode, workPath);

    final TestExecutor executor = new TestExecutor();
    return executor.exec(new ClassPath(workPath), Arrays.asList(buggyCalculator, util),
        Arrays.asList(buggyCalculatorTest, utilTest));
  }

  @SuppressWarnings("unused")
  private TestResults generateTestResultsForExample03() throws Exception {
    final Path rootPath = Paths.get("example/BuildSuccess03");
    final Path workPath = rootPath.resolve("bin");
    final TargetProject targetProject = TargetProjectFactory.create(rootPath);
    final Variant variant = targetProject.getInitialVariant();
    final GeneratedSourceCode generatedSourceCode = variant.getGeneratedSourceCode();
    new ProjectBuilder(targetProject).build(generatedSourceCode, workPath);

    final TestExecutor executor = new TestExecutor();
    return executor.exec(new ClassPath(workPath),
        Arrays.asList(buggyCalculator, util, inner, stInner, outer),
        Arrays.asList(buggyCalculatorTest, utilTest));
  }

  @Test
  public void testTestExecutorForExample01() throws Exception {
    final TestResults r = generateTestResultsForExample01();

    // example01で実行されたテストは4つのはず
    assertThat(r.getExecutedTestFQNs()).containsExactlyInAnyOrder(test01, test02, test03, test04);

    // テストの成否はこうなるはず
    assertThat(r.getTestResult(test01).failed).isFalse();
    assertThat(r.getTestResult(test02).failed).isFalse();
    assertThat(r.getTestResult(test03).failed).isTrue();
    assertThat(r.getTestResult(test04).failed).isFalse();

    final TestResult tr01 = r.getTestResult(test01);
    final TestResult tr04 = r.getTestResult(test04);

    // test01()ではBuggyCalculatorのみが実行されたはず
    assertThat(tr01.getExecutedTargetFQNs()).containsExactlyInAnyOrder(buggyCalculator);

    // test01()で実行されたBuggyCalculatorのカバレッジはこうなるはず
    assertThat(tr01.getCoverages(buggyCalculator).statuses).containsExactly(EMPTY, COVERED, EMPTY,
        COVERED, COVERED, EMPTY, EMPTY, NOT_COVERED, EMPTY, COVERED);

    // test04()で実行されたbuggyCalculatorのバレッジはこうなるはず
    assertThat(tr04.getCoverages(buggyCalculator).statuses).containsExactly(EMPTY, COVERED, EMPTY,
        COVERED, NOT_COVERED, EMPTY, EMPTY, COVERED, EMPTY, COVERED);
  }

  @Test
  public void testTestExecutorForExample02() throws Exception {
    final TestResults r = generateTestResultsForExample02();

    // example02で実行されたテストは10個のはず
    assertThat(r.getExecutedTestFQNs()).containsExactlyInAnyOrder(test01, test02, test03, test04,
        plusTest01, plusTest02, minusTest01, minusTest02, dummyTest01);

    // テストの成否はこうなるはず
    assertThat(r.getTestResult(test01).failed).isFalse();
    assertThat(r.getTestResult(test02).failed).isFalse();
    assertThat(r.getTestResult(test03).failed).isTrue();
    assertThat(r.getTestResult(test04).failed).isFalse();

    final TestResult tr01 = r.getTestResult(test01);

    // test01()ではBuggyCalculatorとUtilが実行されたはず
    assertThat(tr01.getExecutedTargetFQNs()).containsExactlyInAnyOrder(buggyCalculator, util);

    // test01()で実行されたBuggyCalculatorのカバレッジはこうなるはず
    assertThat(tr01.getCoverages(buggyCalculator).statuses).containsExactlyInAnyOrder(EMPTY,
        COVERED, EMPTY, COVERED, COVERED, EMPTY, EMPTY, NOT_COVERED, EMPTY, COVERED);

    // plusTest01()ではBuggyCalculatorとUtilが実行されたはず
    final TestResult plusTest01result = r.getTestResult(plusTest01);
    assertThat(plusTest01result.getExecutedTargetFQNs()).containsExactlyInAnyOrder(buggyCalculator,
        util);

    // plusTest01()で実行されたUtilのカバレッジはこうなるはず
    assertThat(plusTest01result.getCoverages(util).statuses).containsExactlyInAnyOrder(EMPTY,
        NOT_COVERED, EMPTY, COVERED, EMPTY, EMPTY, NOT_COVERED, EMPTY, EMPTY, NOT_COVERED,
        NOT_COVERED);

    // TODO 最後のNOT_COVERDだけ理解できない．謎．
  }

}
