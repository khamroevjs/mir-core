package com.imohsenb.ISO8583.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mohsen Beiranvand
 */
public enum FIELDS {
    // |Field title                         |no       |type       |minLen       |maxLen        |fixed       |format      |hasSubfields     |maxSubfieldsId     |hasElements
    // using
    F1_Bitmap                           (1,  "b",   64, 64,   true,  null, false, 0, false),
    // using
    F2_PAN                              (2,  "n",   13, 19,   false, "LL", false, 0, false),
    F3_ProcessCode                      (3,  "n",   6, 6,    true,  null, true, 3, false),
    // using
    F4_AmountTransaction                (4,  "n",   12, 12,   true,  null, false, 0, false),
    F5_AmountSettlement                 (5,  "n",   12, 12,   true,  null, false, 0, false),
    F6_AmountCardholder                 (6,  "n",   12 , 12,   true,  null, false, 0, false),
    // using probably
    F7_TransmissionDateTime             (7,  "n",   10, 10,   true,  null, false, 0, false),
    // The 8th field is missed in MIP.
    F9_ConversionRate_Settlement        (9,  "n",   8, 8,    true,  null, true, 2, false),
    F10_ConversionRate_Cardholder       (10, "n",   8, 8,    true,  null, true, 2, false),
    F11_STAN                            (11, "n",   6, 6,    true,  null, false, 0, false),
    // using
    F12_LocalTime                       (12, "n",   6, 6,    true,  null, false, 0, false),
    // using
    F13_LocalDate                       (13, "n",   4, 4,    true,  null, false, 0, false),
    F14_ExpirationDate                  (14, "n",   4, 4,    true,  null, false, 0, false),
    F15_SettlementDate                  (15, "n",   4, 4,    true,  null, false, 0, false),
    F16_CurrencyConversionDate          (16, "n",   4, 4,    true,  null, false, 0, false),
    // The 17th field is missed in MIP.
    F18_MerchantType                    (18, "n",   4, 4,    true,  null, false, 0, false),
    // The 19-21th fields are missed in MIP.
    F22_EntryMode                       (22, "n",   3, 3,    true,  null, true, 2, false),
    // using
    F23_PANSequence                     (23, "n",   3, 3,    true,  null, false, 0, false),
    // The 24th field is missed in MIP bu is used in tests!
    F24_NII_FunctionCode                (24, "n",   3,  3,  true,  null, false, 0, false),
    // The 25th fields are missed in MIP.
    F26_POS_CaptureCode                 (26, "n",   2, 2,    true,  null, false, 0, false),
    // The 27th field is missed in MIP.
    F28_Amount_TransactionFee           (28, "an", 8, 8,    true,  null, true, 2, false),
    // The 29-31th fields are missed in MIP.
    F32_AcquiringInstitutionIdCode      (32, "n",   1, 11,   false, "LL", false, 0,false),
    F33_ForwardingInstitutionIdCode     (33, "n",   1, 11,   false, "LL", false, 0, false),
    // The 34th field is missed in MIP.
    F35_Track2                          (35, "ans",   1, 37,   false, "LL", false, 0, false),
    // The 36th field is missed in MIP.
    // using probably
    F37_RRN                             (37, "n",  12, 12,   true,  null, false, 0, false),
    F38_AuthIdResponse                  (38, "ans",  6, 6,    true,  null, false, 0, false),
    F39_ResponseCode                    (39, "an",  2, 2,    true,  null, false, 0, false),
    // The 40th field is missed in MIP.
    F41_CA_TerminalID                   (41, "ans", 8, 8,    true,  null, false, 0, false),
    // using
    F42_CA_ID                           (42, "ans", 15, 15,   true,  null, false, 0, false),
    F43_CardAcceptorInfo                (43, "ans", 40, 40,   true,  null, true, 5, false),
    // The 44-47th fields is missed in MIP.
    // using
    F48_AddData_Private                 (48, "ans", 6, 999,  false, "LLL", false, 0, true),
    F49_CurrencyCode_Transaction        (49, "n",   3, 3,    true,  null, false, 0, false),
    F50_CurrencyCode_Settlement         (50, "n", 3, 3,    true,  null, false, 0, false),
    F51_CurrencyCode_Cardholder         (51, "n", 3, 3,    true,  null, false, 0, false),
    F52_PIN                             (52, "b",   8, 8,    true,  null, false, 0, false),
    F53_SecurityControlInfo             (53, "n",   16,16,   true,  null, true, 6, false),
    F54_AddAmount                       (54, "an",  20, 120,  false, "LLL", true, 6, false),
    // The minLength must be refined before using this field!!!
    // The maximum count of subfields is equal to 22 because compulsory and additional subfields for the acquirer are considered together.
    F55_ICC                             (55, "ans", 1, 999,  false, "LLL", true, 22, false),
    // The 56-60th fields are missed in MIP.
    // The minLength must be refined before using this field!!!
    F61_Reserved_Private                (61, "ans", 1,999,  false, "LLL", true, 10, false),
    // The 62th field is missed in MIP.
    // The minLength must be refined before using this field!!!
    // Unfixed variant from the MIP: F63_Reserved_Private                (63, "ans", 1, 999,  false, "LLL", true, 3, false),
    // Fixed variant for the project:
    // Todo: add to the documentation that there this is fixed field.
    F63_Reserved_Private                (63, "ans", 23, 23,  true, null, true, 3, false),
    // The 64th field is missed in MIP but is necessary for BaseMessageClassBuilder Class of the ISO Lib.
    F64_MAC                             (64, "b",   16, 16,   true,  null, false, 0, false);
    // The fields with bigger numbers are not considered in this project.

    private final int num;
    private final String type;
    private final int minLength;
    private final int maxLength;
    private final boolean fixed;
    private final String format;
    private final boolean hasSubfields;
    // For major fields means the single allowable value of a count.
    // For some fields it means the maximum count.
    private final int maxSubfieldsId;
    private final boolean hasElements;

    FIELDS(int num, String type, int minLength, int maxLength, boolean fixed,
           String format, boolean hasSubfields, int maxSubfieldsId, boolean hasElements) {
        this.num = num;
        this.type = type;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.fixed = fixed;
        this.format = format;
        this.hasSubfields = hasSubfields;
        this.maxSubfieldsId = maxSubfieldsId;
        this.hasElements = hasElements;
    }

    private static Map<Integer, FIELDS> map = new HashMap<Integer, FIELDS>();

    static {
        for (FIELDS field : FIELDS.values()) {
            map.put(field.getNum(), field);
        }
    }

    public int getNum() {
        return num;
    }

    public String getType() {
        return type;
    }

    public int getMinLength() { return minLength; }

    public int getMaxLength() {
        return maxLength;
    }

    public boolean isFixed() {
        return fixed;
    }

    public String getFormat() {
        return format;
    }

    public boolean getHasSubfields() {return this.hasSubfields;}

    public int getMaxSubfieldsId() {return this.maxSubfieldsId;}

    public boolean getHasElements() { return hasElements; }

    public static FIELDS valueOf(int no) {
        return map.get(no);
    }
}
