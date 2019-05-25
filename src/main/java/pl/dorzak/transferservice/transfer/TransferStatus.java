package pl.dorzak.transferservice.transfer;

public enum TransferStatus {

	/**
	 * Is used when transfer has scheduledAt filled. It means that transfer will be invoked later.
	 */
	SCHEDULED,
	/**
	 * Transfer is currently being processed. It cannot be changed anymore.
	 */
	PENDING,
	/**
	 * Transfer was completed with success.
	 */
	FINISHED,
	/**
	 * Transfer did fail for some reason eg. insufficient funds on the account
	 */
	ERROR

}
