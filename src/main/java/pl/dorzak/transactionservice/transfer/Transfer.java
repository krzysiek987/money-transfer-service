package pl.dorzak.transactionservice.transfer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Transfer {

	@JsonIgnore
	private UUID id;

	private Instant createdAt;

}
