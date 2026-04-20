# Strip everything currency-related from the executing player : purse
# balance set to 0, inventory coins/bags cleared, advancement reset.
numismatic set @s 0
clear @s numismatic_reimagined:bronze_coin
clear @s numismatic_reimagined:silver_coin
clear @s numismatic_reimagined:gold_coin
clear @s numismatic_reimagined:netherite_coin
clear @s numismatic_reimagined:money_bag
clear @s numismatic_reimagined:star_coin
advancement revoke @s only numismatic_reimagined:star_coin
tellraw @s {"text":"[Numismatic] Player reset.","color":"yellow"}
