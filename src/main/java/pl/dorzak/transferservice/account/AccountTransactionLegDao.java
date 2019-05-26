package pl.dorzak.transferservice.account;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface AccountTransactionLegDao {

	@SqlUpdate("CREATE TABLE IF NOT EXISTS account_transaction_leg (id uuid default random_uuid(), account_id uuid not null, currency varchar(3) not null, value decimal not null, PRIMARY KEY (id), FOREIGN KEY (account_id) REFERENCES account(id))")
	void createTable();

	@SqlUpdate("INSERT INTO account_transaction_leg(account_id, currency, value) VALUES (?, ?, ?)")
	@GetGeneratedKeys({"id"})
	UUID insertAccountTransactionLeg(UUID accountId, Currency currency, BigDecimal value);

	@SqlQuery("SELECT * FROM account_transaction_leg WHERE account_id = ?")
	@RegisterBeanMapper(AccountTransactionLeg.class)
	List<AccountTransactionLeg> findByAccountId(UUID accountId);

	@SqlQuery("SELECT * FROM account_transaction_leg WHERE id = ?")
	@RegisterBeanMapper(AccountTransactionLeg.class)
	Optional<AccountTransactionLeg> findById(UUID id);

	@SqlUpdate("DELETE FROM account_transaction_leg WHERE id = ?")
	void deleteById(UUID id);

}
