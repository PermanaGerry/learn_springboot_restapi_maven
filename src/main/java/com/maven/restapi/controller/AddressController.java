package com.maven.restapi.controller;

import com.maven.restapi.dto.AddressResponse;
import com.maven.restapi.dto.CreateAddressRequest;
import com.maven.restapi.dto.UpdateAddressRequest;
import com.maven.restapi.dto.WebResponse;
import com.maven.restapi.models.entity.User;
import com.maven.restapi.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AddressController {

    @Autowired
    private AddressService addressService;

    @PostMapping(
            path = "/api/contacts/{contactId}/addresses",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> create(User user,@PathVariable("contactId") String contactId, @RequestBody CreateAddressRequest request) {
        request.setContactId(contactId);
        addressService.create(user, request);
        return WebResponse.<String>builder().data("Ok").build();
    }

    @GetMapping(
            path = "/api/contacts/{contactId}/addresses/{addressId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<AddressResponse> get(User user,
                                            @PathVariable("contactId") String contactId,
                                            @PathVariable("addressId") String addressId) {
        AddressResponse addressResponse = addressService.get(user, contactId, addressId);
        return WebResponse.<AddressResponse>builder().data(addressResponse).build();
    }

    @PutMapping(
            path = "/api/contacts/{contactId}/addresses/{addressId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<AddressResponse> update(User user,
                                               @PathVariable("contactId") String contactId,
                                               @PathVariable("addressId") String addressId,
                                               @RequestBody UpdateAddressRequest request) {
        request.setContactId(contactId);
        request.setAddressId(addressId);
        AddressResponse addressResponse = addressService.update(user, request);

        return WebResponse.<AddressResponse>builder().data(addressResponse).build();
    }

    @DeleteMapping(
            path = "/api/contacts/{contactId}/addresses/{addressId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> delete(User user,
                                      @PathVariable("contactId") String contactId,
                                      @PathVariable("addressId") String id) {
        addressService.delete(user, contactId, id);
        return WebResponse.<String>builder().data("Ok").build();
    }

    @GetMapping(
            path = "/api/contacts/{contactId}/addresses",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<AddressResponse>> list(User user, @PathVariable("contactId") String contactId) {
        List<AddressResponse> listResponses = addressService.list(user, contactId);
        return WebResponse.<List<AddressResponse>>builder()
                .data(listResponses)
                .build();
    }

}
