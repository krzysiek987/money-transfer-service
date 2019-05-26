package pl.dorzak.transferservice.account;

import java.util.Currency;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface AccountDao {

	@SqlUpdate("CREATE TABLE IF NOT EXISTS account (id uuid default random_uuid(), currency varchar(3) not null, primary key (id))")
	void createTable();

	@SqlUpdate("INSERT INTO account(id, currency) VALUES (?, ?)")
	void insertAccount(UUID id, Currency currency);

	@SqlQuery("SELECT * FROM account WHERE id = ?")
	@RegisterBeanMapper(Account.class)
	Optional<Account> findById(UUID id);

	@SqlUpdate("DELETE FROM account WHERE id = ?")
	void deleteById(UUID id);

}
