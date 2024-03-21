package codingdojo.business.helpers;

import codingdojo.entities.internal.Address;

public class AddressPrinter {
    public static String printAddress(Address address) {
        if (address == null) {
            return "'null'";
        }
        String sb = "'" +
                address.getStreet() +
                ", " +
                address.getPostalCode() +
                " " +
                address.getCity() +
                "'";
        return sb;
    }
}
