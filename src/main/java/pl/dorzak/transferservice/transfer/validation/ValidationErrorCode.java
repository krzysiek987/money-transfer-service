package pl.dorzak.transferservice.transfer.validation;

/**
 * Error codes used by {@link TransferValidator}
 */
enum ValidationErrorCode {

	NOT_FOUND,
	CURRENCY_DOES_NOT_MATCH_ACCOUNT,
	NOT_SUPPORTED,
	EXPECTED_NOT_NULL,
	EXPECTED_FUTURE_INSTANT,
	EXPECTED_POSITIVE_VALUE

}
