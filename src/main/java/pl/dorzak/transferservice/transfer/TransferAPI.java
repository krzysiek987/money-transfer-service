package pl.dorzak.transferservice.transfer;

import javax.sql.DataSource;

/**
 * Definition of Transfer module.
 * It will create all necessary database tables during initialization.
 */
public class TransferAPI {

	public TransferAPI(DataSource dataSource) {
		new TransferRepository(dataSource);
	}

}
