package mir.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

/*
Supposes a header.
 */
@JsonAutoDetect
@NoArgsConstructor
public class EncodedMessage {
    @JsonProperty("ParsedMessage")
    // Consists hex symbols.
    public String message;
}
