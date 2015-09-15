package ch.isbsib.sparql.http;

import java.io.File;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.repository.sail.SailRepository;

//import com.sun.net.httpserver.HttpServer;
import ch.isbsib.sparql.vcf.VCFFileStore;

public class SPARQLServer {
	private final SailRepository sr;
	private final Server server;

	public SPARQLServer(File dataDir, String vcffile, String port)
			throws Exception {
		VCFFileStore rep = new VCFFileStore();
		rep.setDataDir(dataDir);
		rep.setBedFile(new File(vcffile));
		rep.setValueFactory(new SimpleValueFactory());
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
