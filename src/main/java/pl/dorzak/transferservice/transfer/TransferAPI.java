package pl.dorzak.transferservice.transfer;

import java.util.UUID;
import javax.sql.DataSource;
import pl.dorzak.transferservice.RatpackChainVisitor;
import pl.dorzak.transferservice.utils.HttpStatus;
import ratpack.handling.Chain;

public class TransferAPI implements RatpackChainVisitor {

	public TransferAPI(DataSource dataSource) {
		new TransferRepository(dataSource);
	}

	public void registerIn(final Chain chain) {
		chain
				.path("1.0/transfers", ctx ->
						ctx.byMethod(method ->
								method
										.get(() -> ctx.getResponse().send())
										.post(() -> ctx.getResponse()
												.status(HttpStatus.CREATED).send())
						)
				)
				.path("1.0/transfers/:transferId", ctx ->
						ctx.byMethod(method ->
								method
										.get(() -> {
											var transferId = UUID.fromString(
													ctx.getPathTokens().get("transferId"));
											ctx.getResponse().send();
										})
										.put(() -> ctx.getResponse().send())
										.patch(() -> ctx.getResponse().send())
										.delete(() -> ctx.getResponse()
												.status(HttpStatus.NO_CONTENT).send())
						)
				);
	}
}
