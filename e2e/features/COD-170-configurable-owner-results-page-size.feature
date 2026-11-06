@owners @COD-170
Feature: Configurable owner results page size
  As a system administrator
  I want the owner results page size to be configurable
  So that the list view can be tuned without code changes

  # Assumption: E2E runs against default configuration petclinic.owners.pageSize=5.

  Background:
    Given I am on the home page

  @COD-170 @AC-1
  Scenario: Owners search results show paging when more than one page is needed (default page size)
    When I navigate to the Find Owners page
    And I search owners by last name ""  # NEW STEP - needs step definition
    Then I should see the owners search results
    And I should see owners pagination controls  # NEW STEP - needs step definition

  @COD-170 @AC-2
  Scenario: Navigating across pages returns consistent results page size
    When I navigate to the Find Owners page
    And I search owners by last name ""  # NEW STEP - needs step definition
    Then I should see the owners search results
    When I go to the next owners results page
    Then I should see the owners search results

  @COD-170
  Scenario: Pagination does not include any page size query parameter
    When I navigate to the Find Owners page
    And I search owners by last name ""  # NEW STEP - needs step definition
    Then I should see the owners search results
    And the owners results URL should not include query parameter "pageSize"  # NEW STEP - needs step definition
