package ch.isbsib.sparql.http;

import java.io.File;

import org.eclipse.jetty.server.Server;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.sail.SailRepository;

//import com.sun.net.httpserver.HttpServer;
import ch.isbsib.sparql.vcf.VCFFileStore;

public class SPARQLServer {
	private final SailRepository sr;
	private final Server server;

	public SPARQLServer(File dataDir, String vcfdir, String port)
			throws Exception {
		VCFFileStore rep = new VCFFileStore();
		rep.setDataDir(dataDir);
		rep.setDirectoryWithVCFFiles(new File(vcfdir));
		rep.setValueFactory(SimpleValueFactory.getInstance());
		this.sr = new SailRepository(rep);
//		rep.initialize();
		this.sr.initialize();

		server = new Server(Integer.parseInt(port));
		server.setHandler(new SparqlHandler(sr));
		System.err.println("Starting server");
		

	}

	public void run() throws Exception, InterruptedException {

		server.start();
		server.dumpStdErr();
		server.join();
	}

}
