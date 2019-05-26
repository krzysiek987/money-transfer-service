package pl.dorzak.transferservice.transfer.validation;

import java.util.Map;
import lombok.Getter;

/**
 * Validation result produced by {@link TransferValidator}
 */
@Getter
public class ValidationResult {

	private final boolean valid;
	private final Map<String, ValidationErrorCode> errorReport;

	ValidationResult(final Map<String, ValidationErrorCode> validationErrors) {
		this.valid = validationErrors.isEmpty();
		this.errorReport = validationErrors;
	}
}
