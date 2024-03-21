package codingdojo.business;

import codingdojo.data.CustomerDataLayer;
import codingdojo.entities.auxiliar.CustomerType;
import codingdojo.entities.internal.Address;
import codingdojo.entities.internal.Customer;
import codingdojo.entities.internal.ShoppingList;
import codingdojo.entities.message.ExternalCustomer;
import codingdojo.exceptions.ConflictException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomerSyncMockitoTest {
    @Test
    public void syncCompanyByExternalId(){
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalCompany();
        externalCustomer.setExternalId(externalId);

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setExternalId(externalId);
        customer.setAddress(new Address("123 main st", "Helsingborg", "SE-123 45"));

        CustomerDataLayer db = mock(CustomerDataLayer.class);
        when(db.findByExternalId(externalId)).thenReturn(customer);
        CustomerSync sut = new CustomerSync(db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        // ASSERT
        assertFalse(created);
        ArgumentCaptor<Customer> argument = ArgumentCaptor.forClass(Customer.class);
        verify(db, atLeastOnce()).updateCustomerRecord(argument.capture());
        Customer updatedCustomer = argument.getValue();
        assertEquals(externalCustomer.getName(), updatedCustomer.getName());
        assertEquals(externalCustomer.getExternalId(), updatedCustomer.getExternalId());
        assertNull(updatedCustomer.getMasterExternalId());
        assertEquals(externalCustomer.getCompanyNumber(), updatedCustomer.getCompanyNumber());
        assertEquals(externalCustomer.getPostalAddress(), updatedCustomer.getAddress());
        assertEquals(externalCustomer.getShoppingLists(), updatedCustomer.getShoppingLists());
        assertNull(updatedCustomer.getBonusPointsBalance());
        assertEquals(CustomerType.COMPANY, updatedCustomer.getCustomerType());
        assertNull(updatedCustomer.getPreferredStore());
    }


    @Test
    public void syncPersonByExternalId(){
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalPerson();
        externalCustomer.setExternalId(externalId);

        Customer customer = createCustomer(externalCustomer);
        customer.setExternalId(externalId);

        CustomerDataLayer db = mock(CustomerDataLayer.class);
        when(db.findByExternalId(externalId)).thenReturn(customer);
        CustomerSync sut = new CustomerSync(db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        // ASSERT
        assertFalse(created);
        ArgumentCaptor<Customer> argument = ArgumentCaptor.forClass(Customer.class);
        verify(db, atLeastOnce()).updateCustomerRecord(argument.capture());
        Customer updatedCustomer = argument.getValue();
        assertEquals(externalCustomer.getName(), updatedCustomer.getName());
        assertEquals(externalCustomer.getExternalId(), updatedCustomer.getExternalId());
        assertNull(updatedCustomer.getMasterExternalId());
        assertEquals(externalCustomer.getCompanyNumber(), updatedCustomer.getCompanyNumber());
        assertEquals(externalCustomer.getPostalAddress(), updatedCustomer.getAddress());
        assertEquals(externalCustomer.getShoppingLists(), updatedCustomer.getShoppingLists());
        assertEquals(externalCustomer.getBonusPointsBalance(), updatedCustomer.getBonusPointsBalance());

        assertEquals(CustomerType.PERSON, updatedCustomer.getCustomerType());
        assertNull(updatedCustomer.getPreferredStore());
    }

    @Test
    public void syncWrongPersonByExternalId(){
        String externalId = "12345";

        ExternalCustomer externalCustomer = createExternalWrongPerson();
        externalCustomer.setExternalId(externalId);

        Customer customer = createCustomer(externalCustomer);
        customer.setExternalId(externalId);

        CustomerDataLayer db = mock(CustomerDataLayer.class);
        when(db.findByExternalId(externalId)).thenReturn(customer);
        CustomerSync sut = new CustomerSync(db);

        // ACT
        try {
            boolean created = sut.syncWithDataLayer(externalCustomer);
        }catch (Exception ex){
            assertTrue(ex instanceof ConflictException);
        }

    }


    private ExternalCustomer createExternalCompany() {
        ExternalCustomer externalCustomer = new ExternalCustomer();
        externalCustomer.setExternalId("12345");
        externalCustomer.setName("Acme Inc.");
        externalCustomer.setBonusPointsBalance(1234);
        externalCustomer.setAddress(new Address("123 main st", "Helsingborg", "SE-123 45"));
        externalCustomer.setCompanyNumber("470813-8895");
        externalCustomer.setShoppingLists(List.of(new ShoppingList("lipstick", "blusher")));
        return externalCustomer;
    }

    private ExternalCustomer createExternalPerson() {
        ExternalCustomer externalCustomer = new ExternalCustomer();
        externalCustomer.setExternalId("12345");
        externalCustomer.setName("John John");
        externalCustomer.setBonusPointsBalance(12233);
        externalCustomer.setAddress(new Address("123 main st", "Helsingborg", "SE-123 45"));
        externalCustomer.setShoppingLists(List.of(new ShoppingList("lipstick", "blusher")));
        return externalCustomer;
    }

    private ExternalCustomer createExternalWrongPerson() {
        ExternalCustomer externalCustomer = new ExternalCustomer();
        externalCustomer.setExternalId("12345");
        externalCustomer.setName("John John");
        externalCustomer.setBonusPointsBalance(12233);
        externalCustomer.setCompanyNumber("470813-8895");
        externalCustomer.setAddress(new Address("123 main st", "Helsingborg", "SE-123 45"));
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

    private Customer createCustomer(ExternalCustomer externalCustomer) {
        Customer customer = new Customer();
        customer.setCompanyNumber(externalCustomer.getCompanyNumber());
        customer.setCustomerType(CustomerType.PERSON);
        customer.setInternalId("12345");
        return customer;
    }
}
