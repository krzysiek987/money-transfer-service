package pl.dorzak.transferservice.transfer;

import lombok.Getter;
import lombok.Value;
import org.apache.groovy.util.Maps;
import pl.dorzak.transferservice.transfer.model.Transfer;
import pl.dorzak.transferservice.utils.HttpStatus;

@Value
public class TransferApiResponse {

	private ResultCode result;
	private Transfer transfer;
	private Object errorResponse;

	static TransferApiResponse ok(Transfer transfer) {
		return new TransferApiResponse(ResultCode.OK, transfer, null);
	}

	static TransferApiResponse validationFailed(final Object errorReport) {
		return new TransferApiResponse(ResultCode.VALIDATION_FAILED, null, Maps.of("error", errorReport));
	}

	static TransferApiResponse notFound() {
		return new TransferApiResponse(ResultCode.NOT_FOUND, null, null);
	}

	static TransferApiResponse notSupported() {
		return new TransferApiResponse(ResultCode.NOT_SUPPORTED, null, null);
	}

	public enum ResultCode {
		OK(HttpStatus.OK),
		VALIDATION_FAILED(HttpStatus.BAD_REQUEST),
		NOT_SUPPORTED(HttpStatus.FORBIDDEN),
		NOT_FOUND(HttpStatus.NOT_FOUND);

		@Getter
		private int httpStatus;

		/**
		 * @param httpStatus advice for clients about which http should probably be returned
		 */
		ResultCode(final int httpStatus) {
			this.httpStatus = httpStatus;
		}

		public boolean isOk() {
			return this == OK;
		}
	}

}
