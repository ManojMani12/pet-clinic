@owners @COD-169
Feature: Filter owner search results by city
  As a receptionist
  I want to filter owner search results by city in addition to last name
  So that I can find the correct owner faster

  Background:
    Given I am on the home page

  @COD-169 @AC-1
  Scenario: Searching by last name prefix and city returns filtered owners
    When I navigate to the Find Owners page
    And I search owners by last name "D" and city "Madison"  # NEW STEP - needs step definition
    Then I should see the owners search results
    And the owners results URL should include query parameter "lastName" with value "D"  # NEW STEP - needs step definition
    And the owners results URL should include query parameter "city" with value "Madison"  # NEW STEP - needs step definition

  @COD-169 @AC-2
  Scenario: Searching with a city that yields no matches returns not found on the Find Owners form
    When I navigate to the Find Owners page
    And I search owners by last name "D" and city "NoSuchCity"  # NEW STEP - needs step definition
    Then I should see a not found error for owner last name  # NEW STEP - needs step definition

  @COD-169 @AC-3
  Scenario: Pagination preserves both last name and city filters
    When I navigate to the Find Owners page
    And I search owners by last name "D" and city "Madison"  # NEW STEP - needs step definition
    Then I should see the owners search results
    When I go to the next owners results page
    Then the owners results URL should include query parameter "lastName" with value "D"  # NEW STEP - needs step definition
    And the owners results URL should include query parameter "city" with value "Madison"  # NEW STEP - needs step definition

  @COD-169
  Scenario: Leaving city blank falls back to last name only search
    When I navigate to the Find Owners page
    And I search owners by last name "D" and city ""  # NEW STEP - needs step definition
    Then I should see the owners search results
    And the owners results URL should not include query parameter "city"  # NEW STEP - needs step definition
