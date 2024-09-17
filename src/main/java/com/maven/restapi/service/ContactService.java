package com.maven.restapi.service;

import com.maven.restapi.dto.ContactResponse;
import com.maven.restapi.dto.CreateContactRequest;
import com.maven.restapi.dto.SearchContactRequest;
import com.maven.restapi.dto.UpdateContactRequest;
import com.maven.restapi.models.entity.Contact;
import com.maven.restapi.models.entity.User;
import com.maven.restapi.models.repository.ContactRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ValidationService validationService;

    @Transactional
    public ContactResponse create(User user, CreateContactRequest request) {
        validationService.validate(request);

        Contact contact = new Contact();
        contact.setId(UUID.randomUUID().toString());
        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());
        contact.setUser(user);

        contactRepository.save(contact);

        return toResponseContact(contact);
    }

    @Transactional
    public ContactResponse get(User user, String Id) {
        Contact contact = contactRepository.findFirstByUserAndId(user, Id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found."));

        return toResponseContact(contact);
    }

    @Transactional
    public ContactResponse update(User user, UpdateContactRequest request) {
        validationService.validate(request);

        Contact contact = contactRepository.findFirstByUserAndId(user, request.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found."));

        if(Objects.nonNull(request.getFirstName())) {
            contact.setFirstName(request.getFirstName());
        }

        if(Objects.nonNull(request.getLastName())) {
            contact.setLastName(request.getLastName());
        }

        if (Objects.nonNull(request.getEmail())) {
            contact.setEmail(request.getEmail());
        }

        if (Objects.nonNull(request.getPhone())) {
            contact.setPhone(request.getPhone());
        }

        contact.setUser(user);

        contactRepository.save(contact);

        return toResponseContact(contact);
    }

    @Transactional
    public void delete(User user, String contactId) {
        Contact contact = contactRepository.findFirstByUserAndId(user, contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found."));

        contactRepository.delete(contact);
    }

    @Transactional
    public Page<ContactResponse> search(User user, SearchContactRequest request) {
        Specification<Contact> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("user"), user));
            if (Objects.nonNull(request.getName())) {
                predicates.add(builder.or(
                        builder.like(root.get("firstName"), "%"+request.getName()+"%"),
                        builder.like(root.get("lastName"), "%"+request.getName()+"%")
                ));
            }
            if (Objects.nonNull(request.getEmail())) {
                predicates.add(
                        builder.like(root.get("email"), "%"+request.getEmail()+"%")
                );
            }
            if (Objects.nonNull(request.getPhone())) {
                predicates.add(
                        builder.like(root.get("phone"), "%"+request.getPhone()+"%")
                );
            }

            return query.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<Contact> contacts = contactRepository.findAll(specification, pageable);
        List<ContactResponse> contactResponses = contacts.getContent().stream()
                .map(this::toResponseContact)
                .toList();

        return new PageImpl<>(contactResponses, pageable, contacts.getTotalElements());
    }

    private ContactResponse toResponseContact(Contact contact) {
        return ContactResponse.builder()
                .id(contact.getId())
                .firstName(contact.getFirstName())
                .lastName(contact.getLastName())
                .email(contact.getEmail())
                .phone(contact.getPhone())
                .build();
    }
}
