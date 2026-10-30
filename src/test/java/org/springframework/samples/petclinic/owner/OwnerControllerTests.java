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
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OwnerController.class)
class OwnerControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private OwnerRepository owners;

	@Test
	void testProcessFindFormSuccess() throws Exception {
		Owner owner1 = new Owner();
		owner1.setId(1);
		owner1.setFirstName("George");
		owner1.setLastName("Franklin");
		Owner owner2 = new Owner();
		owner2.setId(2);
		owner2.setFirstName("Betty");
		owner2.setLastName("Davis");

		Pageable pageable = PageRequest.of(0, 5);
		Page<Owner> page = new PageImpl<>(List.of(owner1, owner2), pageable, 2);
		when(this.owners.findByLastNameStartingWith(eq(""), Mockito.any(Pageable.class))).thenReturn(page);

		this.mockMvc.perform(get("/owners"))
				.andExpect(status().isOk())
				.andExpect(model().attributeExists("listOwners"))
				.andExpect(view().name("owners/ownersList"));
	}

	@Test
	void testProcessFindFormSuccessWithPagination() throws Exception {
		Owner owner1 = new Owner();
		owner1.setId(1);
		owner1.setFirstName("George");
		owner1.setLastName("Franklin");
		Owner owner2 = new Owner();
		owner2.setId(2);
		owner2.setFirstName("Betty");
		owner2.setLastName("Davis");

		Pageable pageable = PageRequest.of(1, 5);
		Page<Owner> page = new PageImpl<>(List.of(owner1, owner2), pageable, 4);
		when(this.owners.findByLastNameStartingWith(eq(""), Mockito.any(Pageable.class))).thenReturn(page);

		this.mockMvc.perform(get("/owners").param("page", "2"))
				.andExpect(status().isOk())
				.andExpect(model().attributeExists("listOwners"))
				.andExpect(model().attributeExists("totalPages"))
				.andExpect(model().attributeExists("currentPage"))
				.andExpect(view().name("owners/ownersList"));
	}

	@Test
	void shouldPreserveTrimmedLastNameAcrossPaginationModel() throws Exception {
		Owner owner1 = new Owner();
		owner1.setId(1);
		owner1.setFirstName("George");
		owner1.setLastName("Franklin");
		Owner owner2 = new Owner();
		owner2.setId(2);
		owner2.setFirstName("Helen");
		owner2.setLastName("Franklin");

		Pageable pageable = PageRequest.of(1, 5);
		Page<Owner> page = new PageImpl<>(List.of(owner1, owner2), pageable, 7);
		when(this.owners.findByLastNameStartingWith(eq("Fra"), Mockito.any(Pageable.class))).thenReturn(page);

		this.mockMvc.perform(get("/owners").param("lastName", "  Fra  ").param("page", "2"))
				.andExpect(status().isOk())
				.andExpect(model().attribute("currentLastName", "Fra"))
				.andExpect(view().name("owners/ownersList"));

		verify(this.owners).findByLastNameStartingWith(eq("Fra"), Mockito.any(Pageable.class));
	}

}
