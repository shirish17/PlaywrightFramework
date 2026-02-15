@smoke
Feature: System configuration page

  As a user with proper permission
  I want to manage system configuration
  So that I can maintain country data

  #Background:
    #Given I login as role "creator"
#@high
  Scenario Outline: User with create role able to add a new country with active status
    Given I login as role "creator"
    #When the user is on the country management page
    #And the user adds a country named "<country>" and activates it
    #Then the country "<country>" appears in the list

    Examples:
      | country           |
      | Auto_CountryName  |  
      
  
  Scenario Outline: User with edit role able to edit an existing country
    Given I login as role "editor"
    #When the user is on the country management page
    #And the user adds a country named "<country>" and activates it
    #Then the country "<country>" appears in the list

    Examples:
      | country           |
      | Auto_CountryName  |    
  