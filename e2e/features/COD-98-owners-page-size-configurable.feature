@owners @COD-98
Feature: Make Owner search results page size configurable
  As a clinic admin
  I want the Owner search results page size to be configurable
  So that paging can be tuned without code changes

  # Note: the page size is controlled by application configuration (petclinic.owners.pageSize).
  # These scenarios validate observable behavior (page counts / list lengths) but do not change config at runtime.

  Background:
    Given I am on the home page
    When I navigate to the Find Owners page

  @AC-1
  Scenario: Default owner search page shows up to 5 results when no configuration is provided
    When I search owners without a last name filter
    Then I should see the owners list
    And the owners list should show at most 5 owners
    # NEW STEP - needs step definition

  @AC-2
  Scenario: Configured owner search page shows up to 10 results when petclinic.owners.pageSize=10
    # Assumption: test environment runs with petclinic.owners.pageSize=10.
    When I search owners without a last name filter
    Then I should see the owners list
    And the owners list should show at most 10 owners
    # NEW STEP - needs step definition

  @AC-3
  Scenario: Non-positive configured page size falls back to 5 and owner search remains functional
    # Assumption: test environment runs with a non-positive petclinic.owners.pageSize value.
    When I search owners without a last name filter
    Then I should see the owners list
    And the owners list should show at most 5 owners
    # NEW STEP - needs step definition
