/*
 * Copyright 2012-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

/**
 * Test class for the {@link OwnerController}
 */
@WebMvcTest(controllers = OwnerController.class)
class OwnerControllerTests {

	private static final int TEST_PAGE_SIZE = 5;

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private OwnerRepository owners;

	@MockBean
	private VisitRepository visits;

	@Test
	void testShowOwner() throws Exception {
		given(this.owners.findById(1)).willReturn(new Owner());
		this.mockMvc.perform(get("/owners/1")).andExpect(status().isOk()).andExpect(model().attributeExists("owner"))
				.andExpect(view().name("owners/ownerDetails"));
	}

	@Test
	void testInitFindForm() throws Exception {
		this.mockMvc.perform(get("/owners/find")).andExpect(status().isOk()).andExpect(model().attributeExists("owner"))
				.andExpect(view().name("owners/findOwners"));
	}

	@Test
	void testProcessFindFormSuccess() throws Exception {
		Owner owner = new Owner();
		owner.setId(1);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		Page<Owner> page = new PageImpl<>(Collections.singletonList(owner));
		given(this.owners.findByLastNameStartingWith(eq("Franklin"), any(PageRequest.class))).willReturn(page);
		this.mockMvc.perform(get("/owners").param("lastName", "Franklin"))
				.andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:/owners/1"));
	}

	@Test
	void testProcessFindFormMultipleOwners() throws Exception {
		Owner owner = new Owner();
		owner.setId(1);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		Owner owner2 = new Owner();
		owner2.setId(2);
		owner2.setFirstName("Betty");
		owner2.setLastName("Davis");
		Page<Owner> page = new PageImpl<>(java.util.List.of(owner, owner2));
		given(this.owners.findByLastNameStartingWith(eq(""), any(PageRequest.class))).willReturn(page);
		this.mockMvc.perform(get("/owners")).andExpect(status().isOk())
				.andExpect(model().attributeExists("currentPage", "totalPages", "totalItems", "listOwners"))
				.andExpect(view().name("owners/ownersList"));
	}

	@Test
	void testProcessFindFormNoOwnersFound() throws Exception {
		Page<Owner> page = new PageImpl<>(Collections.emptyList());
		given(this.owners.findByLastNameStartingWith(eq("Franklin"), any(PageRequest.class))).willReturn(page);
		this.mockMvc.perform(get("/owners").param("lastName", "Franklin")).andExpect(status().isOk())
				.andExpect(model().attributeHasFieldErrors("owner", "lastName"))
				.andExpect(view().name("owners/findOwners"));
	}

	@Test
	void shouldPreserveLastNameAcrossPaginationLinks() throws Exception {
		Owner owner = new Owner();
		owner.setId(1);
		owner.setFirstName("George");
		owner.setLastName("Franklin");

		PageRequest pageable = PageRequest.of(0, TEST_PAGE_SIZE, Sort.by("lastName"));
		Page<Owner> page = new PageImpl<>(Collections.singletonList(owner), pageable, 6);

		given(this.owners.findByLastNameStartingWith(eq("Franklin"), any(PageRequest.class))).willReturn(page);

		this.mockMvc.perform(get("/owners").param("lastName", "Franklin"))
				.andExpect(status().isOk()).andExpect(view().name("owners/ownersList"))
				.andExpect(xpath("//a[contains(@href,'lastName=Franklin')]").exists());
	}

	@Test
	void shouldOmitLastNameFromPaginationLinksWhenNoFilter() throws Exception {
		Owner owner = new Owner();
		owner.setId(1);
		owner.setFirstName("George");
		owner.setLastName("Franklin");

		PageRequest pageable = PageRequest.of(0, TEST_PAGE_SIZE, Sort.by("lastName"));
		Page<Owner> page = new PageImpl<>(Collections.singletonList(owner), pageable, 6);

		given(this.owners.findByLastNameStartingWith(eq(""), any(PageRequest.class))).willReturn(page);

		this.mockMvc.perform(get("/owners")).andExpect(status().isOk()).andExpect(view().name("owners/ownersList"))
				.andExpect(xpath("//a[contains(@href,'lastName=')]").doesNotExist());
	}

	@Test
	void shouldUseCaseInsensitiveExactCityMatch() throws Exception {
		Owner owner = new Owner();
		owner.setId(1);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		owner.setCity("Madison");

		PageRequest pageable = PageRequest.of(0, TEST_PAGE_SIZE, Sort.by("lastName"));
		Page<Owner> page = new PageImpl<>(Collections.singletonList(owner), pageable, 1);

		given(this.owners.findByLastNameStartingWithAndCityIgnoreCase(eq("Franklin"), eq("madison"), any(PageRequest.class)))
				.willReturn(page);

		this.mockMvc.perform(get("/owners").param("lastName", "Franklin").param("city", "madison"))
				.andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:/owners/1"));

		verify(this.owners).findByLastNameStartingWithAndCityIgnoreCase(eq("Franklin"), eq("madison"), any(PageRequest.class));
	}

	@Test
	void shouldCollapseCityWhitespaceBeforeSearch() throws Exception {
		Owner owner = new Owner();
		owner.setId(1);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		owner.setCity("New York");

		PageRequest pageable = PageRequest.of(0, TEST_PAGE_SIZE, Sort.by("lastName"));
		Page<Owner> page = new PageImpl<>(Collections.singletonList(owner), pageable, 1);

		given(this.owners.findByLastNameStartingWithAndCityIgnoreCase(eq("Franklin"), eq("New York"), any(PageRequest.class)))
				.willReturn(page);

		this.mockMvc.perform(get("/owners").param("lastName", "Franklin").param("city", "New   York"))
				.andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:/owners/1"));

		verify(this.owners).findByLastNameStartingWithAndCityIgnoreCase(eq("Franklin"), eq("New York"), any(PageRequest.class));
	}

