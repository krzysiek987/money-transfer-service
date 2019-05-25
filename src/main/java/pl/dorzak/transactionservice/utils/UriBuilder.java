package pl.dorzak.transactionservice.utils;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import lombok.NonNull;

/**
 * Builds java.net.URI from provided mandatory scheme, host and optional port, path segments query params.
 * It is simplified implementation of URI building and does not support more sophisticated features like
 * passing authorization and query params multi values. Those features were not necessary for scope of this service.
 */
public class UriBuilder {

	private static final String SCHEME_SEPARATOR = "://";
	private static final String PORT_SEPARATOR = ":";
	private static final String PATH_SEPARATOR = "/";
	private static final String QUERY_PARAMS_PREFIX = "?";
	private static final String QUERY_PARAMS_DELIMITER = "&";
	private static final String EQUAL = "=";

	private final String scheme;
	private final String host;
	private final Integer port;
	private final List<String> pathSegments = new ArrayList<>();
	private final Map<String, Object> queryParams = new HashMap<>();

	public UriBuilder(@NonNull final String scheme, @NonNull final String host, final Integer port) {
		this.scheme = scheme;
		this.host = host;
		this.port = port;
	}

	public UriBuilder pathSegment(Object path) {
		this.pathSegments.add(Objects.toString(path));
		return this;
	}

	public UriBuilder queryParam(String name, Object value) {
		this.queryParams.put(name, value);
		return this;
	}

	public URI build() {
		final var uriStringBuilder = new StringBuilder();
		uriStringBuilder.append(scheme);
		uriStringBuilder.append(SCHEME_SEPARATOR);
		uriStringBuilder.append(host);
		if (port != null) {
			uriStringBuilder.append(PORT_SEPARATOR);
			uriStringBuilder.append(port);
		}
		pathSegments.forEach(pathSegment -> {
			uriStringBuilder.append(PATH_SEPARATOR);
			uriStringBuilder.append(pathSegment);
		});
		if (!queryParams.isEmpty()) {
			final var queryParamsJoiner = new StringJoiner(QUERY_PARAMS_DELIMITER, QUERY_PARAMS_PREFIX, "");
			queryParams.forEach((name, value) -> queryParamsJoiner.add(name + EQUAL + value));
			uriStringBuilder.append(queryParamsJoiner.toString());
		}

		return URI.create(uriStringBuilder.toString());
	}
}
