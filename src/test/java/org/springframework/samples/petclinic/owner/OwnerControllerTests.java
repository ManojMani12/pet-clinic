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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link OwnerController}
 *
 * @author Colin But
 * @author Wick Dynex
 */
@WebMvcTest(OwnerController.class)
@DisabledInNativeImage
@DisabledInAotMode
class OwnerControllerTests {

	private static final int TEST_OWNER_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	private Owner george() {
		Owner george = new Owner();
		george.setId(TEST_OWNER_ID);
		george.setFirstName("George");
		george.setLastName("Franklin");
		george.setAddress("110 W. Liberty St.");
		george.setCity("Madison");
		george.setTelephone("6085551023");
		Pet max = new Pet();
		PetType dog = new PetType();
		dog.setName("dog");
		max.setType(dog);
		max.setName("Max");
		max.setBirthDate(LocalDate.now());
		george.addPet(max);
		max.setId(1);
		return george;
	}

	@BeforeEach
	void setup() {

		Owner george = george();
		given(this.owners.findByLastNameStartingWith(eq("Franklin"), any(Pageable.class)))
			.willReturn(new PageImpl<>(List.of(george)));

		given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(george));
		Visit visit = new Visit();
		visit.setDate(LocalDate.now());
		george.getPet("Max").getVisits().add(visit);

	}

	@Test
	void initCreationForm() throws Exception {
		mockMvc.perform(get("/owners/new"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("owner"))
			.andExpect(view().name("owners/createOrUpdateOwnerForm"));
	}

	@Test
	void processCreationFormSuccess() throws Exception {
		mockMvc
			.perform(post("/owners/new").param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "123 Caramel Street")
				.param("city", "London")
				.param("telephone", "1316761638"))
			.andExpect(status().is3xxRedirection());
	}

	@Test
	void processCreationFormHasErrors() throws Exception {
		mockMvc
			.perform(post("/owners/new").param("firstName", "Joe").param("lastName", "Bloggs").param("city", "London"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("owner"))
			.andExpect(model().attributeHasFieldErrors("owner", "address"))
			.andExpect(model().attributeHasFieldErrors("owner", "telephone"))
			.andExpect(view().name("owners/createOrUpdateOwnerForm"));
	}

	@Test
	void processCreationFormRejectsCityLongerThanColumn() throws Exception {
		mockMvc
			.perform(post("/owners/new").param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "123 Caramel Street")
				.param("city", "C".repeat(81))
				.param("telephone", "1316761638"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("owner"))
			.andExpect(model().attributeHasFieldErrors("owner", "city"))
			.andExpect(model().attributeHasFieldErrorCode("owner", "city", "Size"))
			.andExpect(view().name("owners/createOrUpdateOwnerForm"));
	}

	@Test
	void processCreationFormRejectsAddressLongerThanColumn() throws Exception {
		mockMvc
			.perform(post("/owners/new").param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "A".repeat(256))
				.param("city", "London")
				.param("telephone", "1316761638"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("owner"))
			.andExpect(model().attributeHasFieldErrors("owner", "address"))
			.andExpect(model().attributeHasFieldErrorCode("owner", "address", "Size"))
			.andExpect(view().name("owners/createOrUpdateOwnerForm"));
	}

	@Test
	void initFindForm() throws Exception {
		mockMvc.perform(get("/owners/find"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("owner"))
			.andExpect(view().name("owners/findOwners"));
	}

	@Test
	void processFindFormSuccess() throws Exception {
		Page<Owner> tasks = new PageImpl<>(List.of(george(), new Owner()));
		when(this.owners.findByLastNameStartingWith(anyString(), any(Pageable.class))).thenReturn(tasks);
		mockMvc.perform(get("/owners?page=1")).andExpect(status().isOk()).andExpect(view().name("owners/ownersList"));
	}

	@Test
	void processFindFormRedirectsOutOfBoundsPageToFirstPage() throws Exception {
		for (int page : List.of(0, 2)) {
			mockMvc.perform(get("/owners").param("page", Integer.toString(page)).param("lastName", "Franklin"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/owners?page=1&lastName=Franklin"));
		}
	}

	@Test
	void processFindFormByLastName() throws Exception {
		Page<Owner> tasks = new PageImpl<>(List.of(george()));
		when(this.owners.findByLastNameStartingWith(eq("Franklin"), any(Pageable.class))).thenReturn(tasks);
		mockMvc.perform(get("/owners?page=1").param("lastName", "Franklin"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/" + TEST_OWNER_ID));
	}

	@Test
	void processFindFormIgnoresSurroundingWhitespace() throws Exception {
		Page<Owner> tasks = new PageImpl<>(List.of(george()));
		when(this.owners.findByLastNameStartingWith(eq("Franklin"), any(Pageable.class))).thenReturn(tasks);

		for (String lastName : List.of(" Franklin", "Franklin ", " Franklin ")) {
			mockMvc.perform(get("/owners?page=1").param("lastName", lastName))
				.andExpect(status().is3xxRedirection())
				.andExpect(view().name("redirect:/owners/" + TEST_OWNER_ID));
		}

		verify(this.owners, times(3)).findByLastNameStartingWith(eq("Franklin"), any(Pageable.class));
	}

	@Test
	void processFindFormWithWhitespaceOnlyLastNameReturnsAllOwners() throws Exception {
		Page<Owner> tasks = new PageImpl<>(List.of(george(), new Owner()));
		when(this.owners.findByLastNameStartingWith(eq(""), any(Pageable.class))).thenReturn(tasks);

		mockMvc.perform(get("/owners?page=1").param("lastName", "   "))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"));

		verify(this.owners).findByLastNameStartingWith(eq(""), any(Pageable.class));
	}

	private Page<Owner> multiPage(int page, int total) {
		return new PageImpl<>(List.of(george(), george()), org.springframework.data.domain.PageRequest.of(page - 1, 2),
				total);
	}

	@Test
	void paginationLinksPreserveTrimmedLastName() throws Exception {
		when(this.owners.findByLastNameStartingWith(eq("Fran klin"), any(Pageable.class))).thenReturn(multiPage(2, 6));

		mockMvc.perform(get("/owners").param("page", "2").param("lastName", "  Fran klin "))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"))
			.andExpect(content().string(containsString("href=\"/owners?page=1&amp;lastName=Fran%20klin\"")))
			.andExpect(content().string(containsString("href=\"/owners?page=3&amp;lastName=Fran%20klin\"")));
	}

	@Test
	void paginationLinksOmitBlankLastName() throws Exception {
		when(this.owners.findByLastNameStartingWith(eq(""), any(Pageable.class))).thenReturn(multiPage(2, 6));

		mockMvc.perform(get("/owners").param("page", "2").param("lastName", "   "))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("href=\"/owners?page=3\"")))
			.andExpect(content().string(not(containsString("lastName="))));
	}

	@Test
	void paginationLinksEncodeSpecialCharactersInLastName() throws Exception {
		when(this.owners.findByLastNameStartingWith(eq("O&B=+"), any(Pageable.class))).thenReturn(multiPage(1, 6));

		mockMvc.perform(get("/owners").param("lastName", "O&B=+"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("lastName=O%26B%3D%2B")));
	}

	@Test
	void processFindFormFiltersByCityCombinedWithLastName() throws Exception {
		when(this.owners.findByLastNameStartingWithAndCityIgnoreCase(eq("Fra"), eq("Madison"), any(Pageable.class)))
			.thenReturn(multiPage(1, 4));

		mockMvc.perform(get("/owners").param("lastName", "Fra").param("city", "  Madison "))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"));

		verify(this.owners).findByLastNameStartingWithAndCityIgnoreCase(eq("Fra"), eq("Madison"), any(Pageable.class));
		verify(this.owners, never()).findByLastNameStartingWith(anyString(), any(Pageable.class));
	}

	@Test
	void processFindFormWithCityOnlyMatchesAllLastNames() throws Exception {
		when(this.owners.findByLastNameStartingWithAndCityIgnoreCase(eq(""), eq("Madison"), any(Pageable.class)))
			.thenReturn(multiPage(1, 4));

		mockMvc.perform(get("/owners").param("city", "Madison"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/ownersList"));
	}

	@Test
	void processFindFormBlankCityKeepsLastNameOnlySearch() throws Exception {
		when(this.owners.findByLastNameStartingWith(eq("Franklin"), any(Pageable.class))).thenReturn(multiPage(1, 4));

		mockMvc.perform(get("/owners").param("lastName", "Franklin").param("city", "   "))
			.andExpect(status().isOk())
			.andExpect(content().string(not(containsString("city="))));

		verify(this.owners, never()).findByLastNameStartingWithAndCityIgnoreCase(anyString(), anyString(),
				any(Pageable.class));
	}

	@Test
	void paginationLinksPreserveLastNameAndCity() throws Exception {
		when(this.owners.findByLastNameStartingWithAndCityIgnoreCase(eq("Fra"), eq("New York"), any(Pageable.class)))
			.thenReturn(multiPage(2, 6));

		mockMvc.perform(get("/owners").param("page", "2").param("lastName", "Fra").param("city", " New York "))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("href=\"/owners?page=1&amp;lastName=Fra&amp;city=New%20York\"")))
			.andExpect(
					content().string(containsString("href=\"/owners?page=3&amp;lastName=Fra&amp;city=New%20York\"")));
	}

	@Test
	void processFindFormRedirectPreservesCity() throws Exception {
		when(this.owners.findByLastNameStartingWithAndCityIgnoreCase(eq("Fra"), eq("Madison"), any(Pageable.class)))
			.thenReturn(multiPage(1, 2));

		mockMvc.perform(get("/owners").param("page", "5").param("lastName", "Fra").param("city", "Madison"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/owners?page=1&lastName=Fra&city=Madison"));
	}

	@Test
	void processFindFormByCityNoOwnersFound() throws Exception {
		when(this.owners.findByLastNameStartingWithAndCityIgnoreCase(eq(""), eq("Atlantis"), any(Pageable.class)))
			.thenReturn(new PageImpl<>(List.of()));

		mockMvc.perform(get("/owners").param("city", "Atlantis"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrorCode("owner", "lastName", "notFound"))
			.andExpect(view().name("owners/findOwners"))
			.andExpect(content().string(containsString("value=\"Atlantis\"")));
	}

	@Test
	void initFindFormShowsCityInput() throws Exception {
		mockMvc.perform(get("/owners/find"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("name=\"city\"")));
	}

	@Test
	void defaultPageSizeIsFiveAndNotAddedToLinks() throws Exception {
		when(this.owners.findByLastNameStartingWith(eq(""), any(Pageable.class))).thenReturn(multiPage(1, 12));

		mockMvc.perform(get("/owners"))
			.andExpect(status().isOk())
			.andExpect(content().string(not(containsString("size="))));

		verify(this.owners).findByLastNameStartingWith(eq(""), argThat(p -> p.getPageSize() == 5));
	}

	@Test
	void requestedPageSizeIsUsedAndPreservedInPaginationLinks() throws Exception {
		when(this.owners.findByLastNameStartingWithAndCityIgnoreCase(eq("Fra"), eq("Madison"), any(Pageable.class)))
			.thenReturn(multiPage(2, 30));

		mockMvc.perform(
				get("/owners").param("page", "2").param("size", "10").param("lastName", "Fra").param("city", "Madison"))
			.andExpect(status().isOk())
			.andExpect(content()
				.string(containsString("href=\"/owners?page=3&amp;lastName=Fra&amp;city=Madison&amp;size=10\"")));

		verify(this.owners).findByLastNameStartingWithAndCityIgnoreCase(eq("Fra"), eq("Madison"),
				argThat(p -> p.getPageSize() == 10));
	}

	@Test
	void invalidPageSizesFallBackToDefault() throws Exception {
		when(this.owners.findByLastNameStartingWith(eq(""), any(Pageable.class))).thenReturn(multiPage(1, 12));

		for (String size : List.of("0", "-3", "abc", "", "2.5", "99999999999")) {
			mockMvc.perform(get("/owners").param("size", size))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("size="))));
		}

		verify(this.owners, times(6)).findByLastNameStartingWith(eq(""), argThat(p -> p.getPageSize() == 5));
	}

	@Test
	void tooLargePageSizeIsClampedToMaximum() throws Exception {
		when(this.owners.findByLastNameStartingWith(eq(""), any(Pageable.class))).thenReturn(multiPage(1, 500));

		mockMvc.perform(get("/owners").param("size", "1000"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("page=2&amp;size=100\"")));

		verify(this.owners).findByLastNameStartingWith(eq(""), argThat(p -> p.getPageSize() == 100));
	}

	@Test
	void outOfRangeRedirectPreservesFiltersAndPageSize() throws Exception {
		when(this.owners.findByLastNameStartingWithAndCityIgnoreCase(eq("Fra"), eq("Madison"), any(Pageable.class)))
			.thenReturn(multiPage(1, 2));

		mockMvc.perform(
				get("/owners").param("page", "9").param("size", "10").param("lastName", "Fra").param("city", "Madison"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/owners?page=1&lastName=Fra&city=Madison&size=10"));
	}

	@Test
	void emptyResultsWithPageSizeShowFindForm() throws Exception {
		when(this.owners.findByLastNameStartingWith(eq("Nobody"), any(Pageable.class)))
			.thenReturn(new PageImpl<>(List.of()));

		mockMvc.perform(get("/owners").param("lastName", "Nobody").param("size", "10"))
			.andExpect(status().isOk())
			.andExpect(view().name("owners/findOwners"));
	}

	@Test
	void processFindFormNoOwnersFound() throws Exception {
		Page<Owner> tasks = new PageImpl<>(List.of());
		when(this.owners.findByLastNameStartingWith(eq("Unknown Surname"), any(Pageable.class))).thenReturn(tasks);
		mockMvc.perform(get("/owners?page=1").param("lastName", "Unknown Surname"))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrors("owner", "lastName"))
			.andExpect(model().attributeHasFieldErrorCode("owner", "lastName", "notFound"))
			.andExpect(view().name("owners/findOwners"));

	}

	@Test
	void initUpdateOwnerForm() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/edit", TEST_OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("owner"))
			.andExpect(model().attribute("owner", hasProperty("lastName", is("Franklin"))))
			.andExpect(model().attribute("owner", hasProperty("firstName", is("George"))))
			.andExpect(model().attribute("owner", hasProperty("address", is("110 W. Liberty St."))))
			.andExpect(model().attribute("owner", hasProperty("city", is("Madison"))))
			.andExpect(model().attribute("owner", hasProperty("telephone", is("6085551023"))))
			.andExpect(view().name("owners/createOrUpdateOwnerForm"));
	}

	@Test
	void processUpdateOwnerFormSuccess() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID).param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "123 Caramel Street")
				.param("city", "London")
				.param("telephone", "1616291589"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Test
	void processUpdateOwnerFormUnchangedSuccess() throws Exception {
		mockMvc.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Test
	void processUpdateOwnerFormHasErrors() throws Exception {
		mockMvc
			.perform(post("/owners/{ownerId}/edit", TEST_OWNER_ID).param("firstName", "Joe")
				.param("lastName", "Bloggs")
				.param("address", "")
				.param("telephone", ""))
			.andExpect(status().isOk())
			.andExpect(model().attributeHasErrors("owner"))
			.andExpect(model().attributeHasFieldErrors("owner", "address"))
			.andExpect(model().attributeHasFieldErrors("owner", "telephone"))
			.andExpect(view().name("owners/createOrUpdateOwnerForm"));
	}

	@Test
	void showOwner() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}", TEST_OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(model().attribute("owner", hasProperty("lastName", is("Franklin"))))
			.andExpect(model().attribute("owner", hasProperty("firstName", is("George"))))
			.andExpect(model().attribute("owner", hasProperty("address", is("110 W. Liberty St."))))
			.andExpect(model().attribute("owner", hasProperty("city", is("Madison"))))
			.andExpect(model().attribute("owner", hasProperty("telephone", is("6085551023"))))
			.andExpect(model().attribute("owner", hasProperty("pets", not(empty()))))
			.andExpect(model().attribute("owner",
					hasProperty("pets", hasItem(hasProperty("visits", hasSize(greaterThan(0)))))))
			.andExpect(view().name("owners/ownerDetails"));
	}

	@Test
	void processUpdateOwnerFormWithIdMismatch() throws Exception {
		int pathOwnerId = 1;

		Owner owner = new Owner();
		owner.setId(2);
		owner.setFirstName("John");
		owner.setLastName("Doe");
		owner.setAddress("Center Street");
		owner.setCity("New York");
		owner.setTelephone("0123456789");

		when(owners.findById(pathOwnerId)).thenReturn(Optional.of(owner));

		mockMvc.perform(MockMvcRequestBuilders.post("/owners/{ownerId}/edit", pathOwnerId).flashAttr("owner", owner))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/owners/" + pathOwnerId + "/edit"))
			.andExpect(flash().attributeExists("error"));
	}

}
