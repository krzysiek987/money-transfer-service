package pl.dorzak.transferservice.transfer.model;

import java.util.EnumSet;

public enum TransferStatus {

	/**
	 * Transfer was completed with success.
	 */
	SUCCESS,
	/**
	 * Transfer did fail after it was invoked according to schedule.
	 */
	ERROR,
	/**
	 * Is used when transfer has scheduledAt filled. It means that transfer will be invoked later.
	 */
	SCHEDULED;

	private static final EnumSet<TransferStatus> ALLOWED_TO_DELETE = EnumSet.of(SCHEDULED, ERROR);

	public boolean allowsToDelete() {
		return ALLOWED_TO_DELETE.contains(this);
	}
}
