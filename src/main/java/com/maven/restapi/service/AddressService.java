package com.maven.restapi.service;

import com.maven.restapi.dto.*;
import com.maven.restapi.models.entity.Address;
import com.maven.restapi.models.entity.Contact;
import com.maven.restapi.models.entity.User;
import com.maven.restapi.models.repository.AddressRepository;
import com.maven.restapi.models.repository.ContactRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class AddressService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ValidationService validationService;

    public AddressResponse create(User user, CreateAddressRequest request) {
        validationService.validate(request);

        Contact contact = contactRepository.findFirstByUserAndId(user, request.getContactId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact is not found."));

        Address address = new Address();
        address.setId(UUID.randomUUID().toString());
        address.setContact(contact);
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setProvince(request.getProvince());
        address.setCountry(request.getCountry());
        address.setPostalCode(request.getPostalCode());

        addressRepository.save(address);

        return toResponseAddress(address);
    }

    @Transactional
    public AddressResponse get(User user, String contactId, String id) {
        Contact contact = contactRepository.findFirstByUserAndId(user, contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact is not found."));

        Address address = addressRepository.findFirstByContactAndId(contact, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address is not found."));

        return toResponseAddress(address);
    }

    @Transactional
    public AddressResponse update(User user, UpdateAddressRequest request) {
        validationService.validate(request);
        Contact contact = contactRepository.findFirstByUserAndId(user, request.getContactId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact is not found."));

        Address address = addressRepository.findFirstByContactAndId(contact, request.getAddressId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address is not found."));

        if(Objects.nonNull(request.getStreet())) {
            address.setStreet(request.getStreet());
        }

        if(Objects.nonNull(request.getCity())) {
            address.setCity(request.getCity());
        }

        if(Objects.nonNull(request.getProvince())) {
            address.setProvince(request.getProvince());
        }

        if(Objects.nonNull(request.getCountry())) {
            address.setCountry(request.getCountry());
        }

        if (Objects.nonNull(request.getPostalCode())) {
            address.setPostalCode(request.getPostalCode());
        }

        addressRepository.save(address);

        return toResponseAddress(address);
    }

    @Transactional
    public void delete(User user, String contactId, String id) {
        Contact contact = contactRepository.findFirstByUserAndId(user, contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact is not found."));

        Address address = addressRepository.findFirstByContactAndId(contact, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address is not found."));

        addressRepository.deleteById(address.getId());
    }

    @Transactional
    public List<AddressResponse> list(User user, String contactId) {
        Contact contact = contactRepository.findFirstByUserAndId(user, contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact is not found."));

        List<Address> addresses = addressRepository.findAllByContact(contact);

        return addresses.stream().map(this::toResponseAddress).toList();
    }

    private AddressResponse toResponseAddress(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .province(address.getProvince())
                .country(address.getCountry())
                .postalCode(address.getPostalCode())
                .build();
    }

}
