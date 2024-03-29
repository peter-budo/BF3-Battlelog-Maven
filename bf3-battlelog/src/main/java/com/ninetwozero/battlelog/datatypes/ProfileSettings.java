
package com.ninetwozero.battlelog.datatypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileSettings {

    public static final String PROFILE_INFO_BIRTHDAY = "profile_info_birthday";
    public static final String PROFILE_INFO_COUNTRY = "profile_info_country";

    public static final Map<String, String> COUNTRY = new HashMap<String, String>() {
        {
            put("Select your location", "");
            put("Afghanistan", "AF");
            put("Albania", "AL");
            put("Algeria", "DZ");
            put("American Samoa", "AS");
            put("Andorra", "AD");
            put("Angola", "AO");
            put("Anguilla", "AI");
            put("Antarctica", "AQ");
            put("Antigua and Barbuda", "AG");
            put("Argentina", "AR");
            put("Armenia", "AM");
            put("Aruba", "AW");
            put("Australia", "AU");
            put("Austria", "AT");
            put("Azerbaijan", "AZ");
            put("Bahamas", "BS");
            put("Bahrain", "BH");
            put("Bangladesh", "BD");
            put("Barbados", "BB");
            put("Belarus", "BY");
            put("Belgium", "BE");
            put("Belize", "BZ");
            put("Benin", "BJ");
            put("Bermuda", "BM");
            put("Bhutan", "BT");
            put("Bolivia, Plurinational State of", "BO");
            put("Bosnia and Herzegovina", "BA");
            put("Botswana", "BW");
            put("Bouvet Island", "BV");
            put("Brazil", "BR");
            put("British Indian Ocean Territory", "IO");
            put("Brunei Darussalam", "BN");
            put("Bulgaria", "BG");
            put("Burkina Faso", "BF");
            put("Burundi", "BI");
            put("Cambodia", "KH");
            put("Cameroon", "CM");
            put("Canada", "CA");
            put("Cape Verde", "CV");
            put("Cayman Islands", "KY");
            put("Central African Republic", "CF");
            put("Chad", "TD");
            put("Chile", "CL");
            put("China", "CN");
            put("Christmas Island", "CX");
            put("Cocos (Keeling) Islands", "CC");
            put("Colombia", "CO");
            put("Comoros", "KM");
            put("Congo", "CG");
            put("Congo, The Democratic Republic of the", "CD");
            put("Cook Islands", "CK");
            put("Costa Rica", "CR");
            put("Croatia", "HR");
            put("Cuba", "CU");
            put("Cyprus", "CY");
            put("Czech Republic", "CZ");
            put("C\u00f4te d\'Ivoire", "CI");
            put("Denmark", "DK");
            put("Djibouti", "DJ");
            put("Dominica", "DM");
            put("Dominican Republic", "DO");
            put("Ecuador", "EC");
            put("Egypt", "EG");
            put("El Salvador", "SV");
            put("Equatorial Guinea", "GQ");
            put("Eritrea", "ER");
            put("Estonia", "EE");
            put("Ethiopia", "ET");
            put("Falkland Islands (Malvinas)", "FK");
            put("Faroe Islands", "FO");
            put("Fiji", "FJ");
            put("Finland", "FI");
            put("France", "FR");
            put("French Guiana", "GF");
            put("French Polynesia", "PF");
            put("French Southern Territories", "TF");
            put("Gabon", "GA");
            put("Gambia", "GM");
            put("Georgia", "GE");
            put("Germany", "DE");
            put("Ghana", "GH");
            put("Gibraltar", "GI");
            put("Greece", "GR");
            put("Greenland", "GL");
            put("Grenada", "GD");
            put("Guadeloupe", "GP");
            put("Guam", "GU");
            put("Guatemala", "GT");
            put("Guernsey", "GG");
            put("Guinea", "GN");
            put("Guinea-Bissau", "GW");
            put("Guyana", "GY");
            put("Haiti", "HT");
            put("Heard Island and McDonald Islands", "HM");
            put("Holy See (Vatican City State)", "VA");
            put("Honduras", "HN");
            put("Hong Kong", "HK");
            put("Hungary", "HU");
            put("Iceland", "IS");
            put("India", "IN");
            put("Indonesia", "ID");
            put("Iran, Islamic Republic of", "IR");
            put("Iraq", "IQ");
            put("Ireland", "IE");
            put("Isle of Man", "IM");
            put("Israel", "IL");
            put("Italy", "IT");
            put("Jamaica", "JM");
            put("Japan", "JP");
            put("Jersey", "JE");
            put("Jordan", "JO");
            put("Kazakhstan", "KZ");
            put("Kenya", "KE");
            put("Kiribati", "KI");
            put("Korea, Democratic People\'s Republic of", "KP");
            put("Korea, Republic of", "KR");
            put("Kuwait", "KW");
            put("Kyrgyzstan", "KG");
            put("Lao People\'s Democratic Republic", "LA");
            put("Latvia", "LV");
            put("Lebanon", "LB");
            put("Lesotho", "LS");
            put("Liberia", "LR");
            put("Libyan Arab Jamahiriya", "LY");
            put("Liechtenstein", "LI");
            put("Lithuania", "LT");
            put("Luxembourg", "LU");
            put("Macao", "MO");
            put("Macedonia, The Former Yugoslav Republic of", "MK");
            put("Madagascar", "MG");
            put("Malawi", "MW");
            put("Malaysia", "MY");
            put("Maldives", "MV");
            put("Mali", "ML");
            put("Malta", "MT");
            put("Marshall Islands", "MH");
            put("Martinique", "MQ");
            put("Mauritania", "MR");
            put("Mauritius", "MU");
            put("Mayotte", "YT");
            put("Mexico", "MX");
            put("Micronesia, Federated States of", "FM");
            put("Moldova, Republic of", "MD");
            put("Monaco", "MC");
            put("Mongolia", "MN");
            put("Montenegro", "ME");
            put("Montserrat", "MS");
            put("Morocco", "MA");
            put("Mozambique", "MZ");
            put("Myanmar", "MM");
            put("Namibia", "NA");
            put("Nauru", "NR");
            put("Nepal", "NP");
            put("Netherlands", "NL");
            put("Netherlands Antilles", "AN");
            put("New Caledonia", "NC");
            put("New Zealand", "NZ");
            put("Nicaragua", "NI");
            put("Niger", "NE");
            put("Nigeria", "NG");
            put("Niue", "NU");
            put("Norfolk Island", "NF");
            put("Northern Mariana Islands", "MP");
            put("Norway", "NO");
            put("Oman", "OM");
            put("Pakistan", "PK");
            put("Palau", "PW");
            put("Palestinian Territory, Occupied", "PS");
            put("Panama", "PA");
            put("Papua New Guinea", "PG");
            put("Paraguay", "PY");
            put("Peru", "PE");
            put("Philippines", "PH");
            put("Pitcairn", "PN");
            put("Poland", "PL");
            put("Portugal", "PT");
            put("Puerto Rico", "PR");
            put("Qatar", "QA");
            put("Romania", "RO");
            put("Russian Federation", "RU");
            put("Rwanda", "RW");
            put("R\u00e9union", "RE");
            put("Saint Barth\u00e9lemy", "BL");
            put("Saint Helena, Ascension and Tristan Da Cunha", "SH");
            put("Saint Kitts and Nevis", "KN");
            put("Saint Lucia", "LC");
            put("Saint Martin", "MF");
            put("Saint Pierre and Miquelon", "PM");
            put("Saint Vincent and the Grenadines", "VC");
            put("Samoa", "WS");
            put("San Marino", "SM");
            put("Sao Tome and Principe", "ST");
            put("Saudi Arabia", "SA");
            put("Senegal", "SN");
            put("Serbia", "RS");
            put("Seychelles", "SC");
            put("Sierra Leone", "SL");
            put("Singapore", "SG");
            put("Slovakia", "SK");
            put("Slovenia", "SI");
            put("Solomon Islands", "SB");
            put("Somalia", "SO");
            put("South Africa", "ZA");
            put("South Georgia and the South Sandwich Islands", "GS");
            put("Spain", "ES");
            put("Sri Lanka", "LK");
            put("Sudan", "SD");
            put("Suriname", "SR");
            put("Svalbard and Jan Mayen", "SJ");
            put("Swaziland", "SZ");
            put("Sweden", "SE");
            put("Switzerland", "CH");
            put("Syrian Arab Republic", "SY");
            put("Taiwan", "TW");
            put("Tajikistan", "TJ");
            put("Tanzania, United Republic of", "TZ");
            put("Thailand", "TH");
            put("Timor-Leste", "TL");
            put("Togo", "TG");
            put("Tokelau", "TK");
            put("Tonga", "TO");
            put("Trinidad and Tobago", "TT");
            put("Tunisia", "TN");
            put("Turkey", "TR");
            put("Turkmenistan", "TM");
            put("Turks and Caicos Islands", "TC");
            put("Tuvalu", "TV");
            put("Uganda", "UG");
            put("Ukraine", "UA");
            put("United Arab Emirates", "AE");
            put("United Kingdom", "GB");
            put("United States", "US");
            put("United States Minor Outlying Islands", "UM");
            put("Uruguay", "UY");
            put("Uzbekistan", "UZ");
            put("Vanuatu", "VU");
            put("Venezuela, Bolivarian Republic of", "VE");
            put("Vietnam", "VN");
            put("Virgin Islands, British", "VG");
            put("Virgin Islands, U.S.", "VI");
            put("Wallis and Futuna", "WF");
            put("Western Sahara", "EH");
            put("Yemen", "YE");
            put("Zambia", "ZM");
            put("Zimbabwe", "ZW");
            put("\u00c5land Islands", "AX");

        }
    };
    public static final List<String> DATE_PATTERNS = new ArrayList<String>() {
        {
            add("dd.MM.yyyy | MM.dd");
            add("yyyy-MM-dd | dd/MM");
            add("dd/MM/yyyy | dd/MM");
            add("MM.dd.yyyy | MM.dd");
            add("MM/dd/yyyy | MM/dd");
        }
    };
    public static final CharSequence[] DATE_FORMAT_VALUES = new CharSequence[] {
            "de", "iso", "es", "us", "uk"
    };

    public static final CharSequence[] TIME_FORMATS = new CharSequence[] {
            "24 hour clock", "12 hour clock (AM/PM)"
    };
    public static final CharSequence[] TIME_FORMAT_VALUES = new CharSequence[] {
            "12", "24"
    };

    public static final CharSequence[] LOCAL_TIME_VALUES =
            new CharSequence[] {
                    "-720", "-660", "-600", "-570", "-540", "-480", "-420", "-360", "-300", "-270",
                    "-240"
                    , "-210", "-180", "-150", "-120", "-60", "0", "60", "120", "180", "210", "240",
                    "270", "300", "330"
                    , "345", "360", "390", "420", "480", "540", "570", "600", "630", "660", "690",
                    "720", "765", "780"
                    , "825", "840"
            };
}
