package codingdojo.data;

import codingdojo.entities.internal.Customer;
import codingdojo.entities.internal.ShoppingList;

public interface CustomerDataLayer {

    Customer updateCustomerRecord(Customer customer);

    Customer createCustomerRecord(Customer customer);

    void updateShoppingList(ShoppingList consumerShoppingList);

    Customer findByExternalId(String externalId);

    Customer findByMasterExternalId(String externalId);

    Customer findByCompanyNumber(String companyNumber);
}
