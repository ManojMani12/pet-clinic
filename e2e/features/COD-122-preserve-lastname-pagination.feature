@COD-122
Feature: Preserve last name filter across Owner search pagination
  As a receptionist
  I want the Owner search (lastName) filter to be preserved when navigating pagination links
  So that paging does not reset my results

  # Assumption: existing e2e step definitions expose a way to navigate to the Find Owners page and submit the search form.
  # Assumption: the Owner search results page contains pagination controls (first/prev/next/last and page numbers)
  #            as described in COD-122.

  @COD-122 @AC-1
  Scenario: Pagination keeps the active last name filter
    Given I am on the Find Owners page
    When I search owners by last name "Davis"
    Then I should see the owners list results
    When I go to the next page of owners results
    Then the owners results should still be filtered by last name "Davis"

  @COD-122 @AC-2
  Scenario: Pagination does not add a blank last name parameter
    Given I am on the Find Owners page
    When I search owners by last name ""
    Then I should see the owners list results
    When I go to the next page of owners results
    Then the owners results should not be filtered by last name

  @COD-122 @AC-3
  Scenario: Special characters in last name are preserved when paging
    Given I am on the Find Owners page
    When I search owners by last name "De La"
    Then I should see the owners list results
    When I go to the next page of owners results
    Then the owners results should still be filtered by last name "De La"

  # NEW STEP - needs step definition
  # The owners results should still be filtered by last name "{string}"

  # NEW STEP - needs step definition
  # The owners results should not be filtered by last name

  # NEW STEP - needs step definition
  # I go to the next page of owners results
