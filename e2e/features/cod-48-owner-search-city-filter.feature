@owners @search @COD-48
Feature: Add city filter to Owner search
  As a front-desk user
  I want to filter Owner search results by city (in addition to last name)
  So that I can find the correct owner faster when last names are common

  Background:
    Given I am on the home page

  @COD-48 @AC-1
  Scenario: Searching by lastName and city returns only owners matching both filters
    When I navigate to the Find Owners page
    And I search owners by last name "Davis" and city "Madison"
    # NEW STEP - needs step definition
    Then I should see the owners list page
    And all owners in the results should have last name starting with "Davis"
    # NEW STEP - needs step definition
    And all owners in the results should have city "Madison"
    # NEW STEP - needs step definition

  @COD-48 @AC-2
  Scenario: Searching by a nonexistent city shows notFound behavior
    When I navigate to the Find Owners page
    And I search owners by last name "" and city "NonexistentCity"
    # NEW STEP - needs step definition
    Then I should see the find owners page
    And I should see a find owners not found validation message
    # NEW STEP - needs step definition

  @COD-48 @AC-3
  Scenario: Paging preserves both lastName and city filters
    When I navigate to the Find Owners page
    And I search owners by last name "Fra" and city "Madison"
    # NEW STEP - needs step definition
    Then I should see the owners list page
    And the owners list pagination should be visible
    When I click owners pagination control "Next"
    Then the URL should contain query parameter "lastName" with value "Fra"
    And the URL should contain query parameter "city" with value "Madison"

  @COD-48 @BR-1
  Scenario: Blank city does not restrict results
    When I navigate to the Find Owners page
    And I search owners by last name "Fra" and city ""
    # NEW STEP - needs step definition
    Then I should see the owners list page
    And the URL should not contain query parameter "city"
    # NEW STEP - needs step definition
