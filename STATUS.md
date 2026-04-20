# Numismatic Reimagined - État du Projet

## Phase 1 : Squelette + Items de monnaie
- [x] Implémenté
- [x] Tests unitaires (Currency 100%, CurrencyConverter 100%, CurrencyResolver 100%)
- [x] Testé manuellement par l'utilisateur
- [x] Issues résolues
- Notes : Build OK (common+fabric+forge). Textures finales (MIT + recoloration netherite/star). Stack max 99 avec texture variants (5 seuils). JEI intégré. Fonctions mcfunction de test. Creative tab dédié. MoneyBag implémenté en Phase 2.

## Phase 2 : Stockage de monnaie / Système Purse
- [x] Implémenté
- [x] Tests unitaires (SimpleCurrencyStorage 96%)
- [x] Testé manuellement par l'utilisateur (commandes balance/deposit/withdraw/set/give/give_bag OK)
- [x] Issues résolues
- Notes : PlayerCurrencyManager (SavedData) + Networking S2C/C2S + PurseHudOverlay + 6 commandes. Coins et MoneyBag se déposent au clic droit.

## Phase 3 : Piggy Bank
- [x] Implémenté
- [x] Tests unitaires (PiggyBankAccount 100%)
- [x] Testé manuellement par l'utilisateur
- [x] Issues résolues (bug créatif corrigé via SlotMixin + CreativeSlotPacketMixin)
- Notes : 3 variantes (base/golden/netherite) avec capacités max-1 (9999B / 999999B / 999999999B). Shift-clic = dépose stack en main. Silk touch préserve les contenus. Logique pure extraite dans PiggyBankAccount pour testabilité.

## Phase 4 : Shop Block (refonte avec lib UI custom)
- [x] Implémenté (3 lots: lib UI, purse popup, shop refonte)
- [x] Tests unitaires (ShopOffer + OfferList, global 98% sur testable)
- [ ] Testé manuellement par l'utilisateur
- [ ] Issues résolues
- Notes :
  * Lib UI custom dans common/client/widgets/ (GuiTextures, CompositeWidget, IconButton, TabSelector, ScrollableList) inspirée de Create
  * UI Purse popup accessible via keybind P (configurable) depuis le monde ou l'inventaire vanilla, avec sliders +/- par dénomination (bronze/silver/gold/netherite)
  * Shop refonte: ShopBlock + AdminShopBlock, ShopBlockEntity étendu (owner UUID, isAdmin, OfferList, revenu)
  * ShopMenu vanilla AbstractContainerMenu (27 stock + player inv)
  * ShopScreen avec 2 onglets (Offers / Stock) + bouton "Test mode" toggle
  * OfferEditScreen popup pour créer/éditer offres
  * ShopPaymentHelper paie depuis inventaire physique uniquement
  * 4 packets shop + sync state S2C
  * Max 56 offres par shop
  * Stack de coins ramené à 64 vanilla (suppression de 3 mixins fragiles)

## Phase 5a : Hitbox PiggyBank directionnelle
- [x] Implémenté (commit NR-120)
- [x] Tests unitaires (PiggyBankShapesTest 6 tests)
- [ ] Testé manuellement par l'utilisateur
- Notes : 4 VoxelShapes rotées (N/S/E/W), corps+snout+slot+4 pieds. Logique de rotation extraite dans PiggyBankShapes (pur).

## Phase 5b : Tooltip coins avec icônes
- [x] Implémenté (commit NR-121)
- [x] Tests unitaires (CurrencyTooltipDataTest 11 tests)
- [ ] Testé manuellement par l'utilisateur
- Notes : CurrencyTooltipData (pur) + CurrencyTooltipComponent (client). Tooltips coin/bag affichent icônes empilées. Enregistrés Fabric + Forge.

## Phase 5c : MoneyBag click L/R
- [x] Implémenté (commit NR-122)
- [x] Tests unitaires (MoneyBagClickLogicTest 15 tests)
- [ ] Testé manuellement par l'utilisateur
- Notes : MoneyBagClickLogic.absorbCoins/absorbBag/extractLargestDenom. Override PRIMARY=absorb, SECONDARY+empty=extract. CoinItem aligné sur PRIMARY.

## Phase 6 : Shop hopper transfer toggle
- [x] Implémenté (commit NR-123)
- [x] Tests unitaires (ShopTransferLogicTest 8 tests)
- [ ] Testé manuellement par l'utilisateur
- Notes : allowsTransfer + WorldlyContainer. canPlaceItem via ShopTransferLogic.canHopperInsert (whitelist offers). Bouton H: ON/OFF sous les tabs owner.

## Phase 7 : Shop 5 tiers
- [x] Implémenté (commit NR-124)
- [x] Tests unitaires (ShopTierTest 8 tests)
- [ ] Testé manuellement par l'utilisateur
- Notes : BRONZE(9/3), SILVER(18/6), GOLD(27/12, legacy shop_block), NETHERITE(36/24), ADMIN(27/56). ShopBlock(Properties, ShopTier). ShopMenu + ShopScreen dynamiques (rows 1..4). Recipes bronze/silver/netherite + blockstates/models/lang.

## Phase 8 : Advancement Star Coin
- [x] Implémenté (commit NR-125)
- [x] Tests unitaires (AccumulationTrackerTest 10 tests)
- [ ] Testé manuellement par l'utilisateur
- Notes : AccumulationTracker SavedData (monotonic counter). PlayerCurrencyManager.addBalanceAndTrack fire CollectNetheriteTrigger à 1000 netherite cumulés → advancement → mcfunction grant_star_coin.

## Phase 9 : Purse HUD + popup 4 denoms
- [x] Implémenté (commit NR-126)
- [x] Tests unitaires (PurseExtractLogicTest 10 tests)
- [ ] Testé manuellement par l'utilisateur
- Notes : PurseHudOverlay affiche icône + total. Keybind P ouvre PurseScreen (Fabric + Forge). PurseExtractLogic pour incrément/decrement avec shift mult + clamp.

## Phase 10 : Reverse shop / Request Board
- [x] Implémenté (commit NR-127)
- [x] Tests unitaires (RequestOfferTest 11, RequestOfferListTest 11, RequestFulfillLogicTest 12)
- [ ] Testé manuellement par l'utilisateur
- Notes : RequestBoardBlock + BE + RequestOffer + RequestOfferList + RequestFulfillLogic (pur, matching strictNbt et undamaged). Commandes `/numismatic request fund/add/remove/deliver` (raycast 5 blocs). UI dédiée reportée post-beta.

## Phase 11 : Polish + release beta
- [x] Implémenté (commit NR-128)
- [x] Coverage JaCoCo ≥ 96% sur le code testable
- [ ] Testé end-to-end sur Fabric + Forge par l'utilisateur
- Notes : Tag v0.1.0-beta. README + STATUS.md synchronisés.

## Post-beta (port 1.21.1 NeoForge)
- Configuration (game rules, prices, caps)
- Loot tables + mob drops
- Villager trades
- API publique pour addons
- NeoForge + MC 1.21.1 (+ owo-lib native pour l'UI)
