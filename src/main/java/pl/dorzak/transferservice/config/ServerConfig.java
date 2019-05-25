package pl.dorzak.transferservice.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class representation of http server configuration.
 *
 * Contains all supported configuration options for underlying RatpackServer.
 *
 * @see ratpack.server.ServerConfigBuilder for more details and extension possibilies.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerConfig {

	private static final String SERVER_CONFIG_SECTION = "server";

	private int port;

	public static ServerConfig createFrom(Config config) {
		return ConfigBeanFactory.create(config.getConfig(SERVER_CONFIG_SECTION), ServerConfig.class);
	}

}
