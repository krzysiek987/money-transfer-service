package pl.dorzak.transferservice.transfer;

import static pl.dorzak.transferservice.transfer.TransferRepository.TransferRowMapper.AMOUNT;
import static pl.dorzak.transferservice.transfer.TransferRepository.TransferRowMapper.CREATED_AT;
import static pl.dorzak.transferservice.transfer.TransferRepository.TransferRowMapper.CURRENCY;
import static pl.dorzak.transferservice.transfer.TransferRepository.TransferRowMapper.DESTINATION_ACCOUNT_ID;
import static pl.dorzak.transferservice.transfer.TransferRepository.TransferRowMapper.ID;
import static pl.dorzak.transferservice.transfer.TransferRepository.TransferRowMapper.SCHEDULED_AT;
import static pl.dorzak.transferservice.transfer.TransferRepository.TransferRowMapper.SOURCE_ACCOUNT_ID;
import static pl.dorzak.transferservice.transfer.TransferRepository.TransferRowMapper.STATUS;
import static pl.dorzak.transferservice.transfer.TransferRepository.TransferRowMapper.TITLE;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

@Slf4j
class TransferRepository {

	private static final String SELECT_TRANSFERS_SQL = "SELECT * FROM transfers WHERE source_account_id = :source_account_id";
	private static final String SELECT_TRANSFER_BY_ID_SQL = "SELECT * FROM transfers WHERE id = :id";
	private static final String UPDATE_TRANSFER_SQL = "UPDATE transfers SET status = :status, amount = :amount, title = :title, scheduled_at = :scheduled_at WHERE id = :id";
	private static final String INSERT_TRANSFER_SQL = "INSERT INTO transfers(status, source_account_id, destination_account_id, amount, currency, title, created_at, scheduled_at) VALUES(:status, :source_account_id, :destination_account_id, :amount, :currency, :title, :created_at, :scheduled_at)";
	private static final String DELETE_TRANSFER_SQL = "DELETE FROM transfers WHERE id = :id";

	private final Jdbi jdbi;

	public TransferRepository(final DataSource dataSource) {
		this.jdbi = Jdbi.create(dataSource);
		this.jdbi.registerRowMapper(Transfer.class, new TransferRowMapper());
	}

	public List<Transfer> findBySourceAccountId(@NonNull final UUID sourceAccountId) {
		return jdbi.withHandle(handle -> handle
				.createQuery(SELECT_TRANSFERS_SQL)
				.bind(SOURCE_ACCOUNT_ID, sourceAccountId)
				.mapTo(Transfer.class).list()
		);
	}

	public Optional<Transfer> findById(final UUID transferId) {
		return jdbi.withHandle(handle -> handle
				.createQuery(SELECT_TRANSFER_BY_ID_SQL)
				.bind(ID, transferId)
				.mapTo(Transfer.class).findOne()
		);
	}

	public Transfer insert(final Transfer transfer) {
		UUID generatedId = jdbi.withHandle(handle -> handle
				.createUpdate(INSERT_TRANSFER_SQL)
				.bind(STATUS, transfer.getStatus())
				.bind(SOURCE_ACCOUNT_ID, transfer.getSourceAccountId())
				.bind(DESTINATION_ACCOUNT_ID, transfer.getDestinationAccountId())
				.bind(AMOUNT, transfer.getAmount())
				.bind(CURRENCY, Objects.toString(transfer.getCurrency()))
				.bind(TITLE, transfer.getTitle())
				.bind(CREATED_AT, transfer.getCreatedAt())
				.bind(SCHEDULED_AT, transfer.getScheduledAt())
				.executeAndReturnGeneratedKeys(ID)
				.mapTo(UUID.class)
				.one()
		);
		return transfer.toBuilder()
				.id(generatedId)
				.build();
	}

	public Transfer update(final Transfer transfer) {
		if (transfer.getId() == null) {
			throw new IllegalArgumentException("Could not invoke update operation for object without id.");
		}
		int updatedRows = jdbi.withHandle(handle -> handle
				.createUpdate(UPDATE_TRANSFER_SQL)
				.bind(ID, transfer.getId())
				.bind(STATUS, transfer.getStatus())
				.bind(AMOUNT, transfer.getAmount())
				.bind(TITLE, transfer.getTitle())
				.bind(SCHEDULED_AT, transfer.getScheduledAt())
				.execute()
		);
		if (updatedRows != 1) {
			throw new TransferRepositoryException("Update", transfer.getId());
		}
		return transfer;
	}

	public void deleteById(final UUID transferId) {
		int deletedRows = jdbi.withHandle(handle -> handle
				.createUpdate(DELETE_TRANSFER_SQL)
				.bind(ID, transferId)
				.execute()
		);
		if (deletedRows != 1) {
			throw new TransferRepositoryException("Delete", transferId);
		}
	}

	static class TransferRowMapper implements RowMapper<Transfer> {

		static final String ID = "id";
		static final String STATUS = "status";
		static final String SOURCE_ACCOUNT_ID = "source_account_id";
		static final String DESTINATION_ACCOUNT_ID = "destination_account_id";
		static final String AMOUNT = "amount";
		static final String CURRENCY = "currency";
		static final String TITLE = "title";
		static final String CREATED_AT = "created_at";
		static final String SCHEDULED_AT = "scheduled_at";

		@Override
		public Transfer map(final ResultSet rs, final StatementContext ctx) throws SQLException {

			return Transfer.builder()
					.id(UUID.fromString(rs.getString(ID)))
					.status(TransferStatus.valueOf(rs.getString(STATUS)))
					.sourceAccountId(UUID.fromString(rs.getString(SOURCE_ACCOUNT_ID)))
					.destinationAccountId(UUID.fromString(rs.getString(DESTINATION_ACCOUNT_ID)))
					.amount(rs.getBigDecimal(AMOUNT))
					.currency(Currency.getInstance(rs.getString(CURRENCY)))
					.title(rs.getString(TITLE))
					.createdAt(getInstant(rs, CREATED_AT))
					.scheduledAt(getInstant(rs, SCHEDULED_AT))
					.build();
		}

		private Instant getInstant(final ResultSet rs, final String scheduledAt) throws SQLException {
			Timestamp timestamp = rs.getTimestamp(scheduledAt);
			return timestamp != null ? timestamp.toInstant() : null;
		}
	}
}
