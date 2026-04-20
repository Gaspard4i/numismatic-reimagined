# Gives one empty piggy bank of each tier, plus a few preset-full ones
# for testing the stack-to-1 mixin.
give @s numismatic_reimagined:piggy_bank 3
give @s numismatic_reimagined:golden_piggy_bank 3
give @s numismatic_reimagined:netherite_piggy_bank 3
# Preset full piggy banks (NBT inline) — for silk-touch and drop testing.
give @s numismatic_reimagined:piggy_bank{BlockEntityTag:{stored:9999L}} 1
give @s numismatic_reimagined:golden_piggy_bank{BlockEntityTag:{stored:500000L}} 1
give @s numismatic_reimagined:netherite_piggy_bank{BlockEntityTag:{stored:999999999L}} 1
