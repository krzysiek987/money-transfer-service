package pl.dorzak.transferservice.transfer.validation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import pl.dorzak.transferservice.account.Account;
import pl.dorzak.transferservice.transfer.TransferAPIImpl.TransferProcessingContext;
import pl.dorzak.transferservice.transfer.model.Transfer;
import pl.dorzak.transferservice.transfer.model.TransferStatus;

/**
 * Performs validations for Transaction insert and update operations.
 */
public class TransferValidator {

	private static final String STATUS = "status";
	private static final String SOURCE_ACCOUNT_ID = "sourceAccountId";
	private static final String DESTINATION_ACCOUNT_ID = "destinationAccountId";
	private static final String VALUE = "value";
	private static final String CURRENCY = "currency";
	private static final String SCHEDULED_AT = "scheduledAt";

	public ValidationResult validateInsert(final TransferProcessingContext context, final Transfer transfer) {
		Map<String, ValidationErrorCode> validationErrors = new HashMap<>();
		validationErrors.putAll(validateCommonFields(transfer));

		Optional<Account> sourceAccount = context.getAccountDao().findById(transfer.getSourceAccountId());
		if (!sourceAccount.isPresent()) {
			validationErrors.put(SOURCE_ACCOUNT_ID, ValidationErrorCode.NOT_FOUND);
		} else {
			if (!Objects.equals(sourceAccount.get().getCurrency(), transfer.getCurrency())) {
				validationErrors.put(CURRENCY, ValidationErrorCode.CURRENCY_DOES_NOT_MATCH_ACCOUNT);
			}
		}

		Optional<Account> destinationAccount = context.getAccountDao().findById(transfer.getDestinationAccountId());
		if (!destinationAccount.isPresent()) {
			validationErrors.put(DESTINATION_ACCOUNT_ID, ValidationErrorCode.NOT_FOUND);
		} else {
			if (!Objects.equals(destinationAccount.get().getCurrency(), transfer.getCurrency())) {
				validationErrors.put(CURRENCY, ValidationErrorCode.CURRENCY_DOES_NOT_MATCH_ACCOUNT);
			}
		}

		if (!EnumSet.of(TransferStatus.SCHEDULED, TransferStatus.SUCCESS).contains(transfer.getStatus())) {
			validationErrors.put(STATUS, ValidationErrorCode.NOT_SUPPORTED);
		}

		return new ValidationResult(validationErrors);
	}

	public ValidationResult validateUpdate(final Transfer transfer) {
		Map<String, ValidationErrorCode> validationErrors = new HashMap<>();
		validationErrors.putAll(validateCommonFields(transfer));

		if (transfer.getStatus() != TransferStatus.SCHEDULED) {
			validationErrors.put("status", ValidationErrorCode.NOT_SUPPORTED);
		}

		return new ValidationResult(validationErrors);
	}

	/**
	 * Validates fields releavant for both insert and update
	 */
	private Map<String, ValidationErrorCode> validateCommonFields(final Transfer transfer) {
		Map<String, ValidationErrorCode> validationErrors = new HashMap<>();

		validationErrors.putAll(validateNotNull(transfer.getStatus(), STATUS));
		validationErrors.putAll(validateNotNull(transfer.getSourceAccountId(), SOURCE_ACCOUNT_ID));
		validationErrors.putAll(validateNotNull(transfer.getDestinationAccountId(), DESTINATION_ACCOUNT_ID));
		validationErrors.putAll(validateNotNull(transfer.getValue(), VALUE));
		validationErrors.putAll(validateNotNull(transfer.getCurrency(), CURRENCY));
		if (isLessOrEqualZero(transfer.getValue())) {
			validationErrors.put(VALUE, ValidationErrorCode.EXPECTED_POSITIVE_VALUE);
		}
		if (transfer.getStatus() == TransferStatus.SCHEDULED && transfer.getScheduledAt() == null) {
			validationErrors.put(SCHEDULED_AT, ValidationErrorCode.EXPECTED_NOT_NULL);
		}
		if (transfer.getScheduledAt() != null && Instant.now().isAfter(transfer.getScheduledAt().toInstant())) {
			validationErrors.put(SCHEDULED_AT, ValidationErrorCode.EXPECTED_FUTURE_INSTANT);
		}

		return validationErrors;
	}

	private Map<String, ValidationErrorCode> validateNotNull(final Object field, final String property) {
		final Map<String, ValidationErrorCode> validationErrors = new HashMap<>();
		if (field == null) {
			validationErrors.put(property, ValidationErrorCode.EXPECTED_NOT_NULL);
		}
		return validationErrors;
	}

	private boolean isLessOrEqualZero(final BigDecimal value) {
		return value != null && value.compareTo(BigDecimal.ZERO) <= 0;
	}
}
