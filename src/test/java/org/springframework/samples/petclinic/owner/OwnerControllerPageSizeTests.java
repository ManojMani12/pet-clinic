package org.springframework.samples.petclinic.owner;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = OwnerController.class)
@TestPropertySource(properties = "petclinic.owners.pageSize=10")
class OwnerControllerPageSizeTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private OwnerRepository owners;

	@Test
	void shouldUseConfiguredPageSizeForOwnersList() throws Exception {
		Owner owner = new Owner();
		owner.setId(1);
		owner.setFirstName("George");
		owner.setLastName("Franklin");

		given(this.owners.findByLastNameStartingWith(any(String.class), any(org.springframework.data.domain.Pageable.class)))
				.willAnswer(invocation -> {
					org.springframework.data.domain.Pageable pageable = invocation.getArgument(1);
					return new org.springframework.data.domain.PageImpl<>(Collections.singletonList(owner),
							org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize()), 1);
				});

		this.mockMvc.perform(get("/owners")).andExpect(status().isOk()).andExpect(view().name("owners/ownersList"))
				.andExpect(model().attributeExists("currentPage", "totalPages", "totalItems", "listOwners"));

		org.mockito.Mockito.verify(this.owners).findByLastNameStartingWith(eq(""),
				org.mockito.ArgumentMatchers.argThat(pageable -> pageable.getPageSize() == 10));
	}

}
