package pl.dorzak.transferservice.transfer;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import pl.dorzak.transferservice.transfer.model.Transfer;
import pl.dorzak.transferservice.transfer.model.TransferStatus;

public interface TransferDao {

	@SqlUpdate("CREATE TABLE IF NOT EXISTS transfer(id uuid default random_uuid(), status text not null, source_account_id uuid not null, destination_account_id uuid not null, value decimal not null, currency varchar(3) not null, title text, created_at timestamp not null default current_timestamp(), scheduled_at timestamp with time zone, primary key (id))")
	void createTable();

	@SqlQuery("SELECT * FROM transfer WHERE source_account_id = ?")
	@RegisterBeanMapper(Transfer.class)
	List<Transfer> findTransfersBySourceAccountId(UUID sourceAccountId);

	@SqlQuery("SELECT * FROM transfer WHERE id = ?")
	@RegisterBeanMapper(Transfer.class)
	Optional<Transfer> findById(UUID id);

	@SqlUpdate("INSERT INTO transfer(status, source_account_id, destination_account_id, value, currency, title, created_at, scheduled_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")
	@GetGeneratedKeys({"id"})
	UUID insertTransfer(TransferStatus status, UUID sourceAccountId, UUID destinationAccountId, BigDecimal value,
			Currency currency, String title, Instant createdAt, ZonedDateTime scheduledAt);

	@SqlUpdate("INSERT INTO transfer(id, status, source_account_id, destination_account_id, value, currency, title, created_at, scheduled_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
	void insertTransferWithId(UUID id, TransferStatus status, UUID sourceAccountId, UUID destinationAccountId,
			BigDecimal value, Currency currency, String title, Instant createdAt, ZonedDateTime scheduledAt);

	@SqlUpdate("UPDATE transfer SET title= ?, value= ?, scheduled_at= ? WHERE id = ?")
	void updateTransfer(String title, BigDecimal value, ZonedDateTime scheduledAt, UUID id);

	@SqlUpdate("DELETE FROM transfer WHERE id = ?")
	void deleteById(UUID id);

	@SqlQuery("SELECT * FROM transfer WHERE status = 'SCHEDULED' AND scheduled_at <= ?")
	@RegisterBeanMapper(Transfer.class)
	List<Transfer> findScheduledUpTo(ZonedDateTime scheduledUpTo);

	@SqlUpdate("UPDATE transfer SET status= ? WHERE id = ?")
	void setStatus(TransferStatus status, UUID id);
}
