package pl.dorzak.moneytransferservice;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
class ServerConfig {

	private int port;

	static ServerConfig createFrom(Config config) {
		return ConfigBeanFactory.create(config, ServerConfig.class);
	}

}
