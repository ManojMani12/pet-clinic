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

import java.util.Collection;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.samples.petclinic.system.CrashController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 */
@Controller
@RequestMapping("/owners")
class OwnerController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";

	private final OwnerRepository owners;

	private final int ownersPageSize;

	@Autowired
	public OwnerController(OwnerRepository clinicService, @Value("${petclinic.owners.pageSize:5}") int ownersPageSize) {
		this.owners = clinicService;
		this.ownersPageSize = ownersPageSize > 0 ? ownersPageSize : 5;
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable(name = "ownerId", required = false) Integer ownerId) throws DataAccessException {
		return ownerId == null ? new Owner() : this.owners.findById(ownerId);
	}

	@GetMapping("/find")
	public String initFindForm() {
		return "owners/findOwners";
	}

	@GetMapping
	public String processFindForm(@RequestParam(defaultValue = "1") int page, Owner owner, BindingResult result,
			Model model) {

		// allow parameterless GET request for /owners to return all records
		if (owner.getLastName() == null) {
			owner.setLastName("");
		}

		String lastName = owner.getLastName().trim();
		String city = owner.getCity() == null ? "" : owner.getCity().trim();

		Page<Owner> ownersResults = findPaginatedForOwnersLastNameAndCity(page, lastName, city);
		if (ownersResults.isEmpty()) {
			// no owners found
			result.rejectValue("lastName", "notFound", "not found");
			return "owners/findOwners";
		}
		if (ownersResults.getTotalElements() == 1) {
			// 1 owner found
			owner = ownersResults.iterator().next();
			return "redirect:/owners/" + owner.getId();
		}

		addPaginationModel(page, model, ownersResults);
		model.addAttribute("searchLastName", lastName);
		model.addAttribute("searchCity", city);
		return "owners/ownersList";
	}

	private Page<Owner> findPaginatedForOwnersLastNameAndCity(int page, String lastName, String city) {
		Pageable pageable = PageRequest.of(page - 1, ownersPageSize, Sort.by("lastName").ascending());
		if (city == null || city.isBlank()) {
			return owners.findByLastNameStartingWith(lastName, pageable);
		}
		return owners.findByLastNameStartingWithAndCityIgnoreCase(lastName, city, pageable);
	}

	private void addPaginationModel(int page, Model model, Page<Owner> paginated) {
		Collection<Owner> listOwners = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listOwners", listOwners);
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
	public String processUpdateOwnerForm(@Valid Owner owner, BindingResult result, @PathVariable("ownerId") int ownerId,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}

		owner.setId(ownerId);
		this.owners.save(owner);
		redirectAttributes.addFlashAttribute("message", "Owner Values Updated");
		return "redirect:/owners/{ownerId}";
	}

	@GetMapping("*")
	public String crashTest() {
		throw new RuntimeException("Expected: controller used to showcase what " + CrashController.class.getName()
				+ " does when an exception is thrown");
	}

}
