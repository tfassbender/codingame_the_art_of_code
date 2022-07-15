# Codingame - The Art of Code

A multiplayer game for [CodinGame](https://www.codingame.com). A bit like the boardgame Risk but without dice, so the battles don't include a random factor.

## Rules

TODO add rules

## Tasks

- [:heavy_check_mark: Tobias - master] Create the basic class structure
- Create a hard-coded map (for testing)
- Let the referee send the current game state to the players
- Let the referee parse the players moves to Actions (that can be executed)
- Execute moves (in the map class)
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
