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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(OwnerController.class)
class OwnerControllerTests {

	private static final int TEST_PAGE_SIZE = 5;

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private OwnerRepository owners;

	@Test
	void testShowOwnersList() throws Exception {
		BDDMockito.given(this.owners.findByLastNameStartingWithAndCity(Mockito.eq(""), Mockito.eq(""), Mockito.any()))
				.willReturn(Page.empty());

		this.mockMvc.perform(get("/owners"))
				.andExpect(status().isOk())
				.andExpect(view().name("owners/findOwners"));
	}

	@Test
	void testProcessFindFormSuccess() throws Exception {
		Owner george = new Owner();
		george.setId(1);
		george.setFirstName("George");
		george.setLastName("Franklin");
		george.setAddress("110 W. Liberty St.");
		george.setCity("Madison");
		george.setTelephone("6085551023");
		Page<Owner> page = new PageImpl<>(List.of(george), PageRequest.of(0, TEST_PAGE_SIZE), 1);

		BDDMockito.given(this.owners.findByLastNameStartingWithAndCity(Mockito.eq("Franklin"), Mockito.eq("Madison"),
				Mockito.any(Pageable.class))).willReturn(page);

		this.mockMvc.perform(get("/owners").param("lastName", "Franklin").param("city", "Madison"))
				.andExpect(status().isOk())
				.andExpect(view().name("owners/ownersList"))
				.andExpect(model().attributeExists("listOwners"))
				.andExpect(model().attribute("lastName", "Franklin"))
				.andExpect(model().attribute("city", "Madison"));
	}

	@Test
	void shouldEscapeLikeWildcardsInLastName() throws Exception {
		Page<Owner> empty = new PageImpl<>(List.of(new Owner()), PageRequest.of(0, TEST_PAGE_SIZE), 1);
		ArgumentCaptor<String> lastNameCaptor = ArgumentCaptor.forClass(String.class);

		BDDMockito.given(this.owners.findByLastNameStartingWithAndCity(lastNameCaptor.capture(), Mockito.eq(""),
				Mockito.any(Pageable.class))).willReturn(empty);

		this.mockMvc.perform(get("/owners").param("lastName", "Sm%_th"))
				.andExpect(status().isOk())
				.andExpect(view().name("owners/ownersList"));

		assertThat(lastNameCaptor.getValue()).isEqualTo("Sm\\%\\_th");
	}

	@Test
	void shouldOmitBlankCityFromModel() throws Exception {
		Owner owner = new Owner();
		owner.setId(1);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		owner.setCity("Madison");
		Page<Owner> page = new PageImpl<>(List.of(owner), PageRequest.of(0, TEST_PAGE_SIZE), 1);

		BDDMockito.given(this.owners.findByLastNameStartingWithAndCity(Mockito.eq("Franklin"), Mockito.eq(""),
				Mockito.any(Pageable.class))).willReturn(page);

		this.mockMvc.perform(get("/owners").param("lastName", "Franklin").param("city", "   "))
				.andExpect(status().isOk())
				.andExpect(view().name("owners/ownersList"))
				.andExpect(model().attribute("lastName", "Franklin"))
				.andExpect(model().attributeDoesNotExist("city"));
	}

	@Test
	void shouldShowSearchContextWhenOnlyCityProvided() throws Exception {
		Owner owner = new Owner();
		owner.setId(1);
		owner.setFirstName("George");
		owner.setLastName("Franklin");
		owner.setCity("Madison");
		Page<Owner> page = new PageImpl<>(List.of(owner), PageRequest.of(0, TEST_PAGE_SIZE), 1);

		BDDMockito.given(this.owners.findByLastNameStartingWithAndCity(Mockito.eq(""), Mockito.eq("Madison"),
				Mockito.any(Pageable.class))).willReturn(page);

		this.mockMvc.perform(get("/owners").param("city", "Madison"))
				.andExpect(status().isOk())
				.andExpect(view().name("owners/ownersList"))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("city=Madison")))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("Back to Search")));
	}

	@Test
	void shouldKeepCityInModelOnNotFound() throws Exception {
		BDDMockito.given(this.owners.findByLastNameStartingWithAndCity(Mockito.eq("Franklin"), Mockito.eq("Nowhere"),
				Mockito.any(Pageable.class))).willReturn(Page.empty());

		this.mockMvc.perform(get("/owners").param("lastName", "Franklin").param("city", "Nowhere"))
				.andExpect(status().isOk())
				.andExpect(view().name("owners/findOwners"))
				.andExpect(model().attribute("city", "Nowhere"));
	}

}
