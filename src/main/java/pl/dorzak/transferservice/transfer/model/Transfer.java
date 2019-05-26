package pl.dorzak.transferservice.transfer.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Transfer entity. Things like update or completion timestamp were intentionally omitted.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Transfer {

	private UUID id;
	@Builder.Default
	private TransferStatus status = TransferStatus.SUCCESS;

	private UUID sourceAccountId;

	private UUID destinationAccountId;

	private BigDecimal value;

	private Currency currency;

	private String title;

	private Instant createdAt;

	private ZonedDateTime scheduledAt;

}
