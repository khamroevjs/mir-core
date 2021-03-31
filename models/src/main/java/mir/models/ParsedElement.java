package mir.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.imohsenb.ISO8583.utils.StringUtil;
import lombok.NoArgsConstructor;

@JsonAutoDetect
@NoArgsConstructor
public class ParsedElement {

    @JsonIgnore
    // ^ (5E in hex) – means that the data of the content represent ASCII symbols.
    public final static String separatorSym = "^";
    @JsonIgnore
    // % (25 in hex) - means that the data of the content represent binary data.
    public final static String separatorBin = "%";

    // Binary data ("%") or ASCII symbols ("^").
    private String type;
    // From 01 to ZZ as hex symbols.
    private int id;
    // The length of the direct content without the type, the id, the length.
    // According to the MIP.
    private int lengthMIP;

    private String content;

    // Getters and Setters.
    public String getType() {
        return type;
    }

    public void setType(String dataFormat) {
        if (dataFormat.compareTo(separatorSym) == 0)
            type = separatorSym;
        // dataFormat.compareTo(separatorBin) == 0.
        else
            type = separatorBin;
    }

    public int getId() {
        return id;
    }

    public String getHexId() { return StringUtil.intToHexString(id); }

    public void setId(int id) {
        this.id = id;
    }

    public int getLengthMIP() {
        return lengthMIP;
    }

    public String getHexLengthMIP() { return StringUtil.intToHexString(lengthMIP); }

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

    /*
    Returns the String representation of the element.
     */
    @JsonIgnore
    public String getString() {
        StringBuilder elementStr = new StringBuilder();
        // The type.
        elementStr.append(type);
        // The id.
        String idStr = Integer.toString(id);
        if (idStr.length() == 2)
            elementStr.append(id);
        else
            // idStr.length() == 1
            elementStr.append("0" + id);
        // The length.
        String lengthStr = Integer.toString(lengthMIP);
        if (lengthStr.length() == 2)
            elementStr.append(lengthMIP);
        else
            // lengthStr.length() == 1
            elementStr.append("0" + lengthMIP);
        // The body.
        elementStr.append(content);
        return elementStr.toString();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("\t\t\t\tElement:\n");
        str.append("\t\t\t\t\tid = " + id + "\n");
        str.append("\t\t\t\t\ttype = " + type + "\n");
        str.append("\t\t\t\t\tlengthMIP = " + lengthMIP + "\n");
        // Todo: remove the addition of the lengthInSymbolsReal.
        // str.append("\t\t\t\t\tlengthReal = " + lengthInSymbolsReal + "\n");
        str.append("\t\t\t\t\tcontent = " + content + "\n");
        return str.toString();
    }
}
