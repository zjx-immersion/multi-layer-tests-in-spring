package com.tw.apistackbase.api.customer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tw.apistackbase.api.customer.response.ResourceWithUrl;
import com.tw.apistackbase.domian.address.model.Address;
import com.tw.apistackbase.domian.customer.model.Customer;
import com.tw.apistackbase.dto.AddressDto;
import com.tw.apistackbase.dto.CustomerDto;
import com.tw.apistackbase.service.AddressService;
import com.tw.apistackbase.service.CustomerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@WebMvcTest(CustomerResource.class)
@EnableSpringDataWebSupport
public class CustomerResourceTest {


    @Autowired
    private CustomerResource customerResource;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private AddressService addressService;


    @Autowired
    private ObjectMapper mapper;

    @Test
    public void should_get_all_customers_without_any_paramters() throws Exception {


        //given
        CustomerDto customerDto = new CustomerDto(1L, "Jason", "Zhong");
        CustomerDto customerDto2 = new CustomerDto(2L, "Jason2", "Zhong2");
        List<CustomerDto> customerDtos = Arrays.asList(customerDto, customerDto2);
        when(customerService.getCustomerByPage(any())).thenReturn(customerDtos);
        //when
        ResultActions result = mvc.perform(get("/customers"));
        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].content.id", is(1)))
                .andExpect(jsonPath("$[0].content.firstName", containsString("Jason")))
                .andExpect(jsonPath("$[0].content.lastName", is("Zhong")))
                .andExpect(jsonPath("$[1].content.id", is(2)));

    }

    @Test
    public void should_get_indicated_todo_by_id() throws Exception {

        //given
        long todoId = 1L;
        CustomerDto customerDto = new CustomerDto(todoId, "Jason", "Zhong");
        when(customerService.getById(todoId)).thenReturn(customerDto);
        //when
        ResultActions result = mvc.perform(get("/customers/{0}", todoId));
        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content.id", is(customerDto.getId().intValue())))
                .andExpect(jsonPath("$.content.firstName", containsString(customerDto.getFirstName())))
                .andExpect(jsonPath("$.content.lastName", is(customerDto.getLastName())));
    }

    @Test
    public void should_return_crated_when_post_to_crate_a_todo() throws Exception {

        //given
        Customer customer = new Customer("Jason", "Zhong");
        when(customerService.save(any(Customer.class))).thenReturn(true);
        //when
        ResultActions result = mvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(customer)));
        //then
        result.andExpect(status().isCreated())
                .andDo(print());

    }


    @Test
    public void should_return_a_bad_request_when_save_Failed() throws Exception {

        //given
        Customer customer = new Customer("Jason", "Zhong");
        when(customerService.save(any(Customer.class))).thenReturn(false);
        //when
        ResultActions result = mvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(customer)));
        //then
        result.andExpect(status().isBadRequest())
                .andDo(print());

    }

    @Test
    public void should_update_the_1L_id_todo_when_patch_the_customers_slash_id_with_body_json_customer() throws Exception {

        //given
        Customer customer = new Customer(1L, "Jason-updated", "Zhong-updated");
        when(customerService.updateById(anyLong(), any(Customer.class))).thenReturn(true);
        //when
        ResultActions result = mvc.perform
                (patch("/customers/{0}", customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(customer))
                );

        //then
        result.andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    public void should_delete_customer_when_call_http_delete_call_by_id() throws Exception {

        //given
        Customer customer = new Customer(1L, "Jason-updated", "Zhong-updated");
        when(customerService.deleteById(customer.getId())).thenReturn(true);
        //when
        ResultActions result = mvc.perform
                (delete("/customers/{0}", customer.getId()));

        //then
        result.andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void should_get_addressDto_list_and_200_status_when_get_addresses_by_customer() throws Exception {

        //given
        Long customerId = 1L;
        AddressDto addressDto1 = new AddressDto(1L, "Chengdu");
        AddressDto addressDto2 = new AddressDto(2L, "Zhuhai");
        List<AddressDto> addressDtos = Arrays.asList(addressDto1, addressDto2);
        when(customerService.getAddressesByCustomerId(customerId)).thenReturn(addressDtos);
        //when
        ResultActions result = mvc.perform
                (get("/customers/{0}/addresses", customerId));


        //then
        ResultActions resultActions = result.andExpect(status().isOk())
                .andDo(print());

        resultActions.andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].content.id", is(1)))
                .andExpect(jsonPath("$[1].content.id", is(2)))
                .andExpect(jsonPath("$[*].content.city", containsInAnyOrder("Chengdu", "Zhuhai")));

        List<ResourceWithUrl<AddressDto>> getedAddressDtos = mapper.readValue(resultActions.andReturn().getResponse().getContentAsString(),
                new TypeReference<List<ResourceWithUrl<AddressDto>>>() {
        });


        assertEquals(2, getedAddressDtos.size());
        assertEquals(new Long(1), getedAddressDtos.get(0).getContent().getId());

    }

    @Test
    public void should_get_id_1L_addressDto_and_200_status_when_get_address_by_indicated_customer_id() throws Exception {

        //given
        Long customerId = 1L;
        Long addressId = 1L;
        AddressDto addressDto = new AddressDto(customerId, "Chengdu");
        when(addressService.getByCustomerIdAndAddressId(customerId, addressId)).thenReturn(addressDto);
        //when
        ResultActions result = mvc.perform
                (get("/customers/{0}/addresses/{1}", customerId, addressId));

        //then
        result.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.content.id", is(addressId.intValue())));
    }

    @Test
    public void should_added_the_address_for_customer_when_post_address_to_customer_slashed_id() throws Exception {

        //given
        Long customerId = 1L;
        Address address = new Address(1L, "Chengdu");
        when(addressService.addAddress(anyLong(), any(Address.class))).thenReturn(true);
        //when
        ResultActions result = mvc.perform(post("/customers/{0}/addresses", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(address)));
        //then
        result.andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void modifyAddressByCustomerIdAndAddressId() throws Exception {

        //given
        Long customerId = 1L;
        Long addressId = 1L;
        Address address = new Address(1L, "Chengdu-new");
        when(addressService.modifyAddressByCustomerIdAndAddressId(anyLong(), anyLong(), any(Address.class))).thenReturn(true);
        //when
        ResultActions result = mvc.perform(patch("/customers/{0}/addresses/{1}", customerId, addressId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(address)));
        //then
        result.andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    public void deleteAddressByCustomerIdAndAddressId() throws Exception {

        //given
        Long customerId = 1L;
        Long addressId = 1L;
        AddressDto addressDto = new AddressDto(customerId, "Chengdu");
        when(addressService.deleteAddressByCustomerIdAndAddressId(customerId, addressId)).thenReturn(true);
        //when
        ResultActions result = mvc.perform
                (delete("/customers/{0}/addresses/{1}", customerId, addressId));

        //then
        result.andExpect(status().isNoContent())
                .andDo(print());
    }
}