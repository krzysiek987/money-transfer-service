package pl.dorzak.transferservice.transfer;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import pl.dorzak.transferservice.transfer.model.Transfer;
import pl.dorzak.transferservice.transfer.model.TransferStatus;

/**
 * Periodically polls scheduled Transfers and tries to invoke them. If operation fails Transfer state is change to ERROR.
 */
@Slf4j
public class ScheduledTransfersProcessor {

	private static final long SCHEDULED_TRANSFER_PROCESSING_DELAY_MS = TimeUnit.SECONDS.toMillis(10);
	private static final long SCHEDULED_TRANSFER_PROCESSING_PERIOD_MS = TimeUnit.SECONDS.toMillis(1);
	private final ScheduledExecutorService scheduler;
	private final TransferAPIImpl transferAPI;

	public ScheduledTransfersProcessor(final TransferAPIImpl transferAPI) {
		this.scheduler = Executors.newSingleThreadScheduledExecutor();
		this.transferAPI = transferAPI;
		start();
	}

	private void start() {
		scheduler.scheduleAtFixedRate(invokeScheduledTransfers(), SCHEDULED_TRANSFER_PROCESSING_DELAY_MS,
				SCHEDULED_TRANSFER_PROCESSING_PERIOD_MS, TimeUnit.MILLISECONDS);
	}

	private Runnable invokeScheduledTransfers() {
		return () -> {
			List<Transfer> toInvoke = transferAPI.getAllScheduledToInvoke();
			for (final Transfer transfer : toInvoke) {
				invokeTransfer(transfer);
			}
		};
	}

	private void invokeTransfer(final Transfer transfer) {
		if (!transferAPI.invoke(transfer).getResult().isOk()) {
			log.error("Could not invoke Transfer {}. Setting to error state.", transfer);
			if (!transferAPI.setStatus(transfer.getId(), TransferStatus.ERROR).getResult().isOk()) {
				log.error("Could not set status or Transfer {} to ERROR", transfer);
			}
		}
	}

	public void stop() {
		this.scheduler.shutdown();
	}
}
