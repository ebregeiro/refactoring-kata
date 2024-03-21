package codingdojo.business.helpers;

import codingdojo.entities.internal.Customer;

public class CustomerPrinter {

    public static String print(Customer customer, String indent) {
        String sb = "\n" + indent + "Customer {" +
                "\n" + indent + "    externalId='" + customer.getExternalId() + '\'' +
                "\n" + indent + "    masterExternalId='" + customer.getMasterExternalId() + '\'' +
                "\n" + indent + "    companyNumber='" + customer.getCompanyNumber() + '\'' +
                "\n" + indent + "    internalId='" + customer.getInternalId() + '\'' +
                "\n" + indent + "    name='" + customer.getName() + '\'' +
                "\n" + indent + "    bonusPointsBalance='" + customer.getBonusPointsBalance() + '\'' +
                "\n" + indent + "    customerType=" + customer.getCustomerType() +
                "\n" + indent + "    preferredStore='" + customer.getPreferredStore() + '\'' +
                "\n" + indent + "    address=" + AddressPrinter.printAddress(customer.getAddress()) +
                "\n" + indent + "    shoppingLists=" + ShoppingListPrinter.printShoppingLists(customer.getShoppingLists(), indent + "    ") +
                "\n" + indent + "}";
        return sb;
    }

}
