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

import java.util.Collection;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/owners")
class OwnerController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";

	private final OwnerRepository owners;

	@Autowired
	public OwnerController(OwnerRepository clinicService) {
		this.owners = clinicService;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable(name = "ownerId", required = false) Integer ownerId) {
		return ownerId == null ? new Owner() : this.owners.findById(ownerId);
	}

	@GetMapping("/find")
	public String initFindForm() {
		return "owners/findOwners";
	}

	@GetMapping
	public String processFindForm(Owner owner, BindingResult result, Model model, @RequestParam(defaultValue = "1") int page) {
		String lastName = owner.getLastName();
		if (lastName == null) {
			lastName = "";
		}
		lastName = lastName.trim();

		String city = owner.getCity();
		if (city == null) {
			city = "";
		}
		city = city.trim();

		model.addAttribute("lastName", lastName);
		model.addAttribute("city", city);

		Page<Owner> ownersResults = findPaginatedForOwnersFilters(page, lastName, city);

		if (ownersResults.isEmpty()) {
			// no owners found
			if (!lastName.isBlank()) {
				result.rejectValue("lastName", "notFound", "not found");
			}
			else {
				result.rejectValue("city", "notFound", "not found");
			}
			return "owners/findOwners";
		}

		if (ownersResults.getTotalElements() == 1) {
			// 1 owner found
			Owner foundOwner = ownersResults.iterator().next();
			return "redirect:/owners/" + foundOwner.getId();
		}

		int totalPages = ownersResults.getTotalPages();
		if (page > totalPages) {
			StringBuilder redirect = new StringBuilder("redirect:/owners?page=1");
			if (!lastName.isBlank()) {
				redirect.append("&lastName=").append(lastName);
			}
			if (!city.isBlank()) {
				redirect.append("&city=").append(city);
			}
			return redirect.toString();
		}

		model.addAttribute("listOwners", ownersResults.getContent());
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		return "owners/ownersList";
	}

	private Page<Owner> findPaginatedForOwnersFilters(int page, String lastName, String city) {
		int pageSize = 5;
		PageRequest pageRequest = PageRequest.of(page - 1, pageSize);

		boolean hasLastName = !lastName.isBlank();
		boolean hasCity = !city.isBlank();

		if (hasLastName && hasCity) {
			return owners.findByLastNameStartingWithAndCityIgnoreCase(lastName, city, pageRequest);
		}
		if (hasCity) {
			return owners.findByCityIgnoreCase(city, pageRequest);
		}
		return owners.findByLastNameStartingWith(lastName, pageRequest);
	}

	@GetMapping("/{ownerId}")
	public String showOwner(@PathVariable("ownerId") int ownerId, Model model) {
		Owner owner = this.owners.findById(ownerId);
		model.addAttribute(owner);
		return "owners/ownerDetails";
	}

	@GetMapping("/new")
	public String initCreationForm(Model model) {
		Owner owner = new Owner();
		model.addAttribute(owner);
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/new")
	public String processCreationForm(@Valid Owner owner, BindingResult result) {
		if (result.hasErrors()) {
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}

		this.owners.save(owner);
		return "redirect:/owners/" + owner.getId();
	}

	@GetMapping("/{ownerId}/edit")
	public String initUpdateOwnerForm(@PathVariable("ownerId") int ownerId, Model model) {
		Owner owner = this.owners.findById(ownerId);
		model.addAttribute(owner);
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/{ownerId}/edit")
	public String processUpdateOwnerForm(@Valid Owner owner, BindingResult result, @PathVariable("ownerId") int ownerId) {
		if (result.hasErrors()) {
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}

		owner.setId(ownerId);
		this.owners.save(owner);
		return "redirect:/owners/{ownerId}";
	}

	@GetMapping("/ownersList")
	public String ownersList(Model model) {
		Collection<Owner> results = this.owners.findAll();
		model.addAttribute("selections", results);
		return "owners/ownersList";
	}

}
