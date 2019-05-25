package pl.dorzak.transferservice;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.util.UUID;
import javax.sql.DataSource;
import pl.dorzak.transferservice.config.DataSourceFactory;
import pl.dorzak.transferservice.config.ServerConfig;
import pl.dorzak.transferservice.transfer.TransferAPI;
import pl.dorzak.transferservice.utils.HttpStatus;
import ratpack.server.RatpackServer;

/**
 * Initializes APIs and all necessary components like HTTP server.
 * This is main entry point to the microservice.
 */
public class TransferService {

	private final RatpackServer server;

	public TransferService(final ServerConfig serverConfig, TransferAPI transferAPI) throws Exception {
		server = RatpackServer.of(definition ->
				definition
						.serverConfig(configBuilder -> configBuilder
								.port(serverConfig.getPort())
						)
						.handlers(
								chain -> {
									chain
											.get("health", ctx -> ctx.render("UP"))
											.path("1.0/transfers", ctx ->
													ctx.byMethod(method ->
															method
																	.get(() -> ctx.getResponse().send())
																	.post(() -> ctx.getResponse()
																			.status(HttpStatus.CREATED).send())
													)
											)
											.path("1.0/transfers/:transferId", ctx ->
													ctx.byMethod(method ->
															method
																	.get(() -> {
																		var transferId = UUID.fromString(
																				ctx.getPathTokens().get("transferId"));
																		ctx.getResponse().send();
																	})
																	.put(() -> ctx.getResponse().send())
																	.patch(() -> ctx.getResponse().send())
																	.delete(() -> ctx.getResponse()
																			.status(HttpStatus.NO_CONTENT).send())
													)
											);
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
		final var transferAPI = new TransferAPI(dataSource);
		new TransferService(serverConfig, transferAPI).start();
	}

}
