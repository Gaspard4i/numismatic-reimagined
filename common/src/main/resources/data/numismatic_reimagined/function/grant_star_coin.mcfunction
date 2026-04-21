# Awarded via the star_coin advancement. Gives one Star Coin if the player
# does not already hold one in their inventory.
execute unless entity @s[nbt={Inventory:[{id:"numismatic_reimagined:star_coin"}]}] run give @s numismatic_reimagined:star_coin 1
