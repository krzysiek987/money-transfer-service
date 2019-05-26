package pl.dorzak.transferservice;

import static ratpack.jackson.Jackson.fromJson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import pl.dorzak.transferservice.config.JdbiFactory;
import pl.dorzak.transferservice.config.ServerConfig;
import pl.dorzak.transferservice.transfer.ScheduledTransfersProcessor;
import pl.dorzak.transferservice.transfer.TransferAPIImpl;
import pl.dorzak.transferservice.transfer.TransferApiResponse;
import pl.dorzak.transferservice.transfer.TransferApiResponse.ResultCode;
import pl.dorzak.transferservice.transfer.model.Transfer;
import pl.dorzak.transferservice.utils.HttpHeaders;
import pl.dorzak.transferservice.utils.HttpStatus;
import pl.dorzak.transferservice.utils.UriBuilder;
import ratpack.handling.Context;
import ratpack.jackson.Jackson;
import ratpack.server.RatpackServer;

/**
 * Initializes APIs and all necessary components like HTTP server.
 * This is main entry point to the microservice.
 */
@Slf4j
public class TransferService {

	private final RatpackServer server;
	private final ScheduledTransfersProcessor scheduledTransfersProcessor;

	public static void main(String[] args) throws Exception {
		final Config config = ConfigFactory.load();
		final var serverConfig = ServerConfig.createFrom(config);
		final Jdbi jdbi = new JdbiFactory().create(config);

		final var transferAPI = new TransferAPIImpl(jdbi);

		var scheduledTransfersProcessor = new ScheduledTransfersProcessor(transferAPI);

		new TransferService(serverConfig, transferAPI, scheduledTransfersProcessor);
	}

	public TransferService(final ServerConfig serverConfig, final TransferAPIImpl transferAPI,
			final ScheduledTransfersProcessor scheduledTransfersProcessor) throws Exception {
		this.scheduledTransfersProcessor = scheduledTransfersProcessor;
		this.server = RatpackServer.start(definition -> definition
				.serverConfig(configBuilder -> configBuilder
						.port(serverConfig.getPort())
				)
				.registryOf(r -> r
						.add(ObjectMapper.class, new ObjectMapper().registerModule(new JavaTimeModule()))
				)
				.handlers(chain -> chain
						.get("health", ctx -> ctx.render("UP"))
						.path("1.0/transfers", ctx -> ctx
								.byMethod(method -> method
										.get(() -> {
											String accountIdParam = ctx.getRequest().getQueryParams()
													.get("sourceAccountId");
											UUID accountId = UUID.fromString(accountIdParam);
											ctx.render(Jackson.json(transferAPI.getBySourceAccountId(accountId)));
										})
										.post(() -> {
											ctx.parse(fromJson(Transfer.class))
													.map(transferAPI::insert)
													.then(transferApiResponse -> {
														addLocationHeader(ctx, transferApiResponse, serverConfig);
														sendTransferApiResponse(ctx, transferApiResponse,
																HttpStatus.CREATED);
													});
										})
								)
						)
						.path("1.0/transfers/:transferId", ctx -> ctx
								.byMethod(method -> method
										.get(() -> {
											var transferId = UUID.fromString(ctx.getPathTokens().get("transferId"));
											TransferApiResponse transferApiResponse = transferAPI.findById(transferId);
											sendTransferApiResponse(ctx, transferApiResponse, HttpStatus.OK);
										})
										.put(() -> {
											ctx.parse(fromJson(Transfer.class))
													.map(transfer -> {
														transfer.setId(
																UUID.fromString(ctx.getPathTokens().get("transferId")));
														return transferAPI.update(transfer);
													})
													.then(transferApiResponse -> sendTransferApiResponse(ctx,
															transferApiResponse, HttpStatus.OK));
										})
										.delete(() -> {
											var transferId = UUID.fromString(ctx.getPathTokens().get("transferId"));
											TransferApiResponse apiResponse = transferAPI.deleteById(transferId);
											ResultCode result = apiResponse.getResult();
											ctx.getResponse()
													.status(result.isOk() ? HttpStatus.NO_CONTENT
															: result.getHttpStatus())
													.send();
										})
								)
						)
				)
		);
	}

	public void stop() throws Exception {
		this.scheduledTransfersProcessor.stop();
		this.server.stop();
	}

	private void addLocationHeader(final Context ctx, final TransferApiResponse transferApiResponse,
			final ServerConfig serverConfig) {
		if (transferApiResponse.getResult().isOk()) {
			ctx.getResponse().getHeaders().add(
					HttpHeaders.LOCATION, buildResourceLocation(transferApiResponse, serverConfig));
		}
	}

	private String buildResourceLocation(final TransferApiResponse transferApiResponse,
			final ServerConfig serverConfig) {
		return new UriBuilder("http", serverConfig.getHost(), serverConfig.getPort())
				.pathSegment("1.0/transfers").pathSegment(transferApiResponse.getTransfer().getId())
				.build().toString();
	}

	private void sendTransferApiResponse(final Context ctx, final TransferApiResponse transferApiResponse,
			int okStatus) {
		if (transferApiResponse.getResult().isOk()) {
			ctx.getResponse().status(okStatus);
			ctx.render(Jackson.json((transferApiResponse.getTransfer())));
		} else {
			ctx.getResponse().status(transferApiResponse.getResult().getHttpStatus());
			if (transferApiResponse.getErrorResponse() != null) {
				ctx.render(Jackson.json(transferApiResponse.getErrorResponse()));
			} else {
				ctx.getResponse().send();
			}
		}
	}
}
