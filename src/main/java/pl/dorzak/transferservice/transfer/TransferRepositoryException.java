package pl.dorzak.transferservice.transfer;

import java.util.UUID;

class TransferRepositoryException extends RuntimeException {

	TransferRepositoryException(final String operation, final UUID transferId) {
		super(operation + " failed for Transfer with id " + transferId);
	}
}
