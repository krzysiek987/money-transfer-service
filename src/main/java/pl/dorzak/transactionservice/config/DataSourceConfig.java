package pl.dorzak.transactionservice.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class representation of data source config.
 *
 * Contains all supported configuration options for DataSource.
 *
 * @see com.zaxxer.hikari.HikariConfig for more details and extension possibilies.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class DataSourceConfig {

	private static final String DATA_SOURCE_CONFIG_SECTION = "dataSource";

	private String jdbcUrl;
	private String username;
	private String password;

	static DataSourceConfig createFrom(Config config) {
		return ConfigBeanFactory.create(config.getConfig(DATA_SOURCE_CONFIG_SECTION), DataSourceConfig.class);
	}
}
