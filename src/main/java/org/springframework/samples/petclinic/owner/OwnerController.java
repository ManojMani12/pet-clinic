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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.validation.Valid;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Wick Dynex
 */
@Controller
class OwnerController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";

	static final int DEFAULT_PAGE_SIZE = 5;

	static final int MAX_PAGE_SIZE = 100;

	private final OwnerRepository owners;

	private final int defaultPageSize;

	public OwnerController(OwnerRepository owners) {
		this(owners, DEFAULT_PAGE_SIZE);
	}

	@Autowired
	public OwnerController(OwnerRepository owners,
			@Value("${owners.page-size:" + DEFAULT_PAGE_SIZE + "}") int configuredPageSize) {
		this.owners = owners;
		this.defaultPageSize = clampPageSize(configuredPageSize, DEFAULT_PAGE_SIZE);
	}

	/**
	 * Returns {@code requested} bounded to [1, MAX_PAGE_SIZE]: non-positive values fall
	 * back to {@code fallback}, values above the maximum are clamped to it.
	 */
	private static int clampPageSize(int requested, int fallback) {
		if (requested < 1) {
			return fallback;
		}
		return Math.min(requested, MAX_PAGE_SIZE);
	}

	private int resolvePageSize(String requestedSize) {
		if (requestedSize == null) {
			return this.defaultPageSize;
		}
		try {
			return clampPageSize(Integer.parseInt(requestedSize.strip()), this.defaultPageSize);
		}
		catch (NumberFormatException ex) {
			return this.defaultPageSize;
		}
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id", "*.id");
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable(name = "ownerId", required = false) Integer ownerId) {
		return ownerId == null ? new Owner()
				: this.owners.findById(ownerId)
					.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId
							+ ". Please ensure the ID is correct " + "and the owner exists in the database."));
	}

	@GetMapping("/owners/new")
	public String initCreationForm() {
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/owners/new")
	public String processCreationForm(@Valid Owner owner, BindingResult result, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in creating the owner.");
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}

		this.owners.save(owner);
		redirectAttributes.addFlashAttribute("message", "New Owner Created");
		return "redirect:/owners/" + owner.getId();
	}

	@GetMapping("/owners/find")
	public String initFindForm() {
		return "owners/findOwners";
	}

	@GetMapping("/owners")
	public String processFindForm(@RequestParam(defaultValue = "1") int page,
			@RequestParam(name = "size", required = false) String requestedSize, Owner owner, BindingResult result,
			Model model, RedirectAttributes redirectAttributes) {
		// allow parameterless GET request for /owners to return all records
		String lastName = owner.getLastName();
		if (lastName == null) {
			lastName = ""; // empty string signifies broadest possible search
		}
		else {
			lastName = lastName.strip();
		}

		String city = owner.getCity() == null ? "" : owner.getCity().strip();
		int pageSize = resolvePageSize(requestedSize);
		Integer sizeParam = pageSize == this.defaultPageSize ? null : pageSize;

		// find owners by last name (prefix) and, when given, city (exact,
		// case-insensitive)
		Page<Owner> ownersResults = findPaginatedForOwners(page, pageSize, lastName, city);
		if (page < 1 || page > Math.max(ownersResults.getTotalPages(), 1)) {
			redirectAttributes.addAttribute("page", 1);
			if (!lastName.isEmpty()) {
				redirectAttributes.addAttribute("lastName", lastName);
			}
			if (!city.isEmpty()) {
				redirectAttributes.addAttribute("city", city);
			}
			if (sizeParam != null) {
				redirectAttributes.addAttribute("size", sizeParam);
			}
			return "redirect:/owners";
		}

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

		// multiple owners found
		model.addAttribute("pageLinks", buildPageLinks(ownersResults.getTotalPages(), lastName, city, sizeParam));
		return addPaginationModel(page, model, ownersResults);
	}

	private String addPaginationModel(int page, Model model, Page<Owner> paginated) {
		List<Owner> listOwners = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listOwners", listOwners);
		return "owners/ownersList";
	}

	/**
	 * Builds one fully-formed, URL-encoded link per page number (1..totalPages). The
	 * filter values are passed as URI variables so they are encoded exactly once (strict
	 * encoding, so {@code &}, {@code %}, {@code +} and non-ASCII characters are escaped).
	 * Blank filters and a default page size are omitted.
	 */
	private static Map<Integer, String> buildPageLinks(int totalPages, String lastName, String city, Integer size) {
		UriComponentsBuilder base = UriComponentsBuilder.fromPath("/owners").queryParam("page", "{page}");
		Map<String, Object> vars = new HashMap<>();
		if (!lastName.isEmpty()) {
			base.queryParam("lastName", "{lastName}");
			vars.put("lastName", lastName);
		}
		if (!city.isEmpty()) {
			base.queryParam("city", "{city}");
			vars.put("city", city);
		}
		if (size != null) {
			base.queryParam("size", "{size}");
			vars.put("size", size);
		}
		UriComponentsBuilder template = base.encode();
		Map<Integer, String> links = new LinkedHashMap<>();
		for (int i = 1; i <= totalPages; i++) {
			vars.put("page", i);
			links.put(i, template.buildAndExpand(vars).toUriString());
		}
		return links;
	}

	private Page<Owner> findPaginatedForOwners(int page, int pageSize, String lastName, String city) {
		int validatedPage = Math.max(page, 1);
		Pageable pageable = PageRequest.of(validatedPage - 1, pageSize);
		if (city.isEmpty()) {
			return owners.findByLastNameStartingWith(lastName, pageable);
		}
		return owners.findByLastNameStartingWithAndCityIgnoreCase(lastName, city, pageable);
	}

	@GetMapping("/owners/{ownerId}/edit")
	public String initUpdateOwnerForm() {
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/owners/{ownerId}/edit")
	public String processUpdateOwnerForm(@Valid Owner owner, BindingResult result, @PathVariable("ownerId") int ownerId,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in updating the owner.");
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}

		if (!Objects.equals(owner.getId(), ownerId)) {
			result.rejectValue("id", "mismatch", "The owner ID in the form does not match the URL.");
			redirectAttributes.addFlashAttribute("error", "Owner ID mismatch. Please try again.");
			return "redirect:/owners/{ownerId}/edit";
		}

		owner.setId(ownerId);
		this.owners.save(owner);
		redirectAttributes.addFlashAttribute("message", "Owner Values Updated");
		return "redirect:/owners/{ownerId}";
	}

	/**
	 * Custom handler for displaying an owner.
	 * @param ownerId the ID of the owner to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@GetMapping("/owners/{ownerId}")
	public ModelAndView showOwner(@PathVariable("ownerId") int ownerId) {
		ModelAndView mav = new ModelAndView("owners/ownerDetails");
		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		Owner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));
		mav.addObject(owner);
		return mav;
	}

}
