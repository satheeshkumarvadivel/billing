//package com.satheesh.billing.dao;
//
//import java.util.List;
//
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.CrudRepository;
//import org.springframework.stereotype.Repository;
//
//import com.satheesh.billing.model.Customer;
//
//@Repository
//public interface CustomerRepository extends CrudRepository<Customer, Long> {
//
//	@Query("SELECT c FROM Customer c where is_active = true and lower(company_name) like %:search% OR lower(customer_name) like %:search% OR  lower(contact_number_1) like %:search% OR lower(contact_number_1) like %:search% OR  lower(address) like %:search% OR lower(email) like %:search%  order by company_name")
//	List<Customer> searchCustomer(String search);
//	
//	List<Customer> findByCustomerName(String name);
//
//}
