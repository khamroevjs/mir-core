package mir.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.imohsenb.ISO8583.exceptions.ISOException;
import com.imohsenb.ISO8583.utils.StringUtil;
import lombok.NoArgsConstructor;

@JsonAutoDetect
@NoArgsConstructor
public class ParsedElement {

    @JsonIgnore
    // ^ (0x5E) – means that symbols in the field (not object Field!) of the data
    // of an element are represented by ASCII.
    public final static String separatorSym = "^";
    @JsonIgnore
    // % (0x25) means that binary data in the field (not object Field!) of an element
    // are represented by bytes 8 bits each.
    public final static String separatorBin = "%";

    // Binary data or ASCII symbols.
    private String type;
    // From 01 to ZZ in the hex system.
    private int id;
    // The length of the direct content without the type, the id, the length.
    private int length;
    // Content.
    private String content;

    // Getters and Setters.
    public String getType() {
        return type;
    }

    public void setType(String dataFormat) throws ISOException {
        if (dataFormat.compareTo(separatorSym) == 0)
            type = separatorSym;
        else {
            if (dataFormat.compareTo(separatorBin) == 0)
                type = separatorBin;
            else
                throw new ISOException("This type of elements does not exist!");
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    // The end of the Getters and Setters.

    @JsonIgnore
    public String getHexString() throws ISOException {
        StringBuilder elementStr = new StringBuilder();
        // The type.
        if (type.compareTo(separatorSym) == 0)
            elementStr.append("5E");
        else {
            if (type.compareTo(separatorBin) == 0)
                elementStr.append("25");
            else
                throw new ISOException("ParsedElement has not a type!");
        }
        // The id.
        String idStr = StringUtil.asciiToHex(Integer.toString(id));
        if (idStr.length() == 4)
            elementStr.append(idStr);
        else
            // idStr.length() == 2
            elementStr.append("30" + idStr);
        // The length.
        String lengthStr = StringUtil.asciiToHex(Integer.toString(id));
        if (lengthStr.length() == 4)
            elementStr.append(lengthStr);
        else
            // lengthStr.length() == 2
            elementStr.append("30" + lengthStr);
        // The body.
        elementStr.append(StringUtil.asciiToHex(content));
        return elementStr.toString();
    }

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
        String lengthStr = Integer.toString(length);
        if (lengthStr.length() == 2)
            elementStr.append(length);
        else
            // lengthStr.length() == 1
            elementStr.append("0" + length);
        // The body.
        elementStr.append(content);
        return elementStr.toString();
    }
}
