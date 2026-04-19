# Numismatic Reimagined - État du Projet

## Phase 1 : Squelette + Items de monnaie
- [x] Implémenté
- [x] Tests unitaires (83 tests, 0 failures — CurrencyTest:19, CurrencyResolverTest:28, CurrencyConverterTest:36)
- [x] Testé manuellement par l'utilisateur
- [x] Issues résolues
- Notes : Build OK (common+fabric). Module Forge supprimé. Textures finales en place (MIT + recoloration netherite/star). Stack max 99 avec texture variants (5 seuils). JEI intégré. Fonctions mcfunction de test. Creative tab dédié. RAM réduite à 4G. MoneyBag non implémenté (reporté, complexité). En pause — prêt pour Phase 2.

## Phase 2 : Stockage de monnaie / Système Purse
- [x] Implémenté
- [ ] Tests unitaires (coverage: ?%)
- [ ] Testé manuellement par l'utilisateur
- [ ] Issues résolues
- Notes : PlayerCurrencyManager (SavedData) + Networking S2C/C2S (NumismaticNetworking) + PurseHudOverlay + commandes /numismatic balance/deposit/withdraw/set/give/give_bag implémentés. Coins et MoneyBag se déposent au clic droit. Validation utilisateur en attente.

## Phase 3 : Piggy Bank
- [x] Implémenté
- [ ] Tests unitaires (coverage: ?%)
- [ ] Testé manuellement par l'utilisateur
- [ ] Issues résolues
- Notes : 3 variantes (base/golden/netherite) avec capacités max-1 (9999B / 999999B / 999999999B). Clic droit = insère 1 unité avec rendu du change en money bag. Shift-clic = dépose tout l'inventaire. Silk touch préserve les contenus. Recettes craft. Stack à 99 via mixin Slot. Bug créatif → inventaire en cours de validation.

## Phase 4 : Shop Block (redesigné)
- [x] Implémenté
- [ ] Tests unitaires (coverage: ?%)
- [ ] Testé manuellement par l'utilisateur
- [ ] Issues résolues
- Notes : 2 blocs (ShopBlock craftable, AdminShopBlock unbreakable+epic). Owner = placeur. ShopBlockEntity (27 slots stock + 81 offres + revenu). 3 onglets owner (Offers/Stock/Client). Achat coins/bags inventaire uniquement (jamais purse). Drop stock+revenu si cassé. Indéplaçable par piston. Permissions OP2 admin shop (compatible LuckPerms). Validation utilisateur en attente.

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
