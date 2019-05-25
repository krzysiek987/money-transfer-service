package pl.dorzak.transactionservice;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import javax.sql.DataSource;
import pl.dorzak.transactionservice.config.DataSourceFactory;
import pl.dorzak.transactionservice.config.ServerConfig;
import pl.dorzak.transactionservice.transfer.TransferModule;
import ratpack.server.RatpackServer;

/**
 * Initializes APIs and all necessary components like HTTP server.
 * This is main entry point to the microservice.
 */
public class TransactionService {

	private final RatpackServer server;

	public TransactionService(final ServerConfig serverConfig, final TransferModule transferModule) throws Exception {
		server = RatpackServer.of(definition ->
				definition
						.serverConfig(configBuilder -> configBuilder
								.port(serverConfig.getPort())
						)
						.handlers(
								chain -> {
									chain
											.get("health", ctx -> ctx.render("UP"));
									transferModule.visit(chain);
								}
						)
		);
	}

	public void start() throws Exception {
		server.start();
	}

	public void stop() throws Exception {
		server.stop();
	}

	public static void main(String[] args) throws Exception {
		final Config config = ConfigFactory.load();
		final var serverConfig = ServerConfig.createFrom(config);
		final DataSource dataSource = new DataSourceFactory().createDataSource(config);
		final var transferModule = new TransferModule(dataSource);
		new TransactionService(serverConfig, transferModule).start();
	}

}
