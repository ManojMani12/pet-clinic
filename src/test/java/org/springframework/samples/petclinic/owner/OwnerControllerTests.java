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
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(OwnerController.class)
class OwnerControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private OwnerRepository owners;

	private Owner george() {
		Owner george = new Owner();
		george.setId(1);
		george.setFirstName("George");
		george.setLastName("Franklin");
		return george;
	}

	@Test
	void testInitFindForm() throws Exception {
		mockMvc.perform(get("/owners/find"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/findOwners"));
	}

	@Test
	void testProcessFindFormSuccess() throws Exception {
		Owner owner = george();
		Page<Owner> tasks = new PageImpl<>(List.of(owner));
		given(this.owners.findByLastNameStartingWith(eq(""), any(Pageable.class))).willReturn(tasks);

		mockMvc.perform(get("/owners?page=1"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/owners/1?page=1&lastName="));
	}

	@Test
	void testProcessFindFormNotFound() throws Exception {
		Page<Owner> tasks = new PageImpl<>(List.of());
		given(this.owners.findByLastNameStartingWith(eq(""), any(Pageable.class))).willReturn(tasks);

		mockMvc.perform(get("/owners?page=1"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("owner", "lastName"))
			.andExpect(model().attributeHasFieldErrorCode("owner", "lastName", "notFound"))
			.andExpect(view().name("owners/findOwners"));
	}

	@Test
	void testShowOwnersList() throws Exception {
		Owner owner = george();
		Page<Owner> tasks = new PageImpl<>(List.of(owner));
		given(this.owners.findByLastNameStartingWith(eq(""), any(Pageable.class))).willReturn(tasks);

		mockMvc.perform(get("/owners/page/1"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"));
	}

	@Test
	void processFindFormRedirectsOutOfBoundsPageToFirstPage() throws Exception {
		Owner owner = george();
		Page<Owner> tasks = new PageImpl<>(List.of(owner));
		given(this.owners.findByLastNameStartingWith(eq(""), any(Pageable.class))).willReturn(tasks);

		mockMvc.perform(get("/owners").param("page", "2"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/owners/1?page=1&lastName="));
	}

	@Test
	void processFindFormAddsLastNameToModelForPagination() throws Exception {
		Owner owner = george();
		Page<Owner> tasks = new PageImpl<>(List.of(owner, new Owner(), new Owner()));
		given(this.owners.findByLastNameStartingWith(eq("Fr"), any(Pageable.class))).willReturn(tasks);

		mockMvc.perform(get("/owners/page/1").param("lastName", "Fr"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("lastName", "Fr"))
			.andExpect(view().name("owners/ownersList"));
	}

	@Test
	void ownersListPaginationLinksContainLastNameAndPage() throws Exception {
		Owner owner = george();
		Page<Owner> tasks = new PageImpl<>(List.of(owner), Pageable.ofSize(5), 30);
		given(this.owners.findByLastNameStartingWith(eq("Fr"), any(Pageable.class))).willReturn(tasks);

		mockMvc.perform(get("/owners/page/1").param("lastName", "Fr"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("lastName=Fr")))
			.andExpect(content().string(containsString("page=2")))
			.andExpect(content().string(not(containsString("lastName=Nope"))));
	}

	@Test
	void processFindFormTrimsCity() throws Exception {
		Page<Owner> tasks = new PageImpl<>(List.of(george(), new Owner()));
		when(this.owners.findByLastNameStartingWithAndCityIgnoreCase(eq("Fr"), eq("Madison"), any(Pageable.class)))
			.thenReturn(tasks);
		mockMvc.perform(get("/owners?page=1").param("lastName", "Fr").param("city", "  Madison  "))
			.andExpect(status().isOk());
		verify(this.owners).findByLastNameStartingWithAndCityIgnoreCase(eq("Fr"), eq("Madison"), any(Pageable.class));
	}

	@Test
	void processFindFormBlankCityUsesLastNameOnlyQuery() throws Exception {
		Page<Owner> tasks = new PageImpl<>(List.of(george(), new Owner()));
		when(this.owners.findByLastNameStartingWith(eq("Fr"), any(Pageable.class))).thenReturn(tasks);
		mockMvc.perform(get("/owners?page=1").param("lastName", "Fr").param("city", "  "))
			.andExpect(status().isOk());
		verify(this.owners).findByLastNameStartingWith(eq("Fr"), any(Pageable.class));
		verify(this.owners, never()).findByLastNameStartingWithAndCityIgnoreCase(anyString(), anyString(), any(Pageable.class));
	}

	@Test
	void processFindFormRedirectKeepsCity() throws Exception {
		when(this.owners.findByLastNameStartingWithAndCityIgnoreCase(eq("Franklin"), eq("Madison"), any(Pageable.class)))
			.thenReturn(new PageImpl<>(List.of(george())));
		mockMvc.perform(get("/owners").param("page", "2").param("lastName", "Franklin").param("city", "Madison"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/owners?page=1&lastName=Franklin&city=Madison"));
	}

	@Test
	void processFindFormUsesDefaultPageSizeOfFive() throws Exception {
		Page<Owner> tasks = new PageImpl<>(List.of(george(), new Owner()));
		when(this.owners.findByLastNameStartingWith(eq(""), any(Pageable.class))).thenReturn(tasks);
		mockMvc.perform(get("/owners?page=1")).andExpect(status().isOk());
		verify(this.owners).findByLastNameStartingWith(eq(""), argThat(p -> p.getPageSize() == 5));
	}

	@Test
	void processFindFormCityOnlyNotFoundRejectsCityField() throws Exception {
		Page<Owner> tasks = new PageImpl<>(List.of());
		when(this.owners.findByCityIgnoreCase(eq("Madison"), any(Pageable.class))).thenReturn(tasks);

		mockMvc.perform(get("/owners?page=1").param("city", "Madison"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("owner", "city"))
			.andExpect(model().attributeHasFieldErrorCode("owner", "city", "notFound"))
			.andExpect(view().name("owners/findOwners"));
	}

	private static ArgumentMatcher<Pageable> pageSize(int expected) {
		return p -> p.getPageSize() == expected;
	}

}
