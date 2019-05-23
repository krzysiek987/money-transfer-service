package pl.dorzak.moneytransferservice;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import ratpack.server.RatpackServer;

public class MoneyTransferServiceStartup {

	public static void main(String... args) throws Exception {
		Config config = ConfigFactory.load();
		config.getString("dummy");

		RatpackServer.start(server -> server
				.handlers(chain -> chain
						.get(ctx -> ctx.render("Hello World!"))
				)
		);
	}

}
