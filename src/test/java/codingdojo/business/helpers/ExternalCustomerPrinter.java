package codingdojo.business.helpers;

import codingdojo.entities.message.ExternalCustomer;

public class ExternalCustomerPrinter {

    public static String print(ExternalCustomer externalCustomer, String indent) {
        String sb = "ExternalCustomer {" +
                "\n" + indent + "    externalId='" + externalCustomer.getExternalId() + '\'' +
                "\n" + indent + "    companyNumber='" + externalCustomer.getCompanyNumber() + '\'' +
                "\n" + indent + "    name='" + externalCustomer.getName() + '\'' +
                "\n" + indent + "    preferredStore='" + externalCustomer.getPreferredStore() + '\'' +
                "\n" + indent + "    bonusPointsBalance='" + externalCustomer.getBonusPointsBalance() + '\'' +
                "\n" + indent + "    address=" + AddressPrinter.printAddress(externalCustomer.getPostalAddress()) +
                "\n" + indent + "    shoppingLists=" + ShoppingListPrinter.printShoppingLists(externalCustomer.getShoppingLists(), indent + "    ") +
                "\n" + indent + "}";

        return sb;
    }
}
