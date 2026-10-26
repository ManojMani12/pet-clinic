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

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * Repository class for <code>Owner</code> domain objects All method names are compliant
 * with Spring Data naming conventions so this interface can easily be extended for
 * Spring Data
 */
public interface OwnerRepository extends Repository<Owner, Integer> {

	@Query("SELECT DISTINCT owner FROM Owner owner left join fetch owner.pets WHERE owner.lastName LIKE :lastName%")
	Collection<Owner> findByLastName(@Param("lastName") String lastName) throws DataAccessException;

	Page<Owner> findByLastNameStartingWith(String lastName, Pageable pageable) throws DataAccessException;

	Page<Owner> findByCityIgnoreCase(String city, Pageable pageable) throws DataAccessException;

	Page<Owner> findByLastNameStartingWithAndCityIgnoreCase(String lastName, String city, Pageable pageable)
			throws DataAccessException;

	Owner findById(int id) throws DataAccessException;

	void save(Owner owner) throws DataAccessException;

	Collection<Owner> findAll() throws DataAccessException;

}
