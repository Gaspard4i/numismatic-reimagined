# One-shot test environment : clears inventory, fills purse with 1M,
# gives a full coin sample + one of every block + 10 diamonds / 10 iron
# for shop-fill testing.
clear @s
function numismatic_reimagined:debug/give_all_coins
function numismatic_reimagined:debug/give_all_shops
function numismatic_reimagined:debug/give_all_piggys
give @s numismatic_reimagined:request_board 1
give @s minecraft:diamond 64
give @s minecraft:iron_ingot 64
give @s minecraft:hopper 16
numismatic give @s 1000000
tellraw @s {"text":"[Numismatic] Test environment ready.","color":"green"}
