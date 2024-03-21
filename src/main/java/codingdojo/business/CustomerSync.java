package codingdojo.business;

import codingdojo.data.CustomerDataAccess;
import codingdojo.data.CustomerDataLayer;
import codingdojo.entities.internal.Customer;
import codingdojo.entities.internal.CustomerMatches;
import codingdojo.entities.internal.ShoppingList;
import codingdojo.entities.auxiliar.CustomerType;
import codingdojo.entities.message.ExternalCustomer;
import codingdojo.exceptions.ConflictException;

import java.util.List;

import static codingdojo.entities.auxiliar.Constants.COMPANY_NUMBER;
import static codingdojo.entities.auxiliar.Constants.EXTERNAL_ID;

/**
 * Class used to synchronize external Customer message with internal Customer.
 * Create or Update the customer accordingly
 */
public class CustomerSync {

    private final CustomerDataAccess customerDataAccess;

    public CustomerSync(CustomerDataLayer customerDataLayer) {
        this(new CustomerDataAccess(customerDataLayer));
    }

    public CustomerSync(CustomerDataAccess db) {
        this.customerDataAccess = db;
    }

    /**
    * The entrypoint, responsible to receive the externalCustomer, apply the rules and constraints, and take
     * action to Create or Update the customer.
    */
    public boolean syncWithDataLayer(ExternalCustomer externalCustomer) {
        CustomerMatches customerMatches = getCustomerMatches(externalCustomer);
        Customer customer = getCustomer(externalCustomer, customerMatches);
        return createOrUpdateCustomer(externalCustomer, customer, customerMatches);
    }

    /**
     * Looking and retrieve for existent customer at database based on externalCustomer
     */
    private CustomerMatches getCustomerMatches(ExternalCustomer externalCustomer) {
        if (externalCustomer.isCompany()) {
            return loadCompanyCustomer(externalCustomer);
        } else {
            return loadPersonCustomer(externalCustomer);
        }
    }

    /**
     * getCustomer returned from database and check existence to populate the data
     */
    private Customer getCustomer(ExternalCustomer externalCustomer, CustomerMatches customerMatches) {
        Customer customer = customerMatches.getCustomer();
        customer = validateCustomerExistence(externalCustomer, customer);
        populateCustomerFields(externalCustomer, customer);
        return customer;
    }

    /**
     * Create a Customer at database and update all data
     */
    private boolean createOrUpdateCustomer(ExternalCustomer externalCustomer, Customer customer, CustomerMatches customerMatches) {
        boolean created = false;
        if (customer.getInternalId() == null){
            customer = createCustomer(customer);
            created = true;
        } else {
            updateCustomer(customer);
        }
        updateAllCustomerData(externalCustomer, customer, customerMatches);
        return created;
    }

    /**
     * in case of non-existence of customer, create a new one in memory, to be persisted after
     */
    private Customer validateCustomerExistence(ExternalCustomer externalCustomer, Customer customer) {
        if (customer == null) {
            customer = new Customer();
            customer.setExternalId(externalCustomer.getExternalId());
            customer.setMasterExternalId(externalCustomer.getExternalId());
        }
        customer.setName(externalCustomer.getName());
        return customer;
    }

    /**
     * Update all customers, check the existence of duplicate customer, to create or update, and replicate all information.
     */
    private void updateAllCustomerData(ExternalCustomer externalCustomer, Customer customer, CustomerMatches customerMatches) {
        updateCustomerContactInfo(externalCustomer, customer);
        if (customerMatches.hasDuplicates()) {
            for (Customer duplicate : customerMatches.getDuplicates()) {
                createOrUpdateDuplicateCustomer(externalCustomer, duplicate);
            }
        }
        updateCustomerRelations(externalCustomer, customer);
        updateCustomerPreferredStore(externalCustomer, customer);
    }

    /**
     * there some case of duplicate customer for the same company, those cases need to be updated with same value
     */
    private void createOrUpdateDuplicateCustomer(ExternalCustomer externalCustomer, Customer duplicate) {
        duplicate = validateCustomerExistence(externalCustomer, duplicate);
        if(!externalCustomer.isCompany()){
            duplicate.setBonusPointsBalance(externalCustomer.getBonusPointsBalance());
        }
        if (duplicate.getInternalId() == null) {
            createCustomer(duplicate);
        } else {
            updateCustomer(duplicate);
        }
    }

    private void updateCustomerPreferredStore(ExternalCustomer externalCustomer, Customer customer) {
        customer.setPreferredStore(externalCustomer.getPreferredStore());
    }

    private void populateCustomerFields(ExternalCustomer externalCustomer, Customer customer) {
        if (externalCustomer.isCompany()) {
            customer.setCompanyNumber(externalCustomer.getCompanyNumber());
            customer.setCustomerType(CustomerType.COMPANY);
        } else {
            customer.setCustomerType(CustomerType.PERSON);
            customer.setBonusPointsBalance(externalCustomer.getBonusPointsBalance());
        }
    }

