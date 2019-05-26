package pl.dorzak.transferservice.config;

import com.typesafe.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Currency;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import pl.dorzak.transferservice.account.AccountDao;
import pl.dorzak.transferservice.account.AccountTransactionLegDao;
import pl.dorzak.transferservice.transfer.TransferDao;

@Slf4j
public class JdbiFactory {

	private static final AtomicBoolean initialized = new AtomicBoolean(false);

	public Jdbi create(final Config config) {
		Jdbi jdbi = Jdbi.create(createDataSource(config));
		jdbi.installPlugin(new SqlObjectPlugin());
		jdbi.registerColumnMapper(new CurrencyMapper());
		jdbi.registerArgument(new CurrencyArgumentFactory());
		if (initialized.compareAndSet(false, true)) {
			createTablesAndTestData(jdbi);
		}
		return jdbi;
	}

	private void createTablesAndTestData(final Jdbi jdbi) {
		jdbi.useExtension(TransferDao.class, TransferDao::createTable);
		jdbi.useExtension(AccountDao.class, dao -> {
			dao.createTable();
			dao.insertAccount(UUID.fromString("d6896820-7f3e-11e9-b475-0800200c9a66"), Currency.getInstance("PLN"));
			dao.insertAccount(UUID.fromString("f18e5e50-7f3e-11e9-b475-0800200c9a66"), Currency.getInstance("PLN"));
			dao.insertAccount(UUID.fromString("faab9700-7f3e-11e9-b475-0800200c9a66"), Currency.getInstance("GBP"));
		});
		jdbi.useExtension(AccountTransactionLegDao.class, AccountTransactionLegDao::createTable);
	}

	private DataSource createDataSource(final Config config) {
		DataSourceConfig dataSourceConfig = DataSourceConfig.createFrom(config);
		final var hikariConfig = new HikariConfig();
		hikariConfig.setJdbcUrl(dataSourceConfig.getJdbcUrl());
		hikariConfig.setUsername(dataSourceConfig.getUsername());
		hikariConfig.setPassword(dataSourceConfig.getPassword());
		return new HikariDataSource(hikariConfig);
	}

	private class CurrencyMapper implements ColumnMapper<Currency> {

		public Currency map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
			return Currency.getInstance(r.getString(columnNumber));
		}
	}

	private class CurrencyArgumentFactory extends AbstractArgumentFactory<Currency> {

		CurrencyArgumentFactory() {
			super(Types.VARCHAR);
		}

		@Override
		protected Argument build(final Currency value, final ConfigRegistry config) {
			return (position, statement, ctx) -> statement.setString(position, value.toString());
		}
	}

}
