package mir.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.NoArgsConstructor;
import mir.models.check_annotations.AnyOf;

import java.util.Arrays;
import java.util.HashMap;

@JsonAutoDetect
@NoArgsConstructor
public class ParsedField {

    @AnyOf(values = {"2", "3", "4", "12", "13", "23", "42", "48", "49", "63"})
    private int id;
    private String type;
    // According to the MIP.
    private int lengthMIP;

    private String content;
    private boolean hasSubfields = false;
    private boolean hasElements = false;

    private HashMap<Integer, ParsedSubfield> subfields = new HashMap<Integer, ParsedSubfield>();
    private HashMap<Integer, ParsedElement> elements = new HashMap<Integer, ParsedElement>();

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

    public boolean getHasSubfields() { return this.hasSubfields;}

    public void setHasSubfields(boolean hasSubfields) { this.hasSubfields = hasSubfields;}

    public boolean getHasElements() {
        return this.hasElements;
    }

    public void setHasElements(boolean hasElements) {
        this.hasElements = hasElements;
    }

    public HashMap<Integer, ParsedSubfield> getSubfields() {
        return subfields;
    }

    public void setSubfields(HashMap<Integer, ParsedSubfield> subfields) {
        this.subfields = subfields;
    }

    public void setSubfield(ParsedSubfield parsedSubfield) {
        subfields.put(parsedSubfield.getId(), parsedSubfield);
    }

    public HashMap<Integer, ParsedElement> getElements() {
        return elements;
    }

    public void setElements(HashMap<Integer, ParsedElement> elements) {
        this.elements = elements;
    }

    // Todo: add to the documentation.
    public void addElement(ParsedElement parsedElement) {
        elements.put(parsedElement.getId(), parsedElement);
    }

    // The end of the Getters and Setters.

    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("\t\tParsedField:\n");
        str.append("\t\t\tid = " + id + "\n");
        str.append("\t\t\ttype = " + type + "\n");
        str.append("\t\t\tlengthMIP = " + lengthMIP + "\n");
        str.append("\t\t\tcontent = " + content + "\n");
        str.append("\t\t\thasSubfields = " + hasSubfields + "\n");
        str.append("\t\t\thasElements = " + hasElements + "\n");
        str.append("\t\t\tSubfields:\n");
        str.append(toStringParsedSubfields());
        str.append("\t\t\tElements:\n");
        str.append(toStringParsedElements());
        return str.toString();
    }

    private String toStringParsedSubfields() {
        StringBuilder str = new StringBuilder();
        Integer[] ids = Arrays.copyOf(subfields.keySet().toArray(), subfields.keySet().size(), Integer[].class);
        Arrays.sort(ids);
        for (int id : ids)
            str.append(subfields.get(id).toString());
        return str.toString();
    }

    private String toStringParsedElements() {
        StringBuilder str = new StringBuilder();
        Integer[] ids = Arrays.copyOf(elements.keySet().toArray(), elements.keySet().size(), Integer[].class);
        Arrays.sort(ids);
        for (int id : ids)
            str.append(elements.get(id).toString());
        return str.toString();
    }
}