package pl.dorzak.transferservice.transfer;

import java.util.Currency;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

@Slf4j
class TransferRepository {

	private final Jdbi jdbi;

	public TransferRepository(final DataSource dataSource) {
		this.jdbi = Jdbi.create(dataSource);
		this.jdbi.registerRowMapper(Transfer.class,
				(rs, ctx) ->
						Transfer.builder()
								.id(UUID.fromString(rs.getString("id")))
								.status(TransferStatus.valueOf(rs.getString("status")))
								.sourceAccountId(UUID.fromString(rs.getString("source_account_id")))
								.destinationAccountId(UUID.fromString(rs.getString("destination_account_id")))
								.amount(rs.getBigDecimal("amount"))
								.currency(Currency.getInstance(rs.getString("currency")))
								.title(rs.getString("title"))
								.createdAt(rs.getTimestamp("created_at").toInstant())
								.updatedAt(rs.getTimestamp("updated_at").toInstant())
								.invokedAt(rs.getTimestamp("invoked_at").toInstant())
								.scheduledAt(rs.getTimestamp("scheduled_at").toInstant())
								.build()
		);
	}

	public List<Transfer> findBySourceAccountId(@NonNull final UUID sourceAccountId) {
		return jdbi.withHandle(handle -> handle
				.createQuery("SELECT * FROM transfers WHERE source_account_id = :sourceAccountId")
				.bind("sourceAccountId", sourceAccountId)
				.mapTo(Transfer.class).list()
		);
	}

	public Transfer findById(final UUID transferId) {
		return null;
	}

	public Transfer save(final Transfer transfer) {
		return null;
	}

	public Transfer deleteById(final UUID transferId) {
		return null;
	}

}
