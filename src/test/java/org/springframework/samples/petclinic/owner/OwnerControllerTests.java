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
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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
	void testProcessFindFormSuccess() throws Exception {
		given(this.owners.findByLastNameStartingWith(eq(""), any(PageRequest.class)))
			. willReturn(new PageImpl<>(Collections.emptyList()));

		mockMvc.perform(get("/owners"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/findOwners"));
	}

	@Test
	void testProcessFindFormByLastName() throws Exception {
		Page<Owner> page = new PageImpl<>(List.of(new Owner(), new Owner(), new Owner()), PageRequest.of(0, TEST_PAGE_SIZE), 10);
		given(this.owners.findByLastNameStartingWith(eq("Franklin"), any(PageRequest.class))).willReturn(page);

		mockMvc.perform(get("/owners").param("lastName", "Franklin").param("page", "1"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("lastName", "Franklin"))
			.andExpect(model().attribute("city", ""))
			.andExpect(model().attribute("currentPage", 1))
			.andExpect(model().attribute("totalPages", 4));
	}

	@Test
	void testProcessFindFormByCity() throws Exception {
		Page<Owner> page = new PageImpl<>(List.of(new Owner(), new Owner(), new Owner()), PageRequest.of(0, TEST_PAGE_SIZE), 10);
		given(this.owners.findByCityIgnoreCase(eq("Madison"), any(PageRequest.class))).willReturn(page);

		mockMvc.perform(get("/owners").param("city", "Madison").param("page", "1"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("lastName", ""))
			.andExpect(model().attribute("city", "Madison"))
			.andExpect(model().attribute("currentPage", 1))
			.andExpect(model().attribute("totalPages", 4));
	}

	@Test
	void testProcessFindFormByLastNameAndCity() throws Exception {
		Page<Owner> page = new PageImpl<>(List.of(new Owner(), new Owner(), new Owner()), PageRequest.of(0, TEST_PAGE_SIZE), 10);
		given(this.owners.findByLastNameStartingWithAndCityIgnoreCase(eq("Fr"), eq("Madison"), any(PageRequest.class))).willReturn(page);

		mockMvc.perform(get("/owners").param("lastName", "Fr").param("city", "Madison").param("page", "1"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("lastName", "Fr"))
			.andExpect(model().attribute("city", "Madison"))
			.andExpect(model().attribute("currentPage", 1))
			.andExpect(model().attribute("totalPages", 4));
	}

	@Test
	void testProcessFindFormNoOwnersFound() throws Exception {
		given(this.owners.findByLastNameStartingWith(eq("Unknown"), any(PageRequest.class)))
			. willReturn(new PageImpl<>(Collections.emptyList()));

		mockMvc.perform(get("/owners").param("lastName", "Unknown"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("owner", "lastName"))
			.andExpect(model().attributeFieldErrorCode("owner", "lastName", "notFound"))
			.andExpect(view().name("owners/findOwners"));
	}

	@Test
	void testProcessFindFormNoOwnersFoundByCity() throws Exception {
		given(this.owners.findByCityIgnoreCase(eq("NoSuchCity"), any(PageRequest.class)))
			.willReturn(new PageImpl<>(Collections.emptyList()));

		mockMvc.perform(get("/owners").param("city", "NoSuchCity"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("owner", "city"))
			.andExpect(model().attributeFieldErrorCode("owner", "city", "notFound"))
			.andExpect(view().name("owners/findOwners"));
	}

	@Test
	void testProcessFindFormRedirectToOwner() throws Exception {
		Owner owner = new Owner();
		owner.setId(1);
		Page<Owner> page = new PageImpl<>(List.of(owner), PageRequest.of(0, TEST_PAGE_SIZE), 1);
		given(this.owners.findByLastNameStartingWith(eq("McTavish"), any(PageRequest.class))).willReturn(page);

		mockMvc.perform(get("/owners").param("lastName", "McTavish"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/owners/1"));

		verify(this.owners, never()).findAll();
	}

	@Test
	void shouldRedirectToFirstPageWhenOutOfRangeAndRetainLastName() throws Exception {
		Page<Owner> page = new PageImpl<>(List.of(new Owner(), new Owner(), new Owner()), PageRequest.of(0, TEST_PAGE_SIZE), 6);
		given(this.owners.findByLastNameStartingWith(eq("Fr"), any(PageRequest.class))).willReturn(page);

		mockMvc.perform(get("/owners").param("lastName", "Fr").param("page", "99"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/owners?page=1&lastName=Fr"));
	}

	@Test
	void shouldRedirectToFirstPageWhenOutOfRangeAndRetainLastNameAndCity() throws Exception {
		Page<Owner> page = new PageImpl<>(List.of(new Owner(), new Owner(), new Owner()), PageRequest.of(0, TEST_PAGE_SIZE), 6);
		given(this.owners.findByLastNameStartingWithAndCityIgnoreCase(eq("Fr"), eq("Madison"), any(PageRequest.class)))
			.willReturn(page);

		mockMvc.perform(get("/owners").param("lastName", "Fr").param("city", "Madison").param("page", "99"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/owners?page=1&lastName=Fr&city=Madison"));
	}

	@Test
	void shouldReturnOwnersListForSubsequentPageWithLastNameAndCityFilter() throws Exception {
		Page<Owner> page2 = new PageImpl<>(List.of(new Owner(), new Owner()), PageRequest.of(1, TEST_PAGE_SIZE), 7);
		given(this.owners.findByLastNameStartingWithAndCityIgnoreCase(eq("Fr"), eq("Madison"), any(PageRequest.class)))
			.willReturn(page2);

		mockMvc.perform(get("/owners").param("lastName", "Fr").param("city", "Madison").param("page", "2"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("lastName", "Fr"))
			.andExpect(model().attribute("city", "Madison"))
			.andExpect(model().attribute("currentPage", 2))
			.andExpect(model().attribute("totalPages", 4));
	}

	@Test
	void shouldNotIncludeLastNameWhenBlank() throws Exception {
		given(this.owners.findByLastNameStartingWith(eq(""), any(PageRequest.class)))
			.willReturn(new PageImpl<>(List.of(new Owner(), new Owner()), PageRequest.of(0, TEST_PAGE_SIZE), 7));

		mockMvc.perform(get("/owners").param("lastName", " ").param("page", "2"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("lastName", ""));
	}

}
