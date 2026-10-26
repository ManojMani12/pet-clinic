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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(OwnerController.class)
@TestPropertySource(properties = "petclinic.owners.pageSize=10")
class OwnerControllerPageSizeTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private OwnerRepository owners;

	@Test
	void testProcessFindFormPageSize() throws Exception {
		List<Owner> ownerList = new ArrayList<>();
		for (int i = 0; i < 25; i++) {
			Owner owner = new Owner();
			owner.setId(i);
			owner.setFirstName("George");
			owner.setLastName("Franklin");
			ownerList.add(owner);
		}
		Page<Owner> ownersResults = new PageImpl<>(ownerList, PageRequest.of(0, 10), 25);
		given(this.owners.findByLastNameStartingWith(eq(""), any())).willReturn(ownersResults);

		mockMvc.perform(get("/owners"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("totalPages", 3));
	}

	@Test
	void testProcessFindFormInvalidPageSizeFallsBackToDefault() throws Exception {
		List<Owner> ownerList = new ArrayList<>();
		for (int i = 0; i < 25; i++) {
			Owner owner = new Owner();
			owner.setId(i);
			owner.setFirstName("George");
			owner.setLastName("Franklin");
			ownerList.add(owner);
		}
		Page<Owner> ownersResults = new PageImpl<>(ownerList, PageRequest.of(0, 5), 25);
		given(this.owners.findByLastNameStartingWith(eq(""), any())).willReturn(ownersResults);

		mockMvc.perform(get("/owners"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(model().attribute("totalPages", 5));
	}

	@TestConfiguration
	static class InvalidPageSizeConfig {
		@Bean
		@Primary
		OwnerController ownerController(OwnerRepository ownerRepository) {
			return new OwnerController(ownerRepository, 0);
		}
	}

}
