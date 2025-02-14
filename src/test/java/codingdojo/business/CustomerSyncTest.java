package codingdojo.business;

import codingdojo.business.helpers.ExternalCustomerPrinter;
import codingdojo.business.helpers.FakeDatabase;
import codingdojo.entities.auxiliar.CustomerType;
import codingdojo.entities.internal.Address;
import codingdojo.entities.internal.Customer;
import codingdojo.entities.internal.ShoppingList;
import codingdojo.entities.message.ExternalCustomer;
import codingdojo.exceptions.ConflictException;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class CustomerSyncTest {

    /**
     * The external record already exists in the customer db, so no need to create it.
     * There is new data in some fields, which is merged in.
     */
    @Test
    public void syncCompanyByExternalId(){
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalCompany();
        externalCustomer.setExternalId(externalId);

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setExternalId(externalId);

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertFalse(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }

    /**
     * The external record already exists in the customer db, so no need to create it.
     * There is new data in some fields, which is merged in.
     * New field BonusPointBalance
     * will accept the customer, but ignore the BonusPointBalance
     */
    @Test
    public void syncCompanyByExternalIdWithBonus(){
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalCompanyWithBonus();
        externalCustomer.setExternalId(externalId);

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setExternalId(externalId);

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertFalse(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }

    /**
     * The external record already exists in the customer db, so no need to create it.
     * There is new data in some fields, which is merged in.
     * New field BonusPointBalance
     * will accept the customer with BonusPointBalance
     */
    @Test
    public void syncPrivatePersonByExternalId(){
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalPrivatePerson();
        externalCustomer.setExternalId(externalId);

        Customer customer = new Customer();
        customer.setCustomerType(CustomerType.PERSON);
        customer.setInternalId("67576");
        customer.setExternalId(externalId);
        customer.setBonusPointsBalance(2233);

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertFalse(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }



    @Test
    public void syncPrivatePersonByWithBonusExternalId(){
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalPrivatePersonWithBonus();
        externalCustomer.setExternalId(externalId);

        Customer customer = new Customer();
        customer.setCustomerType(CustomerType.PERSON);
        customer.setInternalId("67576");
        customer.setExternalId(externalId);
        customer.setBonusPointsBalance(1234);

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertFalse(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }


    @Test
    public void syncShoppingLists(){
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalCompany();
        externalCustomer.setExternalId(externalId);

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setExternalId(externalId);
        customer.setShoppingLists(List.of(new ShoppingList("eyeliner", "blusher")));

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertFalse(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }

    @Test
    public void syncNewCompanyCustomer(){

        ExternalCustomer externalCustomer = createExternalCompany();
        externalCustomer.setExternalId("12345");

        FakeDatabase db = new FakeDatabase();
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertTrue(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }

    @Test
    public void syncNewPrivateCustomer(){

        ExternalCustomer externalCustomer = createExternalPrivatePerson();
        externalCustomer.setExternalId("12345");

        FakeDatabase db = new FakeDatabase();
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertTrue(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }

    @Test
    public void conflictExceptionWhenExistingCustomerIsPerson() {
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalCompany();
        externalCustomer.setExternalId(externalId);

        Customer customer = new Customer();
        customer.setCustomerType(CustomerType.PERSON);
        customer.setInternalId("45435");
        customer.setExternalId(externalId);

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        Assertions.assertThrows(ConflictException.class, () -> {
            sut.syncWithDataLayer(externalCustomer);
        }, printAfterState(db, toAssert).toString());

        Approvals.verify(toAssert);
    }

    @Test
    public void syncByExternalIdButCompanyNumbersConflict(){
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalCompany();
        externalCustomer.setExternalId(externalId);

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setExternalId(externalId);
        customer.setCompanyNumber("000-3234");

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertTrue(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }


    @Test
    public void syncByCompanyNumber(){
        String companyNumber = "12345";

        ExternalCustomer externalCustomer = createExternalCompany();
        externalCustomer.setCompanyNumber(companyNumber);

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setCompanyNumber(companyNumber);
        customer.addShoppingList(new ShoppingList("eyeliner", "mascara", "blue bombe eyeshadow"));

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertFalse(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }

    @Test
    public void syncByCompanyNumberWithConflictingExternalId(){
        String companyNumber = "12345";

        ExternalCustomer externalCustomer = createExternalCompany();
        externalCustomer.setCompanyNumber(companyNumber);
        externalCustomer.setExternalId("45646");

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setCompanyNumber(companyNumber);
        customer.setExternalId("conflicting id");

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        Assertions.assertThrows(ConflictException.class, () -> {
            sut.syncWithDataLayer(externalCustomer);
        }, printAfterState(db, toAssert).toString());

        Approvals.verify(toAssert);
    }

    @Test
    public void conflictExceptionWhenExistingCustomerIsCompany() {
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalPrivatePerson();
        externalCustomer.setExternalId(externalId);

        Customer customer = new Customer();
        customer.setCustomerType(CustomerType.COMPANY);
        customer.setCompanyNumber("32423-342");
        customer.setInternalId("45435");
        customer.setExternalId(externalId);

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        Assertions.assertThrows(ConflictException.class, () -> {
            sut.syncWithDataLayer(externalCustomer);
        }, printAfterState(db, toAssert).toString());

        Approvals.verify(toAssert);
    }

    @Test
    public void syncCompanyByExternalIdWithNonMatchingMasterId(){
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalCompany();
        externalCustomer.setExternalId(externalId);

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setExternalId(externalId);
        customer.setName("company 1");

        Customer customer2 = new Customer();
        customer2.setCompanyNumber(externalCustomer.getCompanyNumber());
        customer2.setCustomerType(CustomerType.COMPANY);
        customer2.setInternalId("45435234");
        customer2.setMasterExternalId(externalId);
        customer2.setName("company 2");

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        db.addCustomer(customer2);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertFalse(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }



    private ExternalCustomer createExternalPrivatePerson() {
        ExternalCustomer externalCustomer = new ExternalCustomer();
        externalCustomer.setExternalId("12345");
        externalCustomer.setName("Joe Bloggs");
        externalCustomer.setBonusPointsBalance(2233);
        externalCustomer.setAddress(new Address("123 main st", "Stockholm", "SE-123 45"));
        externalCustomer.setPreferredStore("Nordstan");
        externalCustomer.setShoppingLists(List.of(new ShoppingList("lipstick", "foundation")));
        return externalCustomer;
    }

    private ExternalCustomer createExternalPrivatePersonWithBonus() {
        ExternalCustomer externalCustomer = new ExternalCustomer();
        externalCustomer.setExternalId("12345");
        externalCustomer.setName("Joe Bloggs");
        externalCustomer.setBonusPointsBalance(1234);
        externalCustomer.setAddress(new Address("123 main st", "Stockholm", "SE-123 45"));
        externalCustomer.setPreferredStore("Nordstan");
        externalCustomer.setShoppingLists(List.of(new ShoppingList("lipstick", "foundation")));

        return externalCustomer;
    }

    private ExternalCustomer createExternalCompany() {
        ExternalCustomer externalCustomer = new ExternalCustomer();
        externalCustomer.setExternalId("12345");
        externalCustomer.setName("Acme Inc.");
        externalCustomer.setAddress(new Address("123 main st", "Helsingborg", "SE-123 45"));
        externalCustomer.setCompanyNumber("470813-8895");
        externalCustomer.setShoppingLists(List.of(new ShoppingList("lipstick", "blusher")));
        return externalCustomer;
    }


    private ExternalCustomer createExternalCompanyWithBonus() {
        ExternalCustomer externalCustomer = new ExternalCustomer();
        externalCustomer.setExternalId("9912345");
        externalCustomer.setName("Acme Bonus Inc.");
        externalCustomer.setBonusPointsBalance(9999);
        externalCustomer.setAddress(new Address("123 main st", "Helsingborg", "SE-123 45"));
        externalCustomer.setCompanyNumber("470813-8895");
        externalCustomer.setShoppingLists(List.of(new ShoppingList("lipstick", "blusher")));
        return externalCustomer;
    }

    private Customer createCustomerWithSameCompanyAs(ExternalCustomer externalCustomer) {
        Customer customer = new Customer();
        customer.setCompanyNumber(externalCustomer.getCompanyNumber());
        customer.setCustomerType(CustomerType.COMPANY);
        customer.setInternalId("45435");
        return customer;
    }

    private StringBuilder printBeforeState(ExternalCustomer externalCustomer, FakeDatabase db) {
        StringBuilder toAssert = new StringBuilder();
        toAssert.append("BEFORE:\n");
        toAssert.append(db.printContents());

        toAssert.append("\nSYNCING THIS:\n");
        toAssert.append(ExternalCustomerPrinter.print(externalCustomer, ""));
        return toAssert;
    }

    private StringBuilder printAfterState(FakeDatabase db, StringBuilder toAssert) {
        toAssert.append("\nAFTER:\n");
        toAssert.append(db.printContents());
        return toAssert;
    }

}
