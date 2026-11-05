@COD-125
Feature: Filter Owner search results by city
  As a receptionist
  I want to filter Owner search results by city in addition to last name
  So that I can narrow down owners with common surnames

  # Assumption: the Find Owners form includes an optional City input.

  @COD-125 @AC-1
  Scenario: City filter narrows results when provided
    Given I am on the Find Owners page
    When I search owners by last name "" and city "Madison"
    Then I should see the owners list results
    And the owners results should be filtered by city "Madison"

  @COD-125 @AC-2
  Scenario: Leaving city blank preserves existing behavior
    Given I am on the Find Owners page
    When I search owners by last name "" and city ""
    Then I should see the owners list results

  @COD-125 @AC-3
  Scenario: City filter is case-insensitive exact match
    Given I am on the Find Owners page
    When I search owners by last name "" and city "madison"
    Then I should see the owners list results
    And the owners results should be filtered by city "Madison"

  @COD-125 @AC-4
  Scenario: Pagination preserves both last name and city filters
    Given I am on the Find Owners page
    When I search owners by last name "D" and city "Madison"
    Then I should see the owners list results
    When I go to the next page of owners results
    Then the owners results should still be filtered by last name "D"
    And the owners results should be filtered by city "Madison"

  # NEW STEP - needs step definition
  # I search owners by last name "{string}" and city "{string}"

  # NEW STEP - needs step definition
  # The owners results should be filtered by city "{string}"
