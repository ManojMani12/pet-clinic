@owners @pagination @COD-98
Feature: Make Owner search results page size configurable
  As a clinic admin
  I want the Owner search results page size to be configurable
  So that paging can be tuned without code changes

  # NOTE: e2e cannot reliably override Spring Boot @Value configuration at runtime.
  # These scenarios verify observable paging behavior with the default configuration on the deployed test environment.

  Background:
    Given I am on the home page

  @COD-98 @AC-1
  Scenario: Default page size is 5 owners per page
    When I navigate to the Find Owners page
    And I search owners with empty last name
    Then I should see the owners list page
    And the owners list should show at most 5 owners on the page
    # NEW STEP - needs step definition

  @COD-98 @AC-2
  Scenario: Increasing configured page size changes total pages and owners per page
    # Assumption: test environment configured with petclinic.owners.pageSize=10
    When I navigate to the Find Owners page
    And I search owners with empty last name
    Then I should see the owners list page
    And the owners list should show at most 10 owners on the page
    # NEW STEP - needs step definition

  @COD-98 @AC-3
  Scenario: Non-positive configured page size falls back to 5 and search remains functional
    # Assumption: test environment configured with petclinic.owners.pageSize <= 0
    When I navigate to the Find Owners page
    And I search owners with empty last name
    Then I should see the owners list page
    And the owners list should show at most 5 owners on the page
    # NEW STEP - needs step definition
