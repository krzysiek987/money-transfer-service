package pl.dorzak.transactionservice.transfer;

import javax.sql.DataSource;
import org.jdbi.v3.core.Jdbi;
import pl.dorzak.transactionservice.utils.HttpStatus;
import ratpack.handling.Chain;

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

	public void visit(final Chain chain) {
		chain
				.path("1.0/transfers", ctx ->
						ctx.byMethod(method ->
								method
										.get(() -> ctx.getResponse().send())
										.post(() -> ctx.getResponse().status(HttpStatus.CREATED).send())
						)
				)
				.path("1.0/transfers/:transferId", ctx ->
						ctx.byMethod(method ->
								method
										.get(() -> ctx.getResponse().send())
										.put(() -> ctx.getResponse().send())
										.patch(() -> ctx.getResponse().send())
										.delete(() -> ctx.getResponse().status(HttpStatus.NO_CONTENT).send())
						)
				);
	}
}
