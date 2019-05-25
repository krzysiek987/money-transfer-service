package pl.dorzak.transferservice.transfer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Transfer {

	@JsonIgnore
	private UUID id;
	@NonNull
	private TransferStatus status;
	@NonNull
	private UUID sourceAccountId;
	@NonNull
	private UUID destinationAccountId;
	@NonNull
	private BigDecimal amount;
	@NonNull
	private Currency currency;

	private String title;
	@NonNull
	private Instant createdAt;

	private Instant updatedAt;

	private Instant invokedAt;

	private Instant scheduledAt;

}
