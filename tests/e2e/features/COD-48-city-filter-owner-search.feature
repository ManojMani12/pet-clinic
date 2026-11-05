@COD-48
Feature: Add city filter to Owner search
  As a front-desk user
  I want to filter Owner search results by city (in addition to last name)
  So that I can find the correct owner faster when last names are common

  @COD-48 @AC-1
  Scenario: City filter restricts results along with lastName
    Given I provide lastName "Davis" and city "Madison" on the Find Owners form
    When I submit the owner search
    Then the results list shows only owners whose last name starts with "Davis"
    And the results list shows only owners whose city is "Madison"

  @COD-48 @AC-2
  Scenario: City with no matches shows notFound behavior
    Given I provide city "NonexistentCity" on the Find Owners form
    When I submit the owner search
    Then I remain on the Find Owners page
    And I see the notFound validation behavior

  @COD-48 @AC-3
  Scenario: Paging preserves both lastName and city
    Given I searched owners with lastName "Fra" and city "Madison" and the results have more than one page
    When I navigate to the next page
    Then the request URL includes "lastName=Fra"
    And the request URL includes "city=Madison"
    And the page results remain filtered

  @COD-48 @Edge
  Scenario: Blank city does not restrict results
    Given I provide lastName "Fra" and blank city on the Find Owners form
    When I submit the owner search
    Then the request URL does not include a "city" query parameter