    private void updateCustomerContactInfo(ExternalCustomer externalCustomer, Customer customer) {
        customer.setAddress(externalCustomer.getPostalAddress());
    }

    private Customer updateCustomer(Customer customer) {
        return this.customerDataAccess.updateCustomerRecord(customer);
    }

    private Customer createCustomer(Customer customer) {
        return this.customerDataAccess.createCustomerRecord(customer);
    }

    private void updateCustomerRelations(ExternalCustomer externalCustomer, Customer customer) {
        List<ShoppingList> consumerShoppingLists = externalCustomer.getShoppingLists();
        for (ShoppingList consumerShoppingList : consumerShoppingLists) {
            this.customerDataAccess.updateCustomerShoppingList(customer, consumerShoppingList);
        }
    }

    /**
     * call the database object to load the Person Customer
     */
    private CustomerMatches loadPersonCustomer(ExternalCustomer externalCustomer) {
        final String externalId = externalCustomer.getExternalId();
        CustomerMatches customerMatches = customerDataAccess.loadPersonCustomer(externalId);
        validateLoadPersonCustomer(customerMatches, externalId);
        return customerMatches;
    }

    /**
     * Validate if the loaded data for Person Customer adhere to the rule
     * 1 - Different CustomerType, throw a ConflictException
     * 2 - Case externalId not match with the term, update the fields
     */
    private void validateLoadPersonCustomer(CustomerMatches customerMatches, String externalId) {
        if (customerMatches.getCustomer() != null) {
            if (!CustomerType.PERSON.equals(customerMatches.getCustomer().getCustomerType())) {
                throw new ConflictException("Existing customer for externalCustomer " + externalId + " already exists and is not a person");
            }
            if (!EXTERNAL_ID.equals(customerMatches.getMatchTerm())) {
                Customer customer = customerMatches.getCustomer();
                customer.setExternalId(externalId);
                customer.setMasterExternalId(externalId);
            }
        }
    }

    /**
     * call the database object to load the Company Customer
     */
    private CustomerMatches loadCompanyCustomer(ExternalCustomer externalCustomer) {
        final String externalId = externalCustomer.getExternalId();
        final String companyNumber = externalCustomer.getCompanyNumber();
        CustomerMatches customerMatches = customerDataAccess.loadCompanyCustomer(externalId, companyNumber);
        validateLoadCompanyCustomer(customerMatches, externalId, companyNumber);
        return customerMatches;
    }

    /**
     * Validate if the loaded data for Company Customer adhere to the rule
     * 1 - Different CustomerType and the customer not null, throw a ConflictException
     * 2 - Case EXTERNAL_ID match with the term, update the fields with "fillDuplicateCustomer"
     *      or case COMPANY_NUMBER match with the term, update the fields "fillNonDuplicateCustomer"
     */
    private void validateLoadCompanyCustomer(CustomerMatches customerMatches, String externalId, String companyNumber) {
        if (customerMatches.getCustomer() != null && !CustomerType.COMPANY.equals(customerMatches.getCustomer().getCustomerType())) {
            throw new ConflictException("Existing customer for externalCustomer " + externalId + " already exists and is not a company");
        }
        if (EXTERNAL_ID.equals(customerMatches.getMatchTerm())) {
            fillDuplicateCustomer(customerMatches, companyNumber);
        } else if (COMPANY_NUMBER.equals(customerMatches.getMatchTerm())) {
            fillNonDuplicateCustomer(customerMatches, externalId, companyNumber);
        }
    }

    private void fillNonDuplicateCustomer(CustomerMatches customerMatches, String externalId, String companyNumber) {
        String customerExternalId = customerMatches.getCustomer().getExternalId();
        if (customerExternalId != null && !externalId.equals(customerExternalId)) {
            throw new ConflictException("Existing customer for externalCustomer " + companyNumber + " doesn't match external id " + externalId + " instead found " + customerExternalId );
        }
        Customer customer = customerMatches.getCustomer();
        customer.setExternalId(externalId);
        customer.setMasterExternalId(externalId);
        customerMatches.addDuplicate(null);
    }

    private void fillDuplicateCustomer(CustomerMatches customerMatches, String companyNumber) {
        String customerCompanyNumber = customerMatches.getCustomer().getCompanyNumber();
        if (!companyNumber.equals(customerCompanyNumber)) {
            customerMatches.getCustomer().setMasterExternalId(null);
            customerMatches.addDuplicate(customerMatches.getCustomer());
            customerMatches.setCustomer(null);
            customerMatches.setMatchTerm(null);
        }
    }
}
