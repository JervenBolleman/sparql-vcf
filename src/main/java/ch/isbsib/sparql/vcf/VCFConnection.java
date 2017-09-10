package ch.isbsib.sparql.vcf;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.common.iteration.CloseableIteratorIteration;
import org.eclipse.rdf4j.common.iteration.EmptyIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.evaluation.EvaluationStrategy;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.BindingAssigner;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.CompareOptimizer;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.ConjunctiveConstraintSplitter;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.ConstantOptimizer;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.DisjunctiveConstraintOptimizer;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.FilterOptimizer;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.IterativeEvaluationOptimizer;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.OrderLimitOptimizer;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.QueryModelNormalizer;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.SameTermFilterOptimizer;
import org.eclipse.rdf4j.query.impl.EmptyBindingSet;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.UnknownSailTransactionStateException;
import org.eclipse.rdf4j.sail.UpdateContext;
//import org.eclipse.rdf4j.sail.UnknownSailTransactionStateException;
//import org.eclipse.rdf4j.sail.UpdateContext;

public class VCFConnection implements SailConnection {
	private final File dir;
	private final ValueFactory vf;

	public VCFConnection(File dir, ValueFactory vf) {
		super();
		this.dir = dir;
		this.vf = vf;
	}

	@Override
	public boolean isOpen() throws SailException {
		return true;
	}

	@Override
	public void close() throws SailException {

	}

	@Override
	public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(
			TupleExpr tupleExpr, Dataset dataset, BindingSet bindings,
			boolean includeInferred) throws SailException {
		try {
			VCFTripleSource tripleSource = new VCFTripleSource(dir, vf);
			EvaluationStrategy strategy = new OneLineAwareEvaluationStrategy(
					tripleSource);
			tupleExpr = tupleExpr.clone();
			new BindingAssigner().optimize(tupleExpr, dataset, bindings);
			new ConstantOptimizer(strategy).optimize(tupleExpr, dataset,
					bindings);
			new CompareOptimizer().optimize(tupleExpr, dataset, bindings);
			new ConjunctiveConstraintSplitter().optimize(tupleExpr, dataset,
					bindings);
			new DisjunctiveConstraintOptimizer().optimize(tupleExpr, dataset,
					bindings);
			new SameTermFilterOptimizer()
					.optimize(tupleExpr, dataset, bindings);
			new QueryModelNormalizer().optimize(tupleExpr, dataset, bindings);

			// new SubSelectJoinOptimizer().optimize(tupleExpr, dataset,
			// bindings);
			new IterativeEvaluationOptimizer().optimize(tupleExpr, dataset,
					bindings);
			new FilterOptimizer().optimize(tupleExpr, dataset, bindings);
			new OrderLimitOptimizer().optimize(tupleExpr, dataset, bindings);

			return strategy.evaluate(tupleExpr, EmptyBindingSet.getInstance());
		} catch (QueryEvaluationException e) {
			throw new SailException(e);
		}
	}

	@Override
	public CloseableIteration<? extends Resource, SailException> getContextIDs()
			throws SailException {
		return new EmptyIteration<Resource, SailException>();
	}

	@Override
	public CloseableIteration<? extends Statement, SailException> getStatements(
			Resource subj, IRI pred, Value obj, boolean includeInferred,
			Resource... contexts) throws SailException {

		VCFFileFilterReader bedFileFilterReader;
		try {
			bedFileFilterReader = new VCFFileFilterReader(dir, subj, pred, obj,
					contexts, vf);
		} catch (IOException e1) {
			throw new SailException(e1);
		}
		return new CloseableIteratorIteration<Statement, SailException>() {

			@Override
			public boolean hasNext() throws SailException {
				try {
					return bedFileFilterReader.hasNext();
				} catch (QueryEvaluationException e) {
					throw new SailException(e);
				}
			}

			@Override
			public Statement next() throws SailException {
				try {
					return bedFileFilterReader.next();
				} catch (QueryEvaluationException e) {
					throw new SailException(e);
				}
			}

			@Override
			protected void handleClose() throws SailException {
				try {
					bedFileFilterReader.close();
				} catch (QueryEvaluationException e) {
					throw new SailException(e);
				}
				super.handleClose();
			}
		};

	}

	@Override
	public long size(Resource... contexts) throws SailException {
		for (Resource context : contexts)
			if (context == null) {
				try (final VCFFileFilterReader bedFileFilterReader = new VCFFileFilterReader(
						dir, null, null, null, null, vf)) {
					long count = 0L;
					try {
						while (bedFileFilterReader.hasNext()) {
							bedFileFilterReader.next();
							count++;
						}
					} catch (QueryEvaluationException e) {
						throw new SailException(e);
					}
					return count;
				} catch (QueryEvaluationException | IOException e1) {
					throw new SailException(e1);
				}
			}
		return 0;
	}

