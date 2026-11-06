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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface OwnerRepository extends Repository<Owner, Integer> {

	/**
	 * Retrieve {@link Owner}s from the data store by last name.
	 * @param lastName Value to search for
	 * @return a Collection of matching {@link Owner}s (or an empty Collection if none
	 * found)
	 */
	@Query("SELECT DISTINCT owner FROM Owner owner left join fetch owner.pets WHERE owner.lastName LIKE :lastName%")
	Collection<Owner> findByLastName(@Param("lastName") String lastName);

	/**
	 * Retrieve {@link Owner}s from the data store by last name, returning all owners whose last
	 * name <i>starts</i> with the given input.
	 * @param lastName last name prefix.
	 * @param pageable paging information.
	 * @return a page of {@link Owner}s.
	 */
	Page<Owner> findByLastNameStartingWith(String lastName, Pageable pageable);

	/**
	 * Retrieve {@link Owner}s by lastName prefix and an optional city filter.
	 * <p>
	 * lastName prefix match is case-insensitive, and city is matched as a
	 * case-insensitive exact match when provided.
	 * <p>
	 * Note: The lastName prefix uses LIKE with explicit escaping. The caller must
	 * escape user-provided input for LIKE using backslash escaping.
	 */
	@Query("select o from Owner o "
			+ "where lower(o.lastName) like lower(concat(:lastName,'%')) escape '\\' "
			+ "and (:city = '' or lower(o.city) = lower(:city))")
	Page<Owner> findByLastNameStartingWithAndCity(@Param("lastName") String lastName, @Param("city") String city,
			Pageable pageable);

	Owner findById(int id);

	void save(Owner owner);

	Collection<Owner> findAll();

}
