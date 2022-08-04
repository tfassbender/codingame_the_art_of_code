import sys
import math

# Defeat the enemy army.

number_of_regions = int(input())  # Number of regions (field groups) on the map
for i in range(number_of_regions):
    # bonus_troops: Number on bonus troops, if one has all fields in the region
    region_id, bonus_troops = [int(j) for j in input().split()]
number_of_fields = int(input())  # Number of fields on the map
for i in range(number_of_fields):
    # field_id: Id of one field, that belongs to the region
    field_id, region_id = [int(j) for j in input().split()]
number_of_connections = int(input())  # Number of connections between the fields (bidirectional)
for i in range(number_of_connections):
    field_aid, field_bid = [int(j) for j in input().split()]
priority = input()  # Either UPPER or LOWER - the part of the field (identified by id) in which you have the higher priority to choose a starting field

# game loop
while True:
    turn_type = input()  # CHOOSE_STARTING_FIELDS, DEPLOY_TROOPS or MOVE_TROOPS
    players_field = int(input())  # Number of fields, that you control
    enemy_fields = int(input())  # Number of fields, that the enemy control
    deployable_troops = int(input())  # Number of troops, that can be deployed
    enemy_deployable_troops = int(input())  # Number of troops, that the enemy can deploy
    starting_fields_left = int(input())  # Number of fields you can still choose
    enemy_starting_fields_left = int(input())  # Number of fields that the enemy can still choose
    for i in range(number_of_fields):
        # owner: 1 if the field is controlled by you; 2 if it's controlled by the opponent player; 0 if it's neutral
        _id, troops, owner = [int(j) for j in input().split()]

    # Write an action using print
    # To debug: print("Debug messages...", file=sys.stderr, flush=True)


    # PICK id | RANDOM | DEPLOY id troops | MOVE sourceId targetId troops | WAIT
    print("WAIT")
