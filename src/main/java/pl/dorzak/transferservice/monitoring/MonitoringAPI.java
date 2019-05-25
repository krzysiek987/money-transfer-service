package pl.dorzak.transferservice.monitoring;

import pl.dorzak.transferservice.RatpackChainVisitor;
import ratpack.handling.Chain;

/**
 * Registers health endpoint. Basically it's only for smoke test, in real life it could do actual checks.
 */
public class MonitoringAPI implements RatpackChainVisitor {

	@Override
	public void registerIn(final Chain chain) {
		chain.get("health", ctx -> ctx.render("UP"));
	}
}
