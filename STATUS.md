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

## Phase 5 : Configuration
- [ ] Implémenté
- [ ] Tests unitaires (coverage: ?%)
- [ ] Testé manuellement par l'utilisateur
- [ ] Issues résolues
- Notes : -

## Phase 6 : Loot Tables + Mob Drops
- [ ] Implémenté
- [ ] Tests unitaires (coverage: ?%)
- [ ] Testé manuellement par l'utilisateur
- [ ] Issues résolues
- Notes : -

## Phase 7 : Trades Villageois
- [ ] Implémenté
- [ ] Tests unitaires (coverage: ?%)
- [ ] Testé manuellement par l'utilisateur
- [ ] Issues résolues
- Notes : -

## Phase 8 : Marketplace (commandes + GUI)
- [ ] Implémenté
- [ ] Tests unitaires (coverage: ?%)
- [ ] Testé manuellement par l'utilisateur
- [ ] Issues résolues
- Notes : -

## Phase 9 : Achievements / Star Coin
- [ ] Implémenté
- [ ] Tests unitaires (coverage: ?%)
- [ ] Testé manuellement par l'utilisateur
- [ ] Issues résolues
- Notes : -

## Phase 10 : Sécurité (hardening)
- [ ] Implémenté
- [ ] Tests unitaires (coverage: ?%)
- [ ] Testé manuellement par l'utilisateur
- [ ] Issues résolues
- Notes : -

## Phase 11 : API Addon
- [ ] Implémenté
- [ ] Tests unitaires (coverage: ?%)
- [ ] Testé manuellement par l'utilisateur
- [ ] Issues résolues
- Notes : -

## Phase 12 : Port NeoForge + MC 1.21.1
- [ ] Implémenté
- [ ] Tests unitaires (coverage: ?%)
- [ ] Testé manuellement par l'utilisateur
- [ ] Issues résolues
- Notes : -
