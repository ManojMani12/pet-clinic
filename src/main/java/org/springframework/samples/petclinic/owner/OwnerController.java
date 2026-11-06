/*
 * Copyright 2012-2023 the original author or authors.
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

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/owners")
class OwnerController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";

	private static final int DEFAULT_PAGE_SIZE = 5;

	private final OwnerRepository owners;

	private final int pageSize;

	public OwnerController(OwnerRepository owners,
			@Value("${petclinic.owners.pageSize:" + DEFAULT_PAGE_SIZE + "}") int pageSize) {
		this.owners = owners;
		this.pageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
	}

	@InitBinder
	void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable(name = "ownerId", required = false) Integer ownerId) {
		return ownerId == null ? new Owner() : this.owners.findById(ownerId);
	}

	@GetMapping("/new")
	public String initCreationForm(Map<String, Object> model) {
		Owner owner = new Owner();
		model.put("owner", owner);
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/new")
	public String processCreationForm(@Validated Owner owner, BindingResult result) {
		if (result.hasErrors()) {
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}

		this.owners.save(owner);
		return "redirect:/owners/" + owner.getId();
	}

	@GetMapping("/find")
	public String initFindForm() {
		return "owners/findOwners";
	}

	@GetMapping
	public String processFindForm(@ModelAttribute Owner owner, BindingResult result, Model model,
			@GetMapping(name = "page") Optional<Integer> page) {
		String lastName = StringUtils.hasText(owner.getLastName()) ? owner.getLastName() : "";
		String city = StringUtils.hasText(owner.getCity()) ? owner.getCity().trim() : "";
		if (city.isEmpty()) {
			owner.setCity(null);
		}

		int currentPage = page.orElse(1);
		Page<Owner> paginatedOwners = findPaginatedForOwners(currentPage, lastName, city);
		Collection<Owner> results = paginatedOwners.getContent();

		if (results.isEmpty()) {
			if (lastName.isEmpty() && !city.isEmpty()) {
				result.rejectValue("city", "notFound", "not found");
			}
			else {
				result.rejectValue("lastName", "notFound", "not found");
			}
			return "owners/findOwners";
		}

		if (results.size() == 1) {
			Owner foundOwner = results.iterator().next();
			String redirectCity = city.isEmpty() ? "" : "&city=" + city;
			return "redirect:/owners/" + foundOwner.getId() + "?page=1&lastName=" + lastName + redirectCity;
		}

		String redirectLastName = lastName.isEmpty() ? "" : "&lastName=" + lastName;
		String redirectCity = city.isEmpty() ? "" : "&city=" + city;
		String redirectPage = currentPage > paginatedOwners.getTotalPages() ? "1" : String.valueOf(currentPage);
		return "redirect:/owners?page=" + redirectPage + redirectLastName + redirectCity;
	}

	@GetMapping("/page/{page}")
	public String showOwnersList(@PathVariable int page, @ModelAttribute("lastName") String lastName,
			@ModelAttribute("city") String city, Model model) {
		return addPaginationModel(page, lastName, city, model);
	}

	private String addPaginationModel(int page, String lastName, String city, Model model) {
		int validatedPage = Math.max(page, 1);
		Page<Owner> paginated = findPaginatedForOwners(validatedPage, lastName, city);
		model.addAttribute("currentPage", validatedPage);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("listOwners", paginated.getContent());
		if (StringUtils.hasText(lastName)) {
			model.addAttribute("lastName", lastName);
		}
		if (StringUtils.hasText(city)) {
			model.addAttribute("city", city);
		}
		return "owners/ownersList";
	}

	private Page<Owner> findPaginatedForOwners(int page, String lastname, String city) {
		int validatedPage = Math.max(page, 1);
		Pageable pageable = PageRequest.of(validatedPage - 1, this.pageSize);
		if (city.isEmpty()) {
			return owners.findByLastNameStartingWith(lastname, pageable);
		}
		if (lastname.isEmpty()) {
			return owners.findByCityIgnoreCase(city, pageable);
		}
		return owners.findByLastNameStartingWithAndCityIgnoreCase(lastname, city, pageable);
	}

	@InitBinder("owner")
	void initOwnerBinder(DataBinder dataBinder) {
		dataBinder.registerCustomEditor(PetType.class, "petType", new PetTypeEditor());
	}

	@GetMapping("/{ownerId}/edit")
	public String initUpdateOwnerForm(@PathVariable Integer ownerId, Model model) {
		Owner owner = this.owners.findById(ownerId);
		model.addAttribute(owner);
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/{ownerId}/edit")
	public String processUpdateOwnerForm(@Validated Owner owner, BindingResult result,
			@PathVariable Integer ownerId) {
		if (result.hasErrors()) {
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}

		owner.setId(ownerId);
		this.owners.save(owner);
		return "redirect:/owners/{ownerId}";
	}

	@GetMapping("/{ownerId}")
	public String showOwner(@PathVariable("ownerId") int ownerId, Model model) {
		Owner owner = this.owners.findById(ownerId);
		model.addAttribute(owner);
		return "owners/ownerDetails";
	}

}