	// @Override
	// public void begin() throws SailException {
	// throw new SailException("BED files can not be updated via SPARQL");
	// }
	//
	// @Override
	// public void prepare() throws SailException {
	// // TODO Auto-generated method stub
	//
	// }

	@Override
	public void commit() throws SailException {
		throw new SailException("BED files can not be updated via SPARQL");

	}

	@Override
	public void rollback() throws SailException {
		// TODO Auto-generated method stub

	}

	// @Override
	// public boolean isActive() throws UnknownSailTransactionStateException {
	// return false;
	// }

	@Override
	public void addStatement(Resource subj, IRI pred, Value obj,
			Resource... contexts) throws SailException {
		throw new SailException("BED files can not be updated via SPARQL");

	}

	@Override
	public void removeStatements(Resource subj, IRI pred, Value obj,
			Resource... contexts) throws SailException {
		throw new SailException("BED files can not be updated via SPARQL");

	}

	// @Override
	// public void startUpdate(UpdateContext op) throws SailException {
	// throw new SailException("BED files can not be updated via SPARQL");
	//
	// }
	//
	// @Override
	// public void addStatement(UpdateContext op, Resource subj, URI pred,
	// Value obj, Resource... contexts) throws SailException {
	// throw new SailException("BED files can not be updated via SPARQL");
	//
	// }
	//
	// @Override
	// public void removeStatement(UpdateContext op, Resource subj, URI pred,
	// Value obj, Resource... contexts) throws SailException {
	// throw new SailException("BED files can not be updated via SPARQL");
	//
	// }
	//
	// @Override
	// public void endUpdate(UpdateContext op) throws SailException {
	// throw new SailException("BED files can not be updated via SPARQL");
	//
	// }

	@Override
	public void clear(Resource... contexts) throws SailException {
		throw new SailException("BED files can not be updated via SPARQL");

	}

	@Override
	public CloseableIteration<? extends Namespace, SailException> getNamespaces()
			throws SailException {

		return new CloseableIteratorIteration<Namespace, SailException>() {
			private Iterator<Namespace> namespaces = Arrays.asList(
					new Namespace[] {
							new SimpleNamespace(FALDO.PREFIX, FALDO.NAMESPACE),
							new SimpleNamespace(VCF.PREFIX, VCF.NAMESPACE) })
					.iterator();

			@Override
			public boolean hasNext() throws SailException {
				return namespaces.hasNext();
			}

			@Override
			public Namespace next() throws SailException {
				return namespaces.next();
			};
		};
	}

	@Override
	public String getNamespace(String prefix) throws SailException {
		if (FALDO.PREFIX.equals(prefix))
			return FALDO.NAMESPACE;
		else if (VCF.PREFIX.equals(prefix))
			return VCF.NAMESPACE;
		return null;
	}

	@Override
	public void setNamespace(String prefix, String name) throws SailException {
		throw new SailException("BED files can not be updated via SPARQL");

	}

	@Override
	public void removeNamespace(String prefix) throws SailException {
		throw new SailException("BED files can not be updated via SPARQL");

	}

	@Override
	public void clearNamespaces() throws SailException {
		throw new SailException("BED files can not be updated via SPARQL");

	}

	@Override
	public void addStatement(UpdateContext arg0, Resource arg1, IRI arg2,
			Value arg3, Resource... arg4) throws SailException {
		throw new SailException("BED files can not be updated via SPARQL");
	}

	@Override
	public void begin() throws SailException {
		throw new SailException("BED files can not be updated via SPARQL");

	}

	@Override
	public void endUpdate(UpdateContext arg0) throws SailException {
		throw new SailException("BED files can not be updated via SPARQL");

	}

	@Override
	public boolean isActive() throws UnknownSailTransactionStateException {
		return false;
	}

	@Override
	public void prepare() throws SailException {
		throw new SailException("BED files can not be updated via SPARQL");
	}

	@Override
	public void removeStatement(UpdateContext arg0, Resource arg1, IRI arg2,
			Value arg3, Resource... arg4) throws SailException {
		throw new SailException("BED files can not be updated via SPARQL");
	}

	@Override
	public void startUpdate(UpdateContext arg0) throws SailException {
		throw new SailException("BED files can not be updated via SPARQL");
	}

	@Override
	public void begin(IsolationLevel arg0)
			throws UnknownSailTransactionStateException, SailException {
		// TODO Auto-generated method stub

	}

	@Override
	public void flush() throws SailException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasStatement(Resource subj, IRI pred, Value obj,
			boolean includeInferred, Resource... contexts) throws SailException {
		// TODO Auto-generated method stub
		return false;
	}

}