	@Test
	void testInitCreationForm() throws Exception {
		this.mockMvc.perform(get("/owners/new")).andExpect(status().isOk())
				.andExpect(model().attributeExists("owner"))
				.andExpect(view().name("owners/createOrUpdateOwnerForm"));
	}

	@Test
	void testProcessCreationFormSuccess() throws Exception {
		this.mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/owners/new")
				.param("firstName", "Joe").param("lastName", "Black").param("address", "Wall Street")
				.param("city", "New York").param("telephone", "1234567890"))
				.andExpect(status().is3xxRedirection());
	}

	@Test
	void testProcessCreationFormHasErrors() throws Exception {
		this.mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/owners/new")
				.param("firstName", "Joe").param("lastName", "").param("address", "Wall Street")
				.param("city", "New York").param("telephone", ""))
				.andExpect(status().isOk()).andExpect(model().attributeHasErrors("owner"))
				.andExpect(view().name("owners/createOrUpdateOwnerForm"));
	}

	@Test
	void testInitUpdateOwnerForm() throws Exception {
		given(this.owners.findById(anyInt())).willReturn(new Owner());
		this.mockMvc.perform(get("/owners/1/edit")).andExpect(status().isOk())
				.andExpect(model().attributeExists("owner"))
				.andExpect(view().name("owners/createOrUpdateOwnerForm"));
	}

	@Test
	void testProcessUpdateOwnerFormSuccess() throws Exception {
		this.mockMvc
				.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/owners/1/edit")
						.param("firstName", "Joe").param("lastName", "Black").param("address", "Wall Street")
						.param("city", "New York").param("telephone", "1234567890"))
				.andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Test
	void testProcessUpdateOwnerFormHasErrors() throws Exception {
		this.mockMvc
				.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/owners/1/edit")
						.param("firstName", "Joe").param("lastName", "").param("address", "Wall Street")
						.param("city", "New York").param("telephone", ""))
				.andExpect(status().isOk()).andExpect(model().attributeHasErrors("owner"))
				.andExpect(view().name("owners/createOrUpdateOwnerForm"));
	}

	@Test
	void testProcessFindFormMultipleOwnersWithPageParam() throws Exception {
		Owner owner = new Owner();
		owner.setId(1);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		Owner owner2 = new Owner();
		owner2.setId(2);
		owner2.setFirstName("Betty");
		owner2.setLastName("Davis");
		Page<Owner> page = new PageImpl<>(java.util.List.of(owner, owner2));
		given(this.owners.findByLastNameStartingWith(eq(""), any(Pageable.class))).willReturn(page);
		this.mockMvc.perform(get("/owners").param("page", "1")).andExpect(status().isOk())
				.andExpect(model().attributeExists("currentPage", "totalPages", "totalItems", "listOwners"))
				.andExpect(view().name("owners/ownersList"));
	}

	@Test
	void testProcessFindFormWhitespacesTrimmedInLastName() throws Exception {
		Owner owner = new Owner();
		owner.setId(1);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		Page<Owner> page = new PageImpl<>(Collections.singletonList(owner));
		given(this.owners.findByLastNameStartingWith(eq("Franklin"), any(PageRequest.class))).willReturn(page);
		this.mockMvc.perform(get("/owners").param("lastName", "Fran kli n"))
				.andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:/owners/1"));
	}

	@Test
	void testProcessFindFormMultipleOwnersWithPageParamAndLastNameFilter() throws Exception {
		Owner owner = new Owner();
		owner.setId(1);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		Owner owner2 = new Owner();
		owner2.setId(2);
		owner2.setFirstName("Betty");
		owner2.setLastName("Franklinson");
		Page<Owner> page = new PageImpl<>(java.util.List.of(owner, owner2));
		given(this.owners.findByLastNameStartingWith(eq("Franklin"), any(Pageable.class))).willReturn(page);
		this.mockMvc.perform(get("/owners").param("page", "1").param("lastName", "Franklin"))
				.andExpect(status().isOk()).andExpect(model().attributeExists("currentPage", "totalPages", "totalItems", "listOwners"))
				.andExpect(view().name("owners/ownersList"));
	}

	@Test
	void testFindOwnerListPageHasPagination() throws Exception {
		Owner owner = new Owner();
		owner.setId(1);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		PageRequest pageable = PageRequest.of(0, 5);
		Page<Owner> page = new PageImpl<>(Collections.singletonList(owner), pageable, 6);
		given(this.owners.findByLastNameStartingWith(anyString(), any(Pageable.class))).willReturn(page);
		this.mockMvc.perform(get("/owners").param("page", "1"))
				.andExpect(status().isOk())
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Pages:")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Next")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Last")));
	}

	@Test
	void testFindOwnerListPagePaginationWithOutOfBoundPageNumber() throws Exception {
		Owner owner = new Owner();
		owner.setId(1);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		PageRequest pageable = PageRequest.of(0, 5);
		Page<Owner> page = new PageImpl<>(Collections.singletonList(owner), pageable, 6);
		given(this.owners.findByLastNameStartingWith(anyString(), any(Pageable.class))).willReturn(page);
		this.mockMvc.perform(get("/owners").param("page", "10"))
				.andExpect(status().isOk())
				.andExpect(model().attribute("currentPage", 1))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Pages:")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Next")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Last")));
	}

}
