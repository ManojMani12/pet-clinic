@COD-128
Feature: Configurable owner search page size preserved during paging
  As a clinic staff user
  I want the Owner search results page size to be configurable and preserved while paging
  So that the results list can be tuned for usability without code changes

  # Assumption: a request query parameter named "size" can be used to set page size.
  # Assumption: invalid size values (non-numeric, <= 0) fall back to default; values > 100 are clamped to 100.

  @COD-128 @AC-1
  Scenario: Non-default page size is preserved when paging
    Given I am on the Find Owners page
    When I search owners with page size 10
    Then I should see the owners list results
    When I go to the next page of owners results
    Then the owners results should still use page size 10

  @COD-128 @AC-2
  Scenario Outline: Invalid page size values fall back to the default
    Given I am on the Find Owners page
    When I search owners with page size <size>
    Then I should see the owners list results
    And the owners results should use the default page size

    Examples:
      | size |
      | 0    |
      | -1   |

  @COD-128 @AC-3
  Scenario: Too-large page size is clamped to the maximum
    Given I am on the Find Owners page
    When I search owners with page size 1000
    Then I should see the owners list results
    And the owners results should use page size 100

  # NEW STEP - needs step definition
  # I search owners with page size {int}

  # NEW STEP - needs step definition
  # The owners results should still use page size {int}

  # NEW STEP - needs step definition
  # The owners results should use the default page size

  # NEW STEP - needs step definition
  # The owners results should use page size {int}
