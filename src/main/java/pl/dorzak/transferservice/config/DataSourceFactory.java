package pl.dorzak.transferservice.config;

import com.typesafe.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;
import java.util.Collection;
import javax.sql.DataSource;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
		HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
		runLiquibase(hikariDataSource, dataSourceConfig.getInitContexts());
		return hikariDataSource;
	}

	/**
	 * Runs liquibase which creates tables and fills database with some test data.
	 */
	private static void runLiquibase(final DataSource dataSource, final Collection<String> initContexts) {
		try {
			var liquibase = new Liquibase("dbChangelog.yaml", new ClassLoaderResourceAccessor(),
					new JdbcConnection(dataSource.getConnection()));
			liquibase.update(new Contexts(initContexts));
		} catch (SQLException | LiquibaseException e) {
			log.error("Could not initialize database. Will skip and hope that it was initialized before.", e);
		}
	}

}
