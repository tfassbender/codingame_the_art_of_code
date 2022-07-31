# Codingame - The Art of Code

A multiplayer game for [CodinGame](https://www.codingame.com). A bit like the boardgame Risk but without dice, so the battles don't include a random factor.

## Rules

The Art of Code is a multiplayer bot programming game, in which two players (bots) play against each other to conquer fields on a map, by moving and deploying troops.

#### Goal

The goal of the game is to **conquer all fields on the game map**. Some of the fields have connections to other fields, allong which the troops of a player can be moved to conquer adjacent fields. To conquer fields, both players gain new troops every turn that can be deployed to the fields that a player controlls. If none of the players manages to take controll of all fields after 200 turns the player that controlls more fields winns.

#### The map

The map consists of a random number of fields. Some of these fields are connected, so troops can move between the fields. 

The **map is symmetric**, so none of the players can gain an advantage from a unique area on the map.

The **fields on the map are grouped into regions**. Every region consists of multiple fields and grants a bonus to the player who manages to conquer the whole region.


#### Turns

The game is played in three kinds of turns:
- CHOOSE_STARTING_FIELDS: the players can choose the starting fields, that each gain one troops
- DEPLOY_TROOPS: the players can deploy new troops to every field they controll
- MOVE_TROOPS: the players can move their troops to attack the opponent and conquer more fields

#### Chosing the starting fields

At the beginning of the game, both players receive the information about the fields and their connections (among other information). Then the players can choose a given number of starting fields (one per turn). After choosing a starting field, the player gains controll of the field and **1 troop is deployed to it**.

Both players choose a starting field simultaneously. Therefore it is possible that both players choose the same starting field in a single turn (a field that was already chosen in a previous turn cannot be chosen anymore). To prevent a tie when both players choose the same starting field the map is divided into two halfs (the map is symmetric). On one half of the map the first player will get the field that both players chose, while on the other half of the map, the second player gets the field (the players are infomed on which half of the map they would win a tie at the beginning of the game). The player that did not get the field, can take one turn more to choose a starting field, so both players have the same number of starting fields.

After all starting fields are chosen, every field that was not chosen is taken by a **neutral army with 2 troops per field**. These troops will not attack, but just stay on a field and defend it.

#### Deploying troops

After choosing the starting fields the turns in which the players deploy new troops and move the troops on the map alternate. When troops need to be deployed in a turn, the players get the number of troops that can be deployed as an input. The players then can deploy any number of troops to any of their fields, till the maximum number of deployable troops is reached.

This is done in a single turn and simultaneously, so the **players won't know where the opponent deployed his troops**.

In every turn, each of the players can deploy at least 5 troops. The number of additional troops that can be deployed depends on:
- The number of fields that a player controlls: 1 additional troop per 3 fields, that a player controlls (rounded down)
- The number of regions that a player controlls: Additional troops depending on the size of the region
- The number of deployable troops from the last turn that were not used: Every troop that is not deployed in a turn, can be deployed in one of the following turns
- Whether it is the first deployment turn: In the first deployment turn, every player gains 10 additional troops to deploy.
- Rounding loss bonus troops: described in the next chapter

#### Movement of troops

After deploying the troops to the fields, each player can choose a list of movements, that are **all executed in the same turn, so the player doesn't know about the movements of the opponent troops**. The information about how many troops are present on every field of the map is given as an input at the start of this turn.

The lists of movements of both players are executed simultaneously. This means that the first move from the first player's list is executed simultaneously to the first move from the second player's list. Afterwards the second move from the first player's list is executed simultaneously to the second move from the second player's list ...

If a movement is done from one field to another field that the same player controlls, the given number of troops are just moved to the next field.

If one player moves his troops to a field that is controlled by the opponent player (or by a neutral army), the troops battle for the controll of the field. In a battle, the **attacking troops will kill 60% of opponent troops**, so 10 attacking troops would kill 6 opponent troops. **Defending troops kill 70% of opponent troops**, so 10 defending troops would kill 7 opponent (attacking) troops. These values are always **rounded up** so 3 attacking troops would kill 2 defending troops and 2 defending roops would kill 2 attacking troops.  
This means that every player looses a bit more troops than he would if the calculation was preceise. Therefore the **additionally killed troops (the difference in the decimal places) are summed up and given back to the player as additional troops that can be deployed in the next turn**. So if a player attacks an army of 2 defenders on two different fields he would loose 2 troops in each of this battles. The additionally killed troops in each of this battles would be 0.6 (2.0 - (2 * 0.7)). This would sum up to 1.2, so the player would get 1 additional troop that can be deployed in the next turn (this value is **rounded down** and **reset after each deployment phase**).

If the simultaneous movement of two armies causes both armies to meat on the battlefield, there is **no defending army** at first. Both moving armies are the attackers in this case, so **both kill 60% of opponent troops**. This movement is executed in **two steps**:
- In the first step, the **two moving armies fight each other**. All other troops on the fields, that are not moved **do not take part in this first battle**.
- In the second step, the remaining troops of both armies may move, according to the following rules:
  - If none of the moving armies survives the first battle: No troops are moved
  - If one of the moving armies survives the first battle: The surviving army may continue the movement, which can lead to a second battle if the movement leads to an opponents field. Only the surviving troops are moved in this case (although there might be more troops left on the attacking field).
  - If both of the moving armies survive the first battle: No second attack is executed, but movements that don't lead to a battle are executed by the surviving troops (that is the case if troops move to another field that is controlled by the same player)

After all movements are executed the number of deployable troops is calculated and both players can deploy new troops in the next turn.

## Tasks

- [:heavy_check_mark: Tobias - master] Create the basic class structure
- [:heavy_check_mark: Dominik - master] Create a hard-coded map (for testing)
- [:heavy_check_mark: Tobias - referee_send_move, master] Let the referee send the current game state to the players
- [:heavy_check_mark: Dominik - referee_parse_move, master] Let the referee parse the players moves to Actions (that can be executed)
- [:heavy_check_mark: Tobias - referee_validate_actions, master] Validate, that all actions from the player are valid and can be executed
- Execute moves (in the map class)
  - [:heavy_check_mark: Tobias - execute_choose_starting_field, master]Choose starting field
  - [:heavy_check_mark: Tobias - execute_deploy_and_move, master] Deploy troop / Move troop
  - [:heavy_check_mark: Tobias - calculate_deployed_troops, master] calculate the number of troops to be deployed in a turn
- Implement a simple view (for testing)
  - [:heavy_check_mark: Tobias - master] Draw background, player icons and some info
  - [:heavy_check_mark: Dominik - simple_view, master] Draw regions and fields
  - [:hammer_and_wrench: Dominik - simple_view] Animate actions
- More adjustments for the Referee
  - [:heavy_check_mark: Tobias - referee_adjustments, master] Skip player in initial phase, if no fields can be chosen anymore
  - [:heavy_check_mark: Tobias - referee_adjustments, master] Detect game ending (no fields left)
  - [:heavy_check_mark: Tobias - referee_adjustments, master] Find out who won the game at the end
- [:heavy_check_mark: Dominik - simple_bot, master] Create a simple bot (for testing)
- Generate random maps
  - [:hammer_and_wrench: Tobias - map_generator] Generate a random, symmetric field graph
  - [:hammer_and_wrench: Tobias - map_generator] Calculate the positions of the random fields on the map
- Create boss bots (not in this repo, so they remain private)
- [:heavy_check_mark: Tobias - master] Define the Rules (in this .md file)
- [:heavy_check_mark: Tobias - master] Add the Rules to `config/statement_en.html`

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
