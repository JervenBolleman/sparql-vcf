package ch.isbsib.sparql.http;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultFormat;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultWriterRegistry;
import org.eclipse.rdf4j.query.resultio.QueryResultFormat;
import org.eclipse.rdf4j.query.resultio.QueryResultIO;
import org.eclipse.rdf4j.query.resultio.QueryResultWriter;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultFormat;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriterRegistry;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

public class SparqlHandler extends AbstractHandler {

	private final SailRepository sr;

	protected SparqlHandler(SailRepository sr) {
		this.sr = sr;
	}

	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		final String query = request.getParameter("query");
		final String mt = getBestMimeType(request);
		if (query == null || query.isEmpty())
			showHelp(response);
		else
			dealWithQuery(request, response, query, mt);

	}

	private void showHelp(HttpServletResponse response) throws IOException {
		response.getOutputStream().write(
				"<html><body><h1>This http endpoint expects sparql requests</h1></body></html>"
						.getBytes(StandardCharsets.UTF_8));
		response.getOutputStream().flush();

	}

	private void dealWithQuery(HttpServletRequest request,
			HttpServletResponse response, final String query, String mt)
			throws IOException {

		try (final SailRepositoryConnection connection = sr.getConnection()) {
			final Query pqpQ = connection.prepareQuery(QueryLanguage.SPARQL,
					query);
			if (pqpQ instanceof TupleQuery) {

				final TupleQueryResultWriterRegistry r = TupleQueryResultWriterRegistry
						.getInstance();
				final QueryResultFormat ff = r.getFileFormatForMIMEType(
						mt).orElse(TupleQueryResultFormat.TSV);
				final TupleQueryResultWriter writer = (TupleQueryResultWriter) QueryResultIO
						.createWriter(ff, response.getOutputStream());
				((TupleQuery) pqpQ).evaluate(writer);
			} else if (pqpQ instanceof GraphQuery) {

				final RDFFormat wmt = Rio.getWriterFormatForMIMEType(mt).orElse(RDFFormat.RDFXML);
				final RDFWriter writer = Rio.createWriter(wmt,
						response.getOutputStream());
				((GraphQuery) pqpQ).evaluate(writer);
			} else if (pqpQ instanceof BooleanQuery) {

				final BooleanQueryResultWriterRegistry r = BooleanQueryResultWriterRegistry
						.getInstance();
				final QueryResultFormat ff = r.getFileFormatForMIMEType(
						mt).orElse(BooleanQueryResultFormat.TEXT);
				final QueryResultWriter writer = QueryResultIO
						.createWriter(ff, response.getOutputStream());
				writer.handleBoolean(((BooleanQuery) pqpQ).evaluate());
			}

		}
	}

	private String getBestMimeType(HttpServletRequest request) {
		final String header = request.getHeader("Accept");
		return header.split(":")[0];
	}
}
