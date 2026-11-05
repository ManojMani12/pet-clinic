@owners @COD-80
Feature: Preserve last name filter across Owner results pagination
  As a front-desk user
  I want Owner results pagination links to retain my last-name search filter
  So that I can browse pages without losing my search context

  Background:
    Given I am on the home page
    When I navigate to the Find Owners page

  @AC-1
  Scenario: Paging preserves an active lastName filter when navigating to page 2
    When I search owners by last name "Fra"
    Then I should see the owners list
    And the URL should contain "lastName=Fra"
    # Note: this scenario assumes the active data set yields multiple pages for lastName=Fra.
    # If the seed data does not yield > 1 page, update the test data or search term accordingly.
    When I navigate to owners page 2 using pagination controls
    Then the URL should contain "page=2"
    And the URL should contain "lastName=Fra"
    And I should see owners with last name starting with "Fra"
    # NEW STEP - needs step definition

  @AC-2
  Scenario: Paging does not introduce lastName when no filter is used
    When I search owners without a last name filter
    Then I should see the owners list
    And the URL should not contain "lastName="
    # NEW STEP - needs step definition
    When I navigate to the next owners page using pagination controls
    Then the URL should contain "page="
    And the URL should not contain "lastName="
    # NEW STEP - needs step definition

  @AC-3
  Scenario: Whitespace in lastName is trimmed and preserved across paging
    When I search owners by last name "  Fra  "
    Then I should see the owners list
    And the URL should contain "lastName=Fra"
    # Assumption: controller trims and uses trimmed value in pagination links.
    When I navigate to the next owners page using pagination controls
    Then the URL should contain "lastName=Fra"
    And the URL should not contain "lastName=%20"
    # NEW STEP - needs step definition
