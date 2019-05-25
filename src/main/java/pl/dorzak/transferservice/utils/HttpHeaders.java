package pl.dorzak.transferservice.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Class gathering common HTTP header names and values used by the service.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpHeaders {

	public static final String LOCATION = "Location";

	public static final String CONTENT_TYPE = "Content-Type";
	public static final String CONTENT_JSON = "application/json";

}
