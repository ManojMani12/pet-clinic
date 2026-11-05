@COD-98
Feature: Make Owner search results page size configurable
  As a clinic admin
  I want the Owner search results page size to be configurable
  So that paging can be tuned without code changes

  @COD-98 @AC-1
  Scenario: Default owner page size is 5 when not configured
    Given no "petclinic.owners.pageSize" is configured
    When I search owners and view results
    Then the results page contains at most 5 owners

  @COD-98 @AC-2
  Scenario: Configured owner page size is applied
    Given "petclinic.owners.pageSize" is configured to 10
    When I search owners and view results
    Then the results page contains at most 10 owners

  @COD-98 @AC-3
  Scenario: Non-positive configured page size falls back to 5
    Given "petclinic.owners.pageSize" is configured to 0
    When I search owners and view results
    Then the results page contains at most 5 owners
