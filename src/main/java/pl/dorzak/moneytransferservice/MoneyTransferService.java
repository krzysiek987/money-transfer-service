package pl.dorzak.moneytransferservice;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import ratpack.server.RatpackServer;

public class MoneyTransferService {

	private final RatpackServer server;

	public MoneyTransferService(final ServerConfig serverConfig) throws Exception {
		server = RatpackServer.of(definition -> definition
				.serverConfig(configBuilder -> configBuilder
						.port(serverConfig.getPort())
				)
				.handlers(chain -> chain
						.get(ctx -> ctx.render("Hello World!"))
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
		Config config = ConfigFactory.load();
		ServerConfig serverConfig = ServerConfig.createFrom(config);

		new MoneyTransferService(serverConfig).start();
	}
}
