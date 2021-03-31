package mir.parsing.routing;

import mir.parsing.formatters.Encoder;
import mir.models.ParsedMessage;
import com.imohsenb.ISO8583.exceptions.ISOException;

import java.io.IOException;

public class Router {

    /*
	Returns the parsedMessage which is given from the hex.
	 */
    public static ParsedMessage getParsedMessage(String hex) throws ISOException {
        Encoder encoder = new Encoder();
        ParsedMessage parsedMessage = encoder.getParsedMessageFromEncodedMessage(hex);
        return parsedMessage;
    }

    /*
    Returns the hex which is given from a parsedMessage.
    */
    public static String getEncodedMessage(ParsedMessage parsedMessage) throws IOException, ISOException {
        Encoder encoder = new Encoder();
        String hex = encoder.getEncodedMessageFromParsedMessage(parsedMessage);
        return hex;
    }
}
