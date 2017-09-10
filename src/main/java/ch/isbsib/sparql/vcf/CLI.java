package ch.isbsib.sparql.vcf;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultFormat;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultWriter;
import org.eclipse.rdf4j.query.resultio.QueryResultIO;
import org.eclipse.rdf4j.query.resultio.QueryResultWriter;
import org.eclipse.rdf4j.query.resultio.text.csv.SPARQLResultsCSVWriter;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.eclipse.rdf4j.sail.SailException;

import ch.isbsib.sparql.http.SPARQLServer;

public class CLI {
	public static void main(String[] args) throws MalformedQueryException,
			RepositoryException, QueryEvaluationException, SailException,
			RDFHandlerException, IOException, TupleQueryResultHandlerException {
		VCFFileStore rep = new VCFFileStore();
		File dataDir = mkTempDir();
		CommandLineParser parser = new DefaultParser();
		try {
			// parse the command line arguments
			final Options options = setUpCLIParsing();
			CommandLine line = parser.parse(options, args);

			String query = (String) line.getParsedOptionValue("q");
			String vcf = (String) line.getParsedOptionValue("v");
			String port = (String) line.getParsedOptionValue("p");
			System.err.println("VCF is: " + vcf);
			if (line.hasOption("q")) {
				runSingleQueryOnCommandLine(rep, dataDir, query, vcf);
			} else if (line.hasOption("p")) {
				SPARQLServer sparqlServer = new SPARQLServer(dataDir, vcf, port);
				sparqlServer.run();
			} else {
				new HelpFormatter().printHelp("sparql-vcf", "", options, "",
						true);
			}
		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			System.exit(1);
		} catch (Exception e) {
			System.err.println("Server failed: " + e.getMessage());
			System.exit(2);
		}

	}

	private static void runSingleQueryOnCommandLine(VCFFileStore rep,
			File dataDir, String query, String vcf) {
		System.err.println("Query is: " + query);
		try {
			rep.setDataDir(dataDir);
			rep.setDirectoryWithVCFFiles(new File(vcf));
			rep.setValueFactory(SimpleValueFactory.getInstance());
			SailRepository sr = new SailRepository(rep);
			rep.initialize();
			Query pTQ = sr.getConnection().prepareQuery(QueryLanguage.SPARQL,
					query);
			if (pTQ instanceof TupleQuery) {
				System.err.println("SELECT query");
				SPARQLResultsCSVWriter handler = new SPARQLResultsCSVWriter(
						System.out);
				((TupleQuery) pTQ).evaluate(handler);
			} else if (pTQ instanceof GraphQuery) {
				System.err.println("CONSTRUCT/DESCRIBE query");
				RDFHandler createWriter = new TurtleWriter(System.out);
				((GraphQuery) pTQ).evaluate(createWriter);
			} else if (pTQ instanceof BooleanQuery) {
				System.err.println("ASK query");
				QueryResultWriter createWriter = QueryResultIO
						.createWriter(BooleanQueryResultFormat.TEXT, System.out);
				boolean evaluate = ((BooleanQuery) pTQ).evaluate();
				createWriter.startDocument();
				createWriter.startHeader();
				createWriter.endHeader();
				createWriter.handleBoolean(evaluate);
				createWriter.endQueryResult();
			}
		} catch (MalformedQueryException qe) {
			System.err.println("Query is wrong:" + qe.getMessage());
			System.exit(1);
		} catch (RepositoryException re) {
			System.err.println("Repository is wrong:" + re.getMessage());
			System.exit(1);
		} catch (QueryEvaluationException re) {
			System.err.println("Query evaluation errored:" + re.getMessage());
			System.exit(1);
		} catch (Exception re) {
			System.err.println("Annother exception errored:" + re.getMessage());
			System.exit(1);
		} finally {
			System.err.println("done");
			deleteDir(dataDir);
			System.exit(0);
		}
	}

	private static Options setUpCLIParsing() {
		Options options = new Options();
		final Option queryOption = Option.builder("q").hasArg()
				.desc("SPARQL query").longOpt("query").build();

		final Option vcfOption = Option.builder("v").hasArg().desc("VCF file")
				.longOpt("vcf").build();

		final Option portOption = Option.builder("p").hasArg()
				.desc("http port").longOpt("port").build();

		final OptionGroup serverOrCli = new OptionGroup();
		// final OptionGroup cli = new OptionGroup();
		serverOrCli.addOption(portOption);
		serverOrCli.addOption(queryOption);
		options.addOptionGroup(serverOrCli);
		options.addOption(vcfOption);
		return options;
	}

	protected static void deleteDir(File dataDir) {
		for (File file : dataDir.listFiles()) {
			if (file.isFile()) {
				if (!file.delete())
					file.deleteOnExit();
			} else if (file.isDirectory())
				deleteDir(file);
		}
		if (!dataDir.delete()) {
			dataDir.deleteOnExit();
		}
	}

	protected static File mkTempDir() {
		File dataDir = new File(System.getProperty("java.io.tmpdir")
				+ "/sparql-bed-temp");
		int i = 0;
		while (dataDir.exists()) {
			dataDir = new File(System.getProperty("java.io.tmpdir")
					+ "/sparql-bed-temp" + i++);
		}
		dataDir.mkdir();
		return dataDir;
	}
}
