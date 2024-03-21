package codingdojo.data;

import codingdojo.entities.auxiliar.CustomerType;
import codingdojo.entities.internal.Customer;
import codingdojo.entities.internal.CustomerMatches;
import codingdojo.entities.internal.ShoppingList;
import codingdojo.entities.message.ExternalCustomer;
import codingdojo.exceptions.ConflictException;

import static codingdojo.entities.auxiliar.Constants.COMPANY_NUMBER;
import static codingdojo.entities.auxiliar.Constants.EXTERNAL_ID;

public class CustomerDataAccess {

    private final CustomerDataLayer customerDataLayer;

    public CustomerDataAccess(CustomerDataLayer customerDataLayer) {
        this.customerDataLayer = customerDataLayer;
    }

    public CustomerMatches loadCompanyCustomer(String externalId, String companyNumber) {
        CustomerMatches matches = new CustomerMatches();
        Customer matchByExternalId = this.customerDataLayer.findByExternalId(externalId);
        if (matchByExternalId != null) {
            matches.setCustomer(matchByExternalId);
            matches.setMatchTerm(EXTERNAL_ID);
            Customer matchByMasterExternalId = this.customerDataLayer.findByMasterExternalId(externalId);
            if (matchByMasterExternalId != null) matches.addDuplicate(matchByMasterExternalId);
        } else {
            Customer matchByCompanyNumber = this.customerDataLayer.findByCompanyNumber(companyNumber);
            if (matchByCompanyNumber != null) {
                matches.setCustomer(matchByCompanyNumber);
                matches.setMatchTerm(COMPANY_NUMBER);
            }
        }

        return matches;
    }

    public CustomerMatches loadPersonCustomer(String externalId) {
        CustomerMatches matches = new CustomerMatches();
        Customer matchByExternalId = this.customerDataLayer.findByExternalId(externalId);
        matches.setCustomer(matchByExternalId);

        if (matchByExternalId != null) matches.setMatchTerm(EXTERNAL_ID);
        return matches;
    }

    public Customer updateCustomerRecord(Customer customer) {
        return customerDataLayer.updateCustomerRecord(customer);
    }

    public Customer createCustomerRecord(Customer customer) {
        return customerDataLayer.createCustomerRecord(customer);
    }

    public void updateCustomerShoppingList(Customer customer, ShoppingList consumerShoppingList) {
        customer.addShoppingList(consumerShoppingList);
        customerDataLayer.updateShoppingList(consumerShoppingList);
        customerDataLayer.updateCustomerRecord(customer);
    }
}
