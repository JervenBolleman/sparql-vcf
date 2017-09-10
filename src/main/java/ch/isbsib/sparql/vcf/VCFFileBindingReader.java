package ch.isbsib.sparql.vcf;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.Join;
import org.eclipse.rdf4j.query.algebra.StatementPattern;

public class VCFFileBindingReader implements
		CloseableIteration<BindingSet, QueryEvaluationException> {

	private final BindingReaderRunner runner;
	private final static ExecutorService exec = Executors.newFixedThreadPool(
			Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
				int a = 0;

				@Override
				public Thread newThread(Runnable arg0) {

					return new Thread(arg0, "VCFFileBindingReader" + (a++));
				}
			});
	private final BlockingQueue<BindingSet> queue;
	private BindingSet next;
	private boolean closed;

	public VCFFileBindingReader(File file, BindingSet bindings, Join join,
			ValueFactory valueFactory) {
		
		StatementPattern left = (StatementPattern) join.getLeftArg();
		StatementPattern right = (StatementPattern) join.getRightArg();
		queue = new ArrayBlockingQueue<BindingSet>(1000);
		System.err.println("reading file:" +file.getAbsolutePath());
		runner = new BindingReaderRunner(file, queue, left, right,
				valueFactory, bindings);
		exec.submit(runner);
	}

	@Override
	public boolean hasNext() throws QueryEvaluationException {
		next = null;
		while (!runner.done || !queue.isEmpty()) {
			try {
				next = queue.poll(1, TimeUnit.SECONDS);
				if (next != null) {
					return true;
				}
			} catch (InterruptedException e) {
				Thread.interrupted();
			}
		}
		return false;
	}

	@Override
	public BindingSet next() throws QueryEvaluationException {
		if (next == null)
			throw new NoSuchElementException("Iteration is exhausted");
		if (closed)
			throw new IllegalStateException("Iteration is closed");
		return next;
	}

	@Override
	public void remove() throws QueryEvaluationException {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() throws QueryEvaluationException {
		runner.done = true;
		closed = true;
	}

}
