package mir.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.imohsenb.ISO8583.exceptions.ISOException;
import com.imohsenb.ISO8583.utils.StringUtil;
import lombok.NoArgsConstructor;

@JsonAutoDetect
@NoArgsConstructor
public class ParsedSubfield {

    private int id;
    private String type;
    // According to the MIP.
    private int lengthMIP;

    private String content;

    // Getters and Setters.

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getLengthMIP() {
        return lengthMIP;
    }

    public void setLengthMIP(int lengthMIP) {
        this.lengthMIP = lengthMIP;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // The end of the Getters and Setters.

    @JsonIgnore
    public String getHexContent() throws ISOException {
        String elementStr = StringUtil.asciiToHex(content);
        return elementStr;
    }

    @Override
    public String toString ()  {
        StringBuilder str  = new StringBuilder();
        str.append("\t\t\t\tSubfield:\n");
        str.append("\t\t\t\t\tid = " + id + "\n");
        str.append("\t\t\t\t\ttype = " + type + "\n");
        str.append("\t\t\t\t\tlengthMIP = " + lengthMIP + "\n");
        // Todo: remove the addition of the lengthInSymbolsReal.
        // str.append("\t\t\t\t\tlengthReal = " + lengthInSymbolsReal + "\n");
        str.append("\t\t\t\t\tcontent = " + content + "\n");
        return str.toString();
    }
}
