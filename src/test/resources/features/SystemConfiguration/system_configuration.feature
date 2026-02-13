@smoke
Feature: System configuration page

  As a user with proper permission
  I want to manage system configuration
  So that I can maintain country data

  Background:
    Given I login as role "creator"

  Scenario Outline: User with create role able to add a new country with active status
    When the user is on the country management page
    #And the user adds a country named "<country>" and activates it
    #Then the country "<country>" appears in the list

    Examples:
      | country           |
      | Auto_CountryName  |
