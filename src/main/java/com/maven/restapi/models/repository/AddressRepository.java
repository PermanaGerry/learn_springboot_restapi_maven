package com.maven.restapi.models.repository;

import com.maven.restapi.models.entity.Address;
import com.maven.restapi.models.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {

    Optional<Address> findFirstByContactAndId(Contact contact, String s);

    List<Address> findAllByContact(Contact contact);
}
