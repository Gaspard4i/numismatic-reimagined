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

## Phase 4 : Shop Block (redesigné)
- [x] Implémenté
- [x] Tests unitaires (ShopOffer 92%, OfferList 99%, ShopRevenue 100%, ShopStockOps 99%, ShopMenuMode 100%, global 98%)
- [ ] Testé manuellement par l'utilisateur
- [ ] Issues résolues
- Notes : 2 blocs (ShopBlock craftable, AdminShopBlock unbreakable+epic). Owner = placeur. ShopBlockEntity (27 slots stock + 81 offres + revenu). 3 onglets owner (Offers/Stock/Client). Logique pure extraite dans ShopStockOps + ShopRevenue. Validation utilisateur en attente (checklist dans rapport commit).

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
