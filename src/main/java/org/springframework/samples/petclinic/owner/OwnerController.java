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
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
@RequestMapping("/owners")
class OwnerController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";

	private final OwnerRepository owners;

	private final Validator validator;

	@Autowired
	public OwnerController(OwnerRepository clinicService, Validator validator) {
		this.owners = clinicService;
		this.validator = validator;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@InitBinder("owner")
	public void initOwnerBinder(WebDataBinder dataBinder) {
		dataBinder.addValidators(this.validator);
	}

	@ModelAttribute("visit")
	public Visit loadVisit() {
		return new Visit();
	}

	@GetMapping("/new")
	public String initCreationForm(Model model) {
		Owner owner = new Owner();
		model.addAttribute("owner", owner);
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

	@GetMapping("/find")
	public String initFindForm(Model model) {
		model.addAttribute("owner", new Owner());
		return "owners/findOwners";
	}

	@GetMapping
	public String processFindForm(@RequestParam(defaultValue = "1") int page, Owner owner, BindingResult result,
			Model model, RedirectAttributes redirectAttributes) {
		// allow parameterless GET request for /owners to return all records
		String lastName = owner.getLastName();
		if (lastName == null) {
			lastName = "";
		}
		else {
			lastName = lastName.strip();
		}

		// find owners by last name
		Page<Owner> paginatedOwners = findPaginatedForOwnersLastName(lastName, page);
		if (paginatedOwners.isEmpty()) {
			// no owners found
			result.rejectValue("lastName", "notFound", "not found");
			return "owners/findOwners";
		}
		else if (paginatedOwners.getTotalElements() == 1) {
			// 1 owner found
			owner = paginatedOwners.iterator().next();
			return "redirect:/owners/" + owner.getId();
		}
		else {
			// multiple owners found
			int currentPage = paginatedOwners.getNumber() + 1;
			int totalPages = paginatedOwners.getTotalPages();
			if (page < 1 || page > totalPages) {
				redirectAttributes.addAttribute("page", 1);
				redirectAttributes.addAttribute("lastName", lastName);
				return "redirect:/owners";
			}

			// Expose effective (trimmed) lastName for paging link construction.
			model.addAttribute("lastName", lastName);
			addPaginationModel(currentPage, paginatedOwners, model);
			return "owners/ownersList";
		}
	}

	private Page<Owner> findPaginatedForOwnersLastName(String lastname, int page) {
		int pageSize = 5;
		page = page < 1 ? 1 : page;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return owners.findByLastNameStartingWith(lastname, pageable);
	}

	private void addPaginationModel(int page, Page<Owner> paginated, Model model) {
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listOwners", paginated.getContent());
	}

	@GetMapping("/{ownerId}")
	public ModelAndView showOwner(@PathVariable("ownerId") int ownerId) {
		ModelAndView mav = new ModelAndView("owners/ownerDetails");
		mav.addObject(this.owners.findById(ownerId));
		return mav;
	}

	@GetMapping("/{ownerId}/edit")
	public String initUpdateOwnerForm(@PathVariable("ownerId") int ownerId, Model model) {
		Owner owner = this.owners.findById(ownerId);
		model.addAttribute(owner);
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/{ownerId}/edit")
	public String processUpdateOwnerForm(@Valid Owner owner, BindingResult result,
			@PathVariable("ownerId") int ownerId) {
		if (result.hasErrors()) {
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}

		owner.setId(ownerId);
		this.owners.save(owner);
		return "redirect:/owners/{ownerId}";
	}

	@GetMapping("/owners")
	public String getOwners(Model model) {
		Collection<Owner> results = this.owners.findAll();
		model.addAttribute("selections", results);
		return "owners/ownersList";
	}

	@GetMapping("/lastName")
	public String showResourcesByLastName(@RequestParam String lastName, Model model)
			throws DataAccessException {
		Collection<Owner> results = this.owners.findByLastName(lastName);
		model.addAttribute("selections", results);
		return "owners/ownersList";
	}

}
