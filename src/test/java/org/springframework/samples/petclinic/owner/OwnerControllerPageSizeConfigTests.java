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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OwnerController.class)
class OwnerControllerPageSizeConfigTests {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	OwnerRepository owners;

	@TestConfiguration
	static class Config {

		@Bean
		OwnerRepository owners() {
			return Mockito.mock(OwnerRepository.class);
		}

		@Bean
		OwnerController ownerController(OwnerRepository owners) {
			return new OwnerController(owners, 10);
		}

	}

	@Test
	void shouldUseConfiguredPageSize() throws Exception {
		Owner owner = new Owner();
		owner.setId(1);
		owner.setFirstName("George");
		owner.setLastName("Franklin");

		Pageable pageable = PageRequest.of(0, 10);
		Page<Owner> page = new PageImpl<>(List.of(owner), pageable, 2);
		when(this.owners.findByLastNameStartingWith(eq(""), Mockito.any(Pageable.class))).thenReturn(page);

		this.mockMvc.perform(get("/owners"))
				.andExpect(status().is3xxRedirection());

		verify(this.owners).findByLastNameStartingWith(eq(""), Mockito.argThat(p -> p.getPageSize() == 10));
	}

}
