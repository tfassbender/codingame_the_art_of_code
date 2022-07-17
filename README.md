# Codingame - The Art of Code

A multiplayer game for [CodinGame](https://www.codingame.com). A bit like the boardgame Risk but without dice, so the battles don't include a random factor.

## Rules

TODO add rules

## Tasks

- [:heavy_check_mark: Tobias - master] Create the basic class structure
- [:heavy_check_mark: Dominik - master] Create a hard-coded map (for testing)
- [:heavy_check_mark: Tobias - referee_send_move, master] Let the referee send the current game state to the players
- [:heavy_check_mark: Dominik - referee_parse_move, master] Let the referee parse the players moves to Actions (that can be executed)
- [:hammer_and_wrench: Tobias - referee_validate_actions] Validate, that all actions from the player are valid and can be executed
- Execute moves (in the map class)
  - Choose starting field
  - Deploy troop / Move troop
- Implement a simple view (for testing)
- Create a simple bot (for testing)
- Create boss bots (not in this repo, so they remain private)
- Define the Rules (in this .md file)
- Add the Rules to `config/statement_en.html`

## Class and package structure

Game classes:
- package: game
  - Player: the bot that plays the game
  - Referee: handles player outputs, sends inputs to the players, ... (executes moves here, so they can be drawn in time)
  - Action: defines an action that the player submitted
  - League: enum to define the league and the league rules
- package: game.core
  - Field: a field of the game, that is to be conquered
  - Region: a group of fields, that define a region (for more units)
  - Map: the map of the game, that includes the fields
  - Movement: the movement of troops, that can be executed on a map
  - Owner: the owner of a field
- package: game.build
  - Random: provides random functions and the used seed
  - MapGenerator: generates a map graph
- package: game.view
  - View: draws the map and other stuff onto the screen
