#> numismatic_reimagined:debug/give_god_pickaxe
# Give the caller a pair of god-tier netherite pickaxes :
#  - "Fortune Breaker" : Efficiency 255 + Fortune 10 + Unbreaking 10 + instant break
#  - "Silk Snatcher"   : Efficiency 255 + Silk Touch + Unbreaking 10 + instant break
# Both stacks are Unbreakable so durability never drops.

give @s netherite_pickaxe[minecraft:custom_name='{"text":"Fortune Breaker","color":"gold","italic":false}',minecraft:unbreakable={},minecraft:enchantments={levels:{"minecraft:efficiency":255,"minecraft:fortune":10,"minecraft:unbreaking":10},show_in_tooltip:true},minecraft:attribute_modifiers={modifiers:[{type:"minecraft:generic.attack_damage",amount:9,operation:"add_value",slot:"mainhand",id:"minecraft:base_attack_damage"},{type:"minecraft:generic.attack_speed",amount:1000,operation:"add_value",slot:"mainhand",id:"minecraft:base_attack_speed"}]}]

give @s netherite_pickaxe[minecraft:custom_name='{"text":"Silk Snatcher","color":"aqua","italic":false}',minecraft:unbreakable={},minecraft:enchantments={levels:{"minecraft:efficiency":255,"minecraft:silk_touch":1,"minecraft:unbreaking":10},show_in_tooltip:true},minecraft:attribute_modifiers={modifiers:[{type:"minecraft:generic.attack_damage",amount:9,operation:"add_value",slot:"mainhand",id:"minecraft:base_attack_damage"},{type:"minecraft:generic.attack_speed",amount:1000,operation:"add_value",slot:"mainhand",id:"minecraft:base_attack_speed"}]}]

# Matching haste beacon effect so mined blocks break instantly client-side too.
effect give @s minecraft:haste 999999 4 true
