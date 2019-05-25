package pl.dorzak.transferservice;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.util.Collection;
import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import pl.dorzak.transferservice.config.DataSourceFactory;
import pl.dorzak.transferservice.config.ServerConfig;
import pl.dorzak.transferservice.monitoring.MonitoringAPI;
import pl.dorzak.transferservice.transfer.TransferAPI;
import ratpack.server.RatpackServer;
import ratpack.server.StartupFailureException;

/**
 * Initializes APIs and all necessary components like HTTP server.
 * This is main entry point to the microservice.
 */
@Slf4j
public class TransferService {

	private final RatpackServer server;

	public TransferService(final ServerConfig serverConfig, Collection<RatpackChainVisitor> apis) {
		try {
			server = RatpackServer.of(definition -> definition
					.serverConfig(configBuilder -> configBuilder
							.port(serverConfig.getPort())
					)
					.handlers(chain -> apis.forEach(api -> api.registerIn(chain)))
			);
		} catch (Exception e) {
			log.error("Unable to configure underlying HTTP server", e);
			throw new StartupFailureException(e.getMessage());
		}
	}

	public void start() {
		try {
			server.start();
		} catch (Exception e) {
			log.error("TransferService was unable to start", e);
			throw new StartupFailureException(e.getMessage());
		}
	}

	public void stop() {
		try {
			server.stop();
		} catch (Exception e) {
			log.error("Could not stop TransferService", e);
		}
	}

	public static void main(String[] args) {
		final Config config = ConfigFactory.load();
		final var serverConfig = ServerConfig.createFrom(config);
		final DataSource dataSource = new DataSourceFactory().createDataSource(config);

		final var transferAPI = new TransferAPI(dataSource);

		final var transferService = new TransferService(serverConfig, List.of(new MonitoringAPI(), transferAPI));
		transferService.start();
		Runtime.getRuntime().addShutdownHook(new Thread(transferService::stop));
	}
}
