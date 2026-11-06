@owners @COD-168
Feature: Preserve last-name filter across owner results pagination
  As a receptionist
  I want owner search pagination to retain the active last-name filter
  So that I can browse the same filtered result set across pages

  # Assumption: Existing e2e steps support navigating to Find Owners and verifying query parameters.

  Background:
    Given I am on the home page

  @COD-168 @AC-1
  Scenario: Pagination keeps the active last name filter when navigating to the next page
    When I navigate to the Find Owners page
    And I search owners by last name "Davis"
    Then I should see the owners search results
    When I go to the next owners results page
    Then the owners results URL should include query parameter "lastName" with value "Davis"  # NEW STEP - needs step definition

  @COD-168 @AC-2
  Scenario: Unfiltered owners pagination does not add a lastName query parameter
    When I navigate to the Find Owners page
    And I search owners with an empty last name
    Then I should see the owners search results
    When I go to the next owners results page
    Then the owners results URL should not include query parameter "lastName"  # NEW STEP - needs step definition

  @COD-168 @AC-3
  Scenario: Whitespace in last name input is normalized and preserved across pagination
    When I navigate to the Find Owners page
    And I search owners by last name "Da vis"
    Then I should see the owners search results
    When I go to the next owners results page
    Then the owners results URL should include query parameter "lastName" with value "Davis"  # NEW STEP - needs step definition
