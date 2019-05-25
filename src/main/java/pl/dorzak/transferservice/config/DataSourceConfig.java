package pl.dorzak.transferservice.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

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
public class DataSourceConfig {

	private static final String DATA_SOURCE_CONFIG_SECTION = "dataSource";

	private String jdbcUrl;

	private String username;

	private String password;
	@Singular
	private List<String> initContexts;

	public static DataSourceConfig createFrom(Config config) {
		return ConfigBeanFactory.create(config.getConfig(DATA_SOURCE_CONFIG_SECTION), DataSourceConfig.class);
	}
}
