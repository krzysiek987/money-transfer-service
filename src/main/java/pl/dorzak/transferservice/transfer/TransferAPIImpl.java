package pl.dorzak.transferservice.transfer;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import pl.dorzak.transferservice.account.AccountDao;
import pl.dorzak.transferservice.account.AccountTransactionLegDao;
import pl.dorzak.transferservice.transfer.model.Transfer;
import pl.dorzak.transferservice.transfer.model.TransferStatus;
import pl.dorzak.transferservice.transfer.validation.TransferValidator;
import pl.dorzak.transferservice.transfer.validation.ValidationResult;

@Slf4j
public class TransferAPIImpl implements TransferAPI {

	private final Jdbi jdbi;
	private final TransferValidator validator;

	public TransferAPIImpl(Jdbi jdbi) {
		this.jdbi = jdbi;
		this.validator = new TransferValidator();
	}

	@Override
	public Collection<Transfer> getBySourceAccountId(final UUID accountId) {
		return jdbi.withExtension(TransferDao.class, dao -> dao.findTransfersBySourceAccountId(accountId));
	}

	@Override
	public List<Transfer> getAllScheduledToInvoke() {
		return jdbi.withExtension(TransferDao.class, dao -> dao.findScheduledUpTo(ZonedDateTime.now()));
	}

	@Override
	public TransferApiResponse findById(final UUID id) {
		return jdbi.withExtension(TransferDao.class, dao -> dao.findById(id)
				.map(TransferApiResponse::ok)
				.orElse(TransferApiResponse.notFound()));
	}

	@Override
	public TransferApiResponse insert(final Transfer transfer) {
		return jdbi.inTransaction(handle -> {
			final var context = new TransferProcessingContext(handle);
			ValidationResult validationResult = validator.validateInsert(context, transfer);
			if (!validationResult.isValid()) {
				return TransferApiResponse.validationFailed(validationResult.getErrorReport());
			}

			boolean isScheduled = transfer.getScheduledAt() != null;
			TransferStatus status = isScheduled ? TransferStatus.SCHEDULED : TransferStatus.SUCCESS;
			Instant createdAt = Instant.now();
			UUID transferId = context.getTransferDao()
					.insertTransfer(status,
							transfer.getSourceAccountId(),
							transfer.getDestinationAccountId(), transfer.getValue(), transfer.getCurrency(),
							transfer.getTitle(), createdAt, transfer.getScheduledAt());
			if (!isScheduled) {
				chargeAccounts(transfer, context);
			}

			Transfer result = transfer.toBuilder().id(transferId).status(status).createdAt(createdAt).build();
			return TransferApiResponse.ok(result);
		});
	}

	@Override
	public TransferApiResponse update(final Transfer updateRequest) {
		return jdbi.inTransaction(handle -> {
			final var context = new TransferProcessingContext(handle);
			Optional<Transfer> existingTransfer = context.getTransferDao().findById(updateRequest.getId());
			final var result = existingTransfer.get().toBuilder()
					.title(updateRequest.getTitle())
					.value(updateRequest.getValue())
					.scheduledAt(updateRequest.getScheduledAt())
					.build();

			ValidationResult validationResult = validator.validateUpdate(result);
			if (!validationResult.isValid()) {
				return TransferApiResponse.validationFailed(validationResult.getErrorReport());
			}

			context.getTransferDao()
					.updateTransfer(result.getTitle(), result.getValue(), result.getScheduledAt(), result.getId());
			return TransferApiResponse.ok(result);
		});
	}

	@Override
	public TransferApiResponse invoke(final Transfer transfer) {
		return jdbi.inTransaction(handle -> {
			final var context = new TransferProcessingContext(handle);
			chargeAccounts(transfer, context);
			context.getTransferDao().setStatus(TransferStatus.SUCCESS, transfer.getId());
			return context.getTransferDao().findById(transfer.getId());
		}).map(TransferApiResponse::ok)
				.orElse(TransferApiResponse.notFound());
	}

	@Override
	public TransferApiResponse setStatus(final UUID id, final TransferStatus status) {
		return jdbi.withExtension(TransferDao.class, dao -> {
			dao.setStatus(status, id);
			return dao.findById(id);
		}).map(TransferApiResponse::ok)
				.orElse(TransferApiResponse.notFound());
	}

	@Override
	public TransferApiResponse deleteById(final UUID id) {
		return jdbi.withExtension(TransferDao.class, dao -> {
			Optional<TransferStatus> transferStatus = dao.findById(id)
					.map(Transfer::getStatus);
			if (transferStatus.isEmpty()) {
				return TransferApiResponse.notFound();
			}
			TransferStatus status = transferStatus.get();
			if (!status.allowsToDelete()) {
				return TransferApiResponse.notSupported();
			}
			dao.deleteById(id);
			return TransferApiResponse.ok(null);
		});
	}

	private void chargeAccounts(final Transfer transfer, final TransferProcessingContext context) {
		context.getAccountTransactionLegDao()
				.insertAccountTransactionLeg(transfer.getSourceAccountId(), transfer.getCurrency(),
						transfer.getValue().negate());
		context.getAccountTransactionLegDao()
				.insertAccountTransactionLeg(transfer.getDestinationAccountId(), transfer.getCurrency(),
						transfer.getValue());
	}

	@Value
	@Getter
	public class TransferProcessingContext {

		private TransferDao transferDao;
		private AccountDao accountDao;
		private AccountTransactionLegDao accountTransactionLegDao;

		TransferProcessingContext(final Handle handle) {
			transferDao = handle.attach(TransferDao.class);
			accountDao = handle.attach(AccountDao.class);
			accountTransactionLegDao = handle.attach(AccountTransactionLegDao.class);
		}
	}
}
