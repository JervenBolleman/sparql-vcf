package ch.isbsib.sparql.vcf;

import java.util.Arrays;
import java.util.List;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.Join;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolverImpl;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.SimpleEvaluationStrategy;

public class OneLineAwareEvaluationStrategy extends SimpleEvaluationStrategy  {

	public OneLineAwareEvaluationStrategy(VCFTripleSource tripleSource) {
		super(tripleSource, new FederatedServiceResolverImpl());
	}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(
			Join join, BindingSet bindings) throws QueryEvaluationException {
		if (oneLine(join)) {
			return ((VCFTripleSource) tripleSource).getStatements(bindings,
					join);
		} else
			return super.evaluate(join, bindings);
	}

	private boolean oneLine(Join join) throws QueryEvaluationException {
		TupleExpr la = join.getLeftArg();
		TupleExpr ra = join.getRightArg();
		if (la instanceof StatementPattern && ra instanceof StatementPattern) {
			StatementPattern lp = (StatementPattern) la;
			StatementPattern rp = (StatementPattern) ra;
			if (lp.getSubjectVar().equals(rp.getSubjectVar())
					&& allowAblePredicate(lp.getPredicateVar())
					&& allowAblePredicate(rp.getPredicateVar()))
				return true;
		}
		return false;
	}

	private List<IRI> predicates = Arrays
			.asList(new IRI[] { FALDO.POSTION_PREDICATE, FALDO.BEGIN_PREDICATE,
					FALDO.END_PREDICATE });

	private boolean allowAblePredicate(Var predicateVar) {
		for (IRI pred : predicates) {
			if (pred.equals(predicateVar.getValue()))
				return true;
		}
		return false;
	}

}
