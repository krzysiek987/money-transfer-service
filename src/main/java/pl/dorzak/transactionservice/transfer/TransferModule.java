package pl.dorzak.transactionservice.transfer;

import javax.sql.DataSource;
import org.jdbi.v3.core.Jdbi;

/**
 * Definition of Transfer module.
 * It will create all necessary database tables during initialization.
 */
public class TransferModule {

	private final Jdbi jdbi;

	public TransferModule(final DataSource dataSource) {
		this.jdbi = Jdbi.create(dataSource);
		initializeDatabase();
	}

	private void initializeDatabase() {
		jdbi.useHandle(handle -> handle.execute(
				"CREATE TABLE transfer(id uuid default random_uuid(), created_at TIMESTAMP, primary key (id))"));
	}
}
