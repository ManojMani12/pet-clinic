@owners @COD-48
Feature: Add city filter to Owner search
  As a front-desk user
  I want to filter Owner search results by city (in addition to last name)
  So that I can find the correct owner faster when last names are common

  Background:
    Given I am on the home page
    When I navigate to the Find Owners page

  @AC-1
  Scenario: Searching by lastName and city returns only matching owners
    When I search owners by last name "Davis" and city "Madison"
    # NEW STEP - needs step definition
    Then I should see the owners list
    And the URL should contain "lastName=Davis"
    And the URL should contain "city=Madison"
    And I should see owners with last name starting with "Davis" and city "Madison"
    # NEW STEP - needs step definition

  @AC-2
  Scenario: Searching with a non-existent city shows the notFound behavior
    When I search owners by last name "" and city "NonexistentCity"
    # NEW STEP - needs step definition
    Then I should remain on the Find Owners page
    And I should see a "not found" validation error
    # NEW STEP - needs step definition

  @AC-3
  Scenario: Paging preserves both lastName and city filters
    When I search owners by last name "Fra" and city "Madison"
    # NEW STEP - needs step definition
    Then I should see the owners list
    And the URL should contain "lastName=Fra"
    And the URL should contain "city=Madison"
    # Note: this scenario assumes the active data set yields multiple pages for the given filters.
    When I navigate to the next owners page using pagination controls
    Then the URL should contain "page="
    And the URL should contain "lastName=Fra"
    And the URL should contain "city=Madison"

  Scenario: Blank city does not restrict results and is not preserved in the URL
    When I search owners by last name "Fra" and city "   "
    # NEW STEP - needs step definition
    Then I should see the owners list
    And the URL should contain "lastName=Fra"
    And the URL should not contain "city="
    # NEW STEP - needs step definition
