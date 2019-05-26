package pl.dorzak.transferservice.account;

import java.util.Currency;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

	private UUID id;
	@NonNull
	private Currency currency;

}
