package ch.isbsib.sparql.vcf;

import java.util.Iterator;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.QueryEvaluationException;


public class ConcatenatingCloseabelIterator<T extends Statement> implements
		CloseableIteration<T, QueryEvaluationException> {
	private final Iterator<CloseableIteration<T, QueryEvaluationException>> iter;

	public ConcatenatingCloseabelIterator(
			Iterator<CloseableIteration<T, QueryEvaluationException>> iter) {
		super();
		this.iter = iter;
	}

	private CloseableIteration<T, QueryEvaluationException> current;

	@Override
	public boolean hasNext() throws QueryEvaluationException {
		if (current != null && current.hasNext()) {
			return true;
		} else {
			while (iter.hasNext()) {
				current = iter.next();
				if (current.hasNext())
					return true;
			}
			return false;
		}
	}

	@Override
	public T next() throws QueryEvaluationException {
		return current.next();
	}

	@Override
	public void remove() throws QueryEvaluationException {
		current.remove();

	}

	@Override
	public void close() throws QueryEvaluationException {
		current.close();
	}

}
