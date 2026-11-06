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
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.samples.petclinic.system.CrashController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
@Controller
class OwnerController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";

	private final OwnerRepository owners;

	private final int pageSize;

	OwnerController(OwnerRepository owners, @Value("${petclinic.owners.pageSize:5}") int pageSize) {
		this.owners = owners;
		// Defensive clamp to avoid PageRequest.of(...) throwing on invalid configuration.
		this.pageSize = Math.max(pageSize, 1);
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@ModelAttribute("types")
	public Collection<PetType> populatePetTypes() {
		return this.owners.findPetTypes();
	}

	@RequestMapping("/owners/find")
	public String initFindForm(Model model) {
		model.addAttribute("owner", new Owner());
		return "owners/findOwners";
	}

	@GetMapping("/owners")
	public String processFindForm(Owner owner, BindingResult result, Model model,
			@RequestParam(defaultValue = "1") int page) {

		// allow parameterless GET request for /owners to return all records
		String lastName = owner.getLastName();
		if (!StringUtils.hasLength(lastName)) {
			lastName = "";
		}
		else {
			lastName = lastName.replaceAll("\\s", "");
		}

		String city = normalizeCity(owner.getCity());

		// find owners by last name
		Page<Owner> ownersResults = findPaginatedForOwnersSearch(page, lastName, city, model);

		if (ownersResults.isEmpty()) {
			// no owners found
			result.rejectValue("lastName", "notFound", "not found");
			return "owners/findOwners";
		}
		else if (ownersResults.getTotalElements() == 1) {
			// 1 owner found
			return "redirect:/owners/" + ownersResults.iterator().next().getId();
		}
		else {
			// multiple owners found
			return "owners/ownersList";
		}
	}

	private String normalizeCity(String city) {
		if (!StringUtils.hasLength(city)) {
			return "";
		}
		return city.strip().replaceAll("\\s+", " ");
	}

	private Page<Owner> findPaginatedForOwnersSearch(int page, String lastName, String city, Model model) {
		int currentPage = Math.max(page, 1);
		Pageable pageable = PageRequest.of(currentPage - 1, this.pageSize, Sort.by("lastName"));

		Page<Owner> findPaginated;
		if (StringUtils.hasLength(city)) {
			findPaginated = this.owners.findByLastNameStartingWithAndCityIgnoreCase(lastName, city, pageable);
		}
		else {
			findPaginated = this.owners.findByLastNameStartingWith(lastName, pageable);
		}

		model.addAttribute("currentPage", currentPage);
		model.addAttribute("totalPages", findPaginated.getTotalPages());
		model.addAttribute("totalItems", findPaginated.getTotalElements());
		model.addAttribute("listOwners", findPaginated.getContent());
		model.addAttribute("lastName", lastName);
		model.addAttribute("city", city);

		return findPaginated;
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

	@GetMapping("/owners/new")
	public String initCreationForm(Map<String, Object> model) {
		Owner owner = new Owner();
		model.put("owner", owner);
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

	@RequestMapping("/owners/{ownerId}")
	public String showOwner(@PathVariable("ownerId") int ownerId, Model model) {
		model.addAttribute(this.owners.findById(ownerId));
		return "owners/ownerDetails";
	}

	@GetMapping("/crash")
	public String crash() {
		return CrashController.triggerException();
	}

}
