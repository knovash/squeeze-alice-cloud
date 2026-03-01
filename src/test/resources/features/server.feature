Feature: Testing Server Endpoints

  Scenario: Check GET request to /alice/
    Given the server is running
    When I send a GET request to "/alice/"
    Then the response status code should be 200