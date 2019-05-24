package pl.dorzak.transactionservice.config;

import com.typesafe.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

public class DataSourceFactory {

	/**
	 * Creates default HikariDataSource based on provided config.
	 *
	 * @param config application config. Method will extract DataSourceConfig from it.
	 * @return initialized DataSource
	 */
	public DataSource createDataSource(final Config config) {
		DataSourceConfig dataSourceConfig = DataSourceConfig.createFrom(config);
		final var hikariConfig = new HikariConfig();
		hikariConfig.setJdbcUrl(dataSourceConfig.getJdbcUrl());
		hikariConfig.setUsername(dataSourceConfig.getUsername());
		hikariConfig.setPassword(dataSourceConfig.getPassword());
		return new HikariDataSource(hikariConfig);
	}

}
