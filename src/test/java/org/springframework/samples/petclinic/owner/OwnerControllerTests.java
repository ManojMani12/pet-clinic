/*
 * Copyright 2012-2025 the original author or authors.
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

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Test class for the {@link OwnerController}
 */
@WebMvcTest(OwnerController.class)
class OwnerControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private OwnerRepository owners;

	@Test
	void testInitFindForm() throws Exception {
		mockMvc.perform(get("/owners/find")).andExpect(status().isOk()).andExpect(view().name("owners/findOwners"));
	}

	@Test
	void testProcessFindFormSuccess() throws Exception {
		Owner owner = new Owner();
		owner.setId(1);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		Page<Owner> ownersResults = new PageImpl<>(Collections.singletonList(owner));
		given(this.owners.findByLastNameStartingWith(eq("Franklin"), any(Pageable.class))).willReturn(ownersResults);

		mockMvc.perform(get("/owners").param("lastName", "Franklin"))
			.andExpect(status().is3xxRedirection()).andExpect(view().name("redirect:/owners/1"));
	}

	@Test
	void testProcessFindFormByLastNamePagingPreservesFilter() throws Exception {
		Owner owner = new Owner();
		owner.setId(1);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		Page<Owner> ownersResults = new PageImpl<>(Collections.singletonList(owner), PageRequest.of(0, 5), 6);
		given(this.owners.findByLastNameStartingWith(eq("Fra"), any(Pageable.class))).willReturn(ownersResults);

		mockMvc.perform(get("/owners").param("lastName", "Fra"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("searchLastName", "Fra"))
			.andExpect(content().string(Matchers.containsString("lastName=Fra")));
	}

	@Test
	void testProcessFindFormByLastNamePagingDoesNotAddFilterWhenBlank() throws Exception {
		Owner owner = new Owner();
		owner.setId(1);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		Page<Owner> ownersResults = new PageImpl<>(Collections.singletonList(owner), PageRequest.of(0, 5), 6);
		given(this.owners.findByLastNameStartingWith(eq(""), any(Pageable.class))).willReturn(ownersResults);

		mockMvc.perform(get("/owners").param("lastName", ""))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("searchLastName", ""))
			.andExpect(content().string(Matchers.not(Matchers.containsString("lastName="))));
	}

	@Test
	void testProcessFindFormByLastNamePagingTrimsWhitespace() throws Exception {
		Owner owner = new Owner();
		owner.setId(1);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		Page<Owner> ownersResults = new PageImpl<>(Collections.singletonList(owner), PageRequest.of(0, 5), 6);
		given(this.owners.findByLastNameStartingWith(eq("Fra"), any(Pageable.class))).willReturn(ownersResults);

		mockMvc.perform(get("/owners").param("lastName", "  Fra  "))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("searchLastName", "Fra"))
			.andExpect(content().string(Matchers.containsString("lastName=Fra")));
	}

	@Test
	void testProcessFindFormWithCityAndLastNameFilters() throws Exception {
		Owner owner = new Owner();
		owner.setId(1);
		owner.setFirstName("Betty");
		owner.setLastName("Davis");
		owner.setCity("Madison");

		Page<Owner> ownersResults = new PageImpl<>(Collections.singletonList(owner), PageRequest.of(0, 5), 6);
		given(this.owners.findByLastNameStartingWithAndCityIgnoreCase(eq("Davis"), eq("Madison"), any(Pageable.class)))
			.willReturn(ownersResults);

		mockMvc.perform(get("/owners").param("lastName", "Davis").param("city", "Madison"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("searchLastName", "Davis"))
			.andExpect(model().attribute("searchCity", "Madison"))
			.andExpect(content().string(Matchers.containsString("city=Madison")))
			.andExpect(content().string(Matchers.containsString("lastName=Davis")));
	}

	@Test
	void testProcessFindFormWithCityNoResultsReturnsFindOwnersWithNotFoundError() throws Exception {
		Page<Owner> ownersResults = Page.empty();
		given(this.owners.findByLastNameStartingWithAndCityIgnoreCase(eq(""), eq("NonexistentCity"), any(Pageable.class)))
			.willReturn(ownersResults);

		mockMvc.perform(get("/owners").param("city", "NonexistentCity"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/findOwners"))
			.andExpect(model().attributeHasFieldErrors("owner", "lastName"));
	}

	@Test
	void testShowOwner() throws Exception {
		given(this.owners.findById(1)).willReturn(new Owner());
		mockMvc.perform(get("/owners/1")).andExpect(status().isOk()).andExpect(model().attributeExists("owner"))
			.andExpect(view().name("owners/ownerDetails"));
	}

}
