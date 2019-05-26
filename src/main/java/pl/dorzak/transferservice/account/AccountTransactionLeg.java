package pl.dorzak.transferservice.account;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Models transaction influencing account balance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountTransactionLeg {

	private UUID id;
	@NonNull
	private UUID accountId;
	@NonNull
	private BigDecimal value;
	@NonNull
	private Currency currency;

}
