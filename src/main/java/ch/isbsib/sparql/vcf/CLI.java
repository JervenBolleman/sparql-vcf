package ch.isbsib.sparql.vcf;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultWriter;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.text.csv.SPARQLResultsCSVWriter;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.SailException;

public class CLI {
	public static void main(String[] args) throws MalformedQueryException,
			RepositoryException, QueryEvaluationException, SailException,
			RDFHandlerException, IOException, TupleQueryResultHandlerException {
		VCFFileStore rep = new VCFFileStore();
		File dataDir = mkTempDir();
		CommandLineParser parser = new DefaultParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(setUpCLIParsing(), args);

			String query = (String) line.getParsedOptionValue("q");
			String vcf = (String) line.getParsedOptionValue("v");
			System.err.println("VCF is: " + vcf);
			if (line.hasOption("q")) {
				runSingleQueryOnCommandLine(rep, dataDir, query, vcf);
			}
		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			System.exit(1);
		}

	}

	private static void runSingleQueryOnCommandLine(VCFFileStore rep,
			File dataDir, String query, String vcf) {
		System.err.println("Query is: " + query);
		try {
			rep.setDataDir(dataDir);
			rep.setBedFile(new File(vcf));
			rep.setValueFactory(new SimpleValueFactory());
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
				System.err.println("GRAPH query");
				RDFHandler createWriter = new TurtleWriter(System.out);
				((GraphQuery) pTQ).evaluate(createWriter);
			} else if (pTQ instanceof BooleanQuery) {
				System.err.println("Boolean query");
				BooleanQueryResultWriter createWriter = QueryResultIO
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
