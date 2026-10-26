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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitController;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Test class for {@link OwnerController}
 */
@WebMvcTest(controllers = OwnerController.class)
@Import(VisitController.class)
class OwnerControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private OwnerRepository owners;

	@Test
	void testInitFindForm() throws Exception {
		mockMvc.perform(get("/owners/find")).andExpect(status().isOk()).andExpect(view().name("owners/findOwners"))
				.andExpect(model().attributeExists("owner"));
	}

	@Test
	void testProcessFindFormSuccess() throws Exception {
		Page<Owner> page = new PageImpl<>(List.of(new Owner(), new Owner()), PageRequest.of(0, 5), 2);
		when(owners.findByLastNameStartingWith(eq(""), any(Pageable.class))).thenReturn(page);

		mockMvc.perform(get("/owners")).andExpect(status().isOk()).andExpect(view().name("owners/ownersList"))
				.andExpect(model().attributeExists("listOwners"));
	}

	@Test
	void shouldPreserveLastNameFilterInPagingLinks() throws Exception {
		Page<Owner> page = new PageImpl<>(List.of(new Owner(), new Owner()), PageRequest.of(0, 5), 12);
		when(owners.findByLastNameStartingWith(eq("Frank"), any(Pageable.class))).thenReturn(page);

		mockMvc.perform(get("/owners").param("lastName", "Frank")).andExpect(status().isOk())
				.andExpect(view().name("owners/ownersList"))
				.andExpect(model().attribute("lastName", "Frank"))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("/owners?page=2&amp;lastName=Frank")));
	}

	@Test
	void shouldPreserveTrimmedLastNameInPagingLinks() throws Exception {
		Page<Owner> page = new PageImpl<>(List.of(new Owner(), new Owner()), PageRequest.of(0, 5), 12);
		when(owners.findByLastNameStartingWith(eq("Fran"), any(Pageable.class))).thenReturn(page);

		mockMvc.perform(get("/owners").param("lastName", "  Fran  ")).andExpect(status().isOk())
				.andExpect(view().name("owners/ownersList"))
				.andExpect(model().attribute("lastName", "Fran"))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("lastName=Fran")));
	}

	@Test
	void shouldReturnFindOwnersWhenNoResults() throws Exception {
		when(owners.findByLastNameStartingWith(eq("Zzzz"), any(Pageable.class)))
				.thenReturn(Page.empty(PageRequest.of(0, 5)));

		mockMvc.perform(get("/owners").param("lastName", "Zzzz")).andExpect(status().isOk())
				.andExpect(view().name("owners/findOwners"))
				.andExpect(model().attributeHasFieldErrors("owner", "lastName"));
	}

}
