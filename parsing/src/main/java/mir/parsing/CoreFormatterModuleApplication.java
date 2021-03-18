
package mir.parsing;
import mir.models.EncodedMessage;
import mir.models.ParsedMessage;
import mir.parsing.routing.Router;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imohsenb.ISO8583.builders.ISOMessageBuilder;
import com.imohsenb.ISO8583.entities.ISOMessage;
import com.imohsenb.ISO8583.enums.FIELDS;
import com.imohsenb.ISO8583.enums.MESSAGE_FUNCTION;
import com.imohsenb.ISO8583.enums.MESSAGE_ORIGIN;
import com.imohsenb.ISO8583.enums.VERSION;
import com.imohsenb.ISO8583.exceptions.ISOException;
import com.imohsenb.ISO8583.utils.StringUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;


@SpringBootApplication
@RestController
public class CoreFormatterModuleApplication {

	/*
	This is not supposed to be run.
	 */
	public static void main(String[] args) throws IOException, ISOException {
		SpringApplication.run(CoreFormatterModuleApplication.class, args);

		// ---------------------------------------------------------------------------------------
		// On the side of the services.

		// Creation an unparsed ISO-message.
		ISOMessage isoMessageService = ISOMessageBuilder.Unpacker()
				.setMessage(generateISOMessage())
				.build();
		// Creation of the JSON-wrapper for the ISO-message.
		EncodedMessage encodedServiceMessage = new EncodedMessage();
		// isoMessage without a header like it some service makes.
		encodedServiceMessage.message =
						isoMessageService.toString()
						.substring(10 /*after the end of a header*/, isoMessageService.toString().length());


		// Serialization of the encodedMessage into JSON.
		// String myMessage = "080020380000000000009200000000010231200332"; // Fields: 3, 11, 12, 13


		// initialMessage = 080000000000000100000008^0103111 (looks like in MIP). F48_Additional_Data_1, length = "0008"
		// initial string = "0800|0000000000000000000000000000000000000000000000010000000000000000|0008^0103111";
		// The hex representation of the initial message = 80|00000100|085E0103313131;
		// The representation in a byte[] array conditionally:
		// [8, 0] [0, 0] [0, 0] [0, 1] [0, 0] [0, 8] [5, E] [3, 0] [3, 1] [3, 0] [3, 3] [3, 1] [3, 1] [3, 1]
		// directly:
		// [1000|0000] [0000|0000] [0000|0000] [0000|0001] [0000|0000] [0000|1000]
		// [0101|1110] [0011|0000] [0011|0001] [0011|0000] [0011|0011] [0011|0001] [0011|0001] [0011|0001]
		// As decimal numbers: ...


		// String myMessage = "0800000000000001000000065E0103313131"; // F48_Additional_Data_1, length = "0008"
		// Fields: 3, 5, 7, 11, 12, 13, ...
		//String myMessage = "08002A3800000000000092000011111111111122222222220000010231200332";
		// Fields: 48. Elements: 1, 3
		// For a person: 0000000000|0800|0000000000010000|0016^0103111^0303115
	// 	String myMessage = "0000000000080000000000000100000016^0103111^0303115";
		String myMessage = "00000000000800000000000001000000165E303130333131315E30333033313135"; // LL before the 48th field.
		System.out.println("myMessage = " + myMessage);
		ISOMessage myISOMessage = ISOMessageBuilder.Unpacker()
								.setMessage(myMessage)
								.build();
		System.out.println("F48. Hex = " + myISOMessage.getStringField(48, false));
		System.out.println("F48. ASCII = " + myISOMessage.getStringField(48, true));
		System.out.println("F48. Length = " + myISOMessage.getStringField(48, true).length());
		// System.out.println((int)'^');
		// System.out.println((int)'%');
		EncodedMessage myEncodedMessage = new EncodedMessage();
		myEncodedMessage.message = myMessage;

		StringWriter writer = new StringWriter();
		ObjectMapper mapper = new ObjectMapper();
		//mapper.writeValue(writer, encodedServiceMessage);
		mapper.writeValue(writer, myEncodedMessage);
		// It is that a service sends to the Platform.
		String encodedMessageJSON = writer.toString();
		System.out.println("encodedServiceMessage.message = " + encodedServiceMessage.message);
		// ----------------------------------------------------------------------------------------

		// ----------------------------------------------------------------------------------------
		// On the side of the Platform.

		// For parsing.
		// Like we has accepted this from the routing module.
		String encodedMessageJSONFromService = encodedMessageJSON;
		Router router = new Router();
		// This is that will be sent to routing module back.
		// The extracting of the encodedMessage from the transmitted JSON string.
		ParsedMessage parsedMessage = router.getParsedMessage(encodedMessageJSONFromService);

		// The serialization for debugging.
		writer = new StringWriter();
		mapper.writeValue(writer, parsedMessage);

		// For encoding.
		String parsedMessageJSON2 = writer.toString();
		String encodedMessageJSON2 = router.getEncodedMessage(parsedMessage);
		// ----------------------------------------------------------------------------------------
	}

	/*
	For debugging. Returns a generated ISOMessage.
	 */
	static String generateISOMessage() throws ISOException {
		ISOMessage isoMessageService = new ISOMessage();
		try {
			isoMessageService = ISOMessageBuilder.Packer(VERSION.V1987)
					.networkManagement()
					.mti(MESSAGE_FUNCTION.Request, MESSAGE_ORIGIN.Acquirer)
					.processCode("920000")
					.setField(FIELDS.F11_STAN,  "1")
					.setField(FIELDS.F12_LocalTime,  "023120")
					.setField(FIELDS.F13_LocalDate,  "0332")
					//.setField(FIELDS.F32_AcquiringInstitutionIdCode, "22222")
					//.setField(FIELDS.F45_Track1, "7777777777777777777777777777777777777777777777777777777777777777777777777777")
					.setField(FIELDS.F48_AddData_Private, "^0103222^97043937") // "^0103222"
					//.setField(FIELDS.F57_Reserved_National, "%^222")
					//.setField(FIELDS.F24_NII_FunctionCode,  "333")
					.setHeader("0000000000") // "0000000000"
					.build();
		} catch (ISOException e) {
			e.printStackTrace();
		}
		System.out.println("generated isoMessage = " + isoMessageService.toString());
		System.out.println("generated isoMessage. The initial form of the 48th field = "
							+ isoMessageService.getStringField(48, true));
		System.out.println("primaryBitmap = " + StringUtil.fromByteArray(isoMessageService.getPrimaryBitmap()));
		System.out.println("length of the 48th field in the initial representation = " +
							isoMessageService.getField(48).length);
		System.out.println("The 48th field = " + isoMessageService.getStringField(48));
		return isoMessageService.toString();
	}
}