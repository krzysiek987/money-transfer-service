package pl.dorzak.transferservice.transfer;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import pl.dorzak.transferservice.transfer.model.Transfer;
import pl.dorzak.transferservice.transfer.model.TransferStatus;

public interface TransferAPI {

	/**
	 * @param accountId id of source account id that will be applied as filter
	 * @return all transfers matching passed id
	 */
	Collection<Transfer> getBySourceAccountId(UUID accountId);

	/**
	 * @return all Transfers that should be invoked according to scheduledAt
	 */
	List<Transfer> getAllScheduledToInvoke();

	/**
	 * @param id id of Transfer to be returned
	 * @return {@link TransferApiResponse} with found transfer or resultCode indicating error
	 */
	TransferApiResponse findById(UUID id);

	/**
	 * @param transfer Transfer insert request
	 * @return {@link TransferApiResponse} with found transfer or resultCode indicating error
	 */
	TransferApiResponse insert(Transfer transfer);

	/**
	 * @param transfer Transfer update request
	 * @return {@link TransferApiResponse} with found transfer or resultCode indicating error
	 */
	TransferApiResponse update(Transfer transfer);

	/**
	 * Invokes Transfer by charging accounts and setting status
	 */
	TransferApiResponse invoke(Transfer transfer);

	/**
	 * @param id id of transfer to change status
	 * @param status new status
	 */
	TransferApiResponse setStatus(UUID id, TransferStatus status);

	/**
	 * @param id id of Transfer to be deleted
	 */
	TransferApiResponse deleteById(UUID id);
}
