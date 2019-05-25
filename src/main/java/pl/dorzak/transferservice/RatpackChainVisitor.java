package pl.dorzak.transferservice;

import ratpack.handling.Chain;

public interface RatpackChainVisitor {

	void registerIn(final Chain chain);
}
