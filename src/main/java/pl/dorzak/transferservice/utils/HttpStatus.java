package pl.dorzak.transferservice.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Class gathering common HTTP statuses used by the service.
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpStatus {

	//2xx codes
	public static final int OK = 200;
	public static final int CREATED = 201;
	public static final int NO_CONTENT = 204;

	//4xx codes
	public static final int BAD_REQUEST = 400;
	public static final int FORBIDDEN = 403;
}
