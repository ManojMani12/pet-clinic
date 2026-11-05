@COD-80
Feature: Preserve last name filter across Owner results pagination
  As a front-desk user
  I want Owner results pagination links to retain my last-name search filter
  So that I can browse pages without losing my search context

  @COD-80 @AC-1
  Scenario: Page-number link preserves lastName filter
    Given I have searched owners with lastName "Fra" and the results have more than one page
    When I click the pagination link for page 2
    Then the request URL includes "lastName=Fra"
    And the page results contain only owners whose last name starts with "Franklin"

  @COD-80 @AC-2
  Scenario: Unfiltered pagination does not add lastName parameter
    Given I navigated to "/owners" with no lastName filter
    When I use pagination controls
    Then generated pagination links do not include a "lastName" query parameter

  @COD-80 @AC-3
  Scenario: Whitespace in lastName is trimmed and preserved in links
    Given I searched owners with lastName "  Fra  "
    When I navigate to another page via pagination controls
    Then the lastName value used in subsequent requests is "Fra"

  @COD-80 @Edge
  Scenario: All pagination controls preserve active filters
    Given I have searched owners with lastName "Fra" and city "Madison" and the results have more than one page
    When the owners list page is displayed
    Then every pagination control link includes "lastName=Fra"
    And every pagination control link includes "city=Madison"
