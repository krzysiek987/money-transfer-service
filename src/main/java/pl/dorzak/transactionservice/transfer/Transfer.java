package pl.dorzak.transactionservice.transfer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.UUID;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class Transfer {

	@JsonIgnore
	private UUID id;
	@NonNull
	private UUID sourceAccountId;
	@NonNull
	private UUID destinationAccountId;
	@NonNull
	private BigDecimal amount;
	@NonNull
	private Currency currency;
	@NonNull
	private String title;

	private ZonedDateTime scheduleAt;

}
