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

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@SessionAttributes("owner")
class OwnerController {

	private final OwnerRepository owners;

	private final int pageSize;

	OwnerController(OwnerRepository owners) {
		this(owners, 5);
	}

	OwnerController(OwnerRepository owners, @Value("${petclinic.owners.pageSize:5}") int pageSize) {
		this.owners = owners;
		this.pageSize = clampPageSize(pageSize);
	}

	private int clampPageSize(int value) {
		if (value < 1) {
			return 1;
		}
		if (value > 100) {
			return 100;
		}
		return value;
	}

	@InitBinder
	public void setAllowedFields(org.springframework.web.bind.WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable(value = "ownerId", required = false) Integer ownerId) {
		return ownerId == null ? new Owner() : this.owners.findById(ownerId);
	}

	@GetMapping("/owners/new")
	public String initCreationForm(Model model) {
		Owner owner = new Owner();
		model.addAttribute("owner", owner);
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/owners/new")
	public String processCreationForm(@Valid Owner owner, BindingResult result) {
		if (result.hasErrors()) {
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}

		this.owners.save(owner);
		return "redirect:/owners/" + owner.getId();
	}

	@GetMapping("/owners/find")
	public String initFindForm(@ModelAttribute Owner owner, @RequestParam(required = false) String city, Model model) {
		// Note: keep existing behavior for lastName binding via Owner model attribute.
		String normalizedCity = (city == null) ? "" : city.strip();
		if (!normalizedCity.isBlank()) {
			model.addAttribute("city", normalizedCity);
		}
		return VIEWS_OWNER_FIND_FORM;
	}

	private String escapeLike(String input) {
		// Escape LIKE wildcards and the escape character itself.
		return input.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
	}

	@GetMapping("/owners")
	public String processFindForm(@ModelAttribute Owner owner, BindingResult result,
			@RequestParam(defaultValue = "1") int page, @RequestParam(required = false) Integer size,
			@RequestParam(required = false) String city, Model model) {
		String lastName = owner.getLastName();
		lastName = lastName == null ? "" : lastName.strip();

		String normalizedCity = (city == null) ? "" : city.strip();

		if (!lastName.isEmpty()) {
			owner.setLastName(lastName);
		}

		int effectivePageSize = this.pageSize;
		if (size != null) {
			effectivePageSize = clampPageSize(size);
		}
		Pageable pageable = PageRequest.of(page - 1, effectivePageSize);

		Page<Owner> ownersResults = this.owners.findByLastNameStartingWithAndCity(escapeLike(lastName), normalizedCity,
				pageable);

		if (ownersResults.isEmpty()) {
			// no owners found
			result.rejectValue("lastName", "notFound", "not found");
			if (!normalizedCity.isBlank()) {
				model.put("city", normalizedCity);
			}
			return VIEWS_OWNER_FIND_FORM;
		}

		if (ownersResults.getTotalPages() > 0 && page > ownersResults.getTotalPages()) {
			UriComponentsBuilder redirect = UriComponentsBuilder.fromPath("/owners")
					.queryParam("page", ownersResults.getTotalPages());
			if (!lastName.isBlank()) {
				redirect.queryParam("lastName", lastName);
			}
			if (!normalizedCity.isBlank()) {
				redirect.queryParam("city", normalizedCity);
			}
			if (effectivePageSize != this.pageSize) {
				redirect.queryParam("size", effectivePageSize);
			}
			return "redirect:" + redirect.toUriString();
		}

		model.put("listOwners", ownersResults.getContent());
		model.put("currentPage", page);
		model.put("totalPages", ownersResults.getTotalPages());
		if (!lastName.isBlank()) {
			model.put("lastName", lastName);
		}
		if (!normalizedCity.isBlank()) {
			model.put("city", normalizedCity);
		}
		if (effectivePageSize != this.pageSize) {
			model.put("size", effectivePageSize);
		}
		return VIEWS_OWNER_LIST;
	}

	@GetMapping("/owners/{ownerId}/edit")
	public String initUpdateOwnerForm(@PathVariable("ownerId") int ownerId, Model model) {
		Owner owner = this.owners.findById(ownerId);
		model.addAttribute(owner);
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/owners/{ownerId}/edit")
	public String processUpdateOwnerForm(@Valid Owner owner, BindingResult result, @PathVariable("ownerId") int ownerId) {
		if (result.hasErrors()) {
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}

		owner.setId(ownerId);
		this.owners.save(owner);
		return "redirect:/owners/{ownerId}";
	}

	@GetMapping("/owners/{ownerId}")
	public String showOwner(@PathVariable("ownerId") int ownerId, Model model) {
		model.addAttribute(this.owners.findById(ownerId));
		return "owners/ownerDetails";
	}

	@GetMapping(value = "/owners", produces = "application/json")
	@ResponseBody
	public Page<Owner> showResourcesOwnerList(@RequestParam(defaultValue = "") String lastName,
			@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "5") int size) {

		Pageable pageable = PageRequest.of(page - 1, size);
		return this.owners.findByLastNameStartingWith(lastName, pageable);
	}

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";

	private static final String VIEWS_OWNER_FIND_FORM = "owners/findOwners";

	private static final String VIEWS_OWNER_LIST = "owners/ownersList";

}
