package ch.isbsib.sparql.http;

import java.awt.image.renderable.RenderableImageOp;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.dsig.keyinfo.PGPData;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultWriter;
import org.openrdf.query.resultio.BooleanQueryResultWriterRegistry;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
import org.openrdf.query.resultio.TupleQueryResultWriterRegistry;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailRepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerBase;

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
				final TupleQueryResultFormat ff = r.getFileFormatForMIMEType(
						mt, TupleQueryResultFormat.TSV);
				final TupleQueryResultWriter writer = QueryResultIO
						.createWriter(ff, response.getOutputStream());
				((TupleQuery) pqpQ).evaluate(writer);
			} else if (pqpQ instanceof GraphQuery) {

				final RDFFormat wmt = Rio.getWriterFormatForMIMEType(mt);
				final RDFWriter writer = Rio.createWriter(wmt,
						response.getOutputStream());
				((GraphQuery) pqpQ).evaluate(writer);
			} else if (pqpQ instanceof BooleanQuery) {

				final BooleanQueryResultWriterRegistry r = BooleanQueryResultWriterRegistry
						.getInstance();
				final BooleanQueryResultFormat ff = r.getFileFormatForMIMEType(
						mt, BooleanQueryResultFormat.TEXT);
				final BooleanQueryResultWriter writer = QueryResultIO
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
