@owners @pagination @COD-80
Feature: Preserve last name filter across Owner results pagination
  As a front-desk user
  I want Owner results pagination links to retain my last-name search filter
  So that I can browse pages without losing my search context

  Background:
    Given I am on the home page

  @COD-80 @AC-1
  Scenario: Paging to page 2 preserves lastName filter in the URL and keeps results filtered
    When I navigate to the Find Owners page
    And I search owners by last name "Fra"
    Then I should see the owners list page
    And the owners list should show last name filter "Fra" in the search form
    # Assumption: there are enough seeded owners with last name starting with "Fra" to produce multiple pages
    And the owners list pagination should be visible
    When I click owners pagination link "2"
    Then the URL should contain query parameter "lastName" with value "Fra"
    And all owners in the results should have last name starting with "Fra"
    # NEW STEP - needs step definition

  @COD-80 @AC-2
  Scenario: Paging without a lastName filter does not introduce lastName in links
    When I navigate to the Find Owners page
    And I search owners with empty last name
    Then I should see the owners list page
    And the owners list pagination should be visible
    When I click owners pagination control "Next"
    Then the URL should not contain query parameter "lastName"
    # NEW STEP - needs step definition

  @COD-80 @AC-3
  Scenario: lastName is trimmed by the controller and the trimmed value is preserved while paging
    When I navigate to the Find Owners page
    And I search owners by last name "  Fra  "
    Then I should see the owners list page
    And the owners list should show last name filter "Fra" in the search form
    # NEW STEP - needs step definition
    When I click owners pagination control "Next"
    Then the URL should contain query parameter "lastName" with value "Fra"
    And the URL should not contain query parameter "lastName" with value "  Fra  "
    # NEW STEP - needs step definition

  @COD-80 @BR-2
  Scenario: All paging controls preserve the active lastName filter
    When I navigate to the Find Owners page
    And I search owners by last name "Fra"
    Then I should see the owners list page
    And the owners list pagination should be visible
    Then all owners pagination links should include query parameter "lastName" with value "Fra"
    # NEW STEP - needs step definition
