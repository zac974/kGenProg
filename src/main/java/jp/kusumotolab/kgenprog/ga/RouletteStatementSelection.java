package jp.kusumotolab.kgenprog.ga;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.eclipse.jdt.core.dom.Statement;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import jp.kusumotolab.kgenprog.project.FullyQualifiedName;
import jp.kusumotolab.kgenprog.project.GeneratedAST;
import jp.kusumotolab.kgenprog.project.ProductSourcePath;

public class RouletteStatementSelection implements CandidateSelection {

  private final Random random;
  private Roulette<Statement> roulette;

  private final Multimap<String, Statement> packageNameStatementMultimap = HashMultimap.create();
  private final Multimap<FullyQualifiedName, Statement> fqnStatementMultiMap = HashMultimap.create();

  private final Map<String, Roulette<Statement>> packageNameRouletteMap = new HashMap<>();
  private final Map<FullyQualifiedName, Roulette<Statement>> fqnRouletteMap = new HashMap<>();

  public RouletteStatementSelection(final Random random) {
    this.random = random;
  }

  @Override
  public void setCandidates(final List<GeneratedAST<ProductSourcePath>> candidates) {
    final StatementVisitor visitor = new StatementVisitor(candidates);

    final List<ReuseCandidate<Statement>> statements = visitor.getStatements();

    for (final ReuseCandidate<Statement> statement : statements) {
      final Statement value = statement.getValue();
      packageNameStatementMultimap.put(statement.getPackageName(), value);
      fqnStatementMultiMap.put(statement.getFqn(), value);
    }

    final List<Statement> statementList = statements.stream()
        .map(ReuseCandidate::getValue)
        .collect(Collectors.toList());
    roulette = createStatement(statementList);
  }

  protected int getStatementWeight(final Statement statement) {
    final StatementVisitor statementVisitor = new StatementVisitor(statement);
    final List<ReuseCandidate<Statement>> statements = statementVisitor.getStatements();
    return statements.size();
  }

  @Override
  public Statement exec() {
    return roulette.exec();
  }

  @Override
  public Statement exec(final FullyQualifiedName fqn) {
    return getRoulette(fqn.getPackageName()).exec();
  }

  private Roulette<Statement> getRoulette(final String packageName) {
    if (packageName == null || packageName.isEmpty()) {
      return roulette;
    }
    Roulette<Statement> roulette = packageNameRouletteMap.get(packageName);
    if (roulette != null) {
      return roulette;
    }

    final Collection<Statement> statementCollection = packageNameStatementMultimap.get(packageName);
    final List<Statement> statements = convertToList(statementCollection);
    if (statements.isEmpty()) {
      return this.roulette;
    }
    roulette = createStatement(statements);
    packageNameRouletteMap.put(packageName, roulette);
    return roulette;
  }

  private List<Statement> convertToList(final Collection<Statement> collection) {
    return new ArrayList<>(collection);
  }

  private Roulette<Statement> createStatement(final List<Statement> statements) {
    final Function<Statement, Double> weightFunction = statement -> {
      final int statementWeight = getStatementWeight(statement);

      final double inverse = 1 / ((double) statementWeight);
      return Math.pow(inverse, 2);
    };
    return new Roulette<>(statements, weightFunction, random);
  }
}
