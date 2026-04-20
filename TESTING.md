# Map de tests — Numismatic Reimagined 2.0

Checklist exhaustive à valider in-game sur **Fabric** ET **NeoForge** avant release stable.

## Setup

1. `./gradlew :fabric:runClient` ou `./gradlew :neoforge:runClient`
2. Mode créatif, monde plat, peace.
3. Donner les prérequis : `/numismatic give @s 1000000`

---

## Bloc 1 — Currency core

| # | Test | Attendu | Fabric | NeoForge |
|---|---|---|---|---|
| 1.1 | Inventaire créatif → onglet "Numismatic Reimagined" | 4 coins + money bag + star coin + 3 piggy banks + 5 shops + request board | ☐ | ☐ |
| 1.2 | Coin tooltip | Affiche le nom traduit en fr/en | ☐ | ☐ |
| 1.3 | `/numismatic balance` | Affiche le solde courant | ☐ | ☐ |
| 1.4 | `/numismatic give @s 12345` | Solde augmente de 12345 (vérifier via `/numismatic balance`) | ☐ | ☐ |
| 1.5 | `/numismatic set @s 0` (op) | Solde devient 0 | ☐ | ☐ |

## Bloc 2 — Bourse / Purse overlay

| # | Test | Attendu | Fabric | NeoForge |
|---|---|---|---|---|
| 2.1 | Ouvrir l'inventaire (E) | Bouton money bag visible en haut à droite (~152, 6 du leftPos) | ☐ | ☐ |
| 2.2 | Clic sur le bouton | Popup s'ouvre avec 4 lignes (NETHERITE/GOLD/SILVER/BRONZE) | ☐ | ☐ |
| 2.3 | Bouton + sur GOLD | Pending augmente, total bottom +10000 | ☐ | ☐ |
| 2.4 | Shift+clic + | Pending +10 | ☐ | ☐ |
| 2.5 | Bouton - | Pending diminue | ☐ | ☐ |
| 2.6 | Clic "Extract" | Coins apparaissent en inventaire, balance diminue | ☐ | ☐ |
| 2.7 | Re-ouvrir inventaire | Popup ne reste PAS ouvert | ☐ | ☐ |
| 2.8 | Pas de fond | Ne droppe pas en dessous de 0 | ☐ | ☐ |

## Bloc 3 — Tirelires (Piggy Bank)

| # | Test | Attendu | Fabric | NeoForge |
|---|---|---|---|---|
| 3.1 | Place piggy_bank, casse-le | Aucun coin droppé (vide) | ☐ | ☐ |
| 3.2 | `/numismatic give @s 5000` puis casser piggy_bank avec coins en inv | Pas affecté (pas encore d'auto-deposit) | ☐ | ☐ |
| 3.3 | Place golden_piggy_bank et netherite_piggy_bank | Tous trois textures distinctes | ☐ | ☐ |
| 3.4 | Place piggy_bank, fais tomber un golem dessus depuis 10 blocs | Le bloc se brise (tag VERY_HEAVY) | ☐ | ☐ |

## Bloc 4 — Boutiques (5 tiers)

| # | Test | Attendu | Fabric | NeoForge |
|---|---|---|---|---|
| 4.1 | Place les 5 tiers | bronze/silver/gold/netherite/admin, textures distinctes | ☐ | ☐ |
| 4.2 | Casse-les | Drop bien le bon item | ☐ | ☐ |

*Note : la GUI shop n'est pas encore implémentée en 2.0-alpha.1 (Phase 4 backend uniquement).*

## Bloc 5 — Reverse shop (Request board)

| # | Test | Attendu | Fabric | NeoForge |
|---|---|---|---|---|
| 5.1 | Place request_board | Texture oak planks distincte | ☐ | ☐ |

*Note : la GUI request board n'est pas encore implémentée (Phase 5 backend uniquement).*

## Bloc 6 — Star Coin

| # | Test | Attendu | Fabric | NeoForge |
|---|---|---|---|---|
| 6.1 | Donner star_coin via creative | Apparaît avec nom violet (Rarity.EPIC) | ☐ | ☐ |
| 6.2 | Lance dans la lave | Ne brûle pas (fireResistant) | ☐ | ☐ |

## Bloc 7 — Mob drops

| # | Test | Attendu | Fabric | NeoForge |
|---|---|---|---|---|
| 7.1 | Tuer 50 zombies | Environ 30% droppent des coins (selon health) | ☐ | ☐ |
| 7.2 | Tuer enderdragon (créatif) | Drop important (max health élevé × 2 × variance) | ☐ | ☐ |

## Bloc 8 — Villager trades

| # | Test | Attendu | Fabric | NeoForge |
|---|---|---|---|---|
| 8.1 | Spawner librarian level 1 | Trade : 1 emerald → 1 silver_coin | ☐ | ☐ |
| 8.2 | Faire monter librarian level 3 | Trade : 1 emerald → 1 gold_coin | ☐ | ☐ |

## Bloc 9 — Compatibilité

| # | Test | Attendu | Fabric | NeoForge |
|---|---|---|---|---|
| 9.1 | Ouvrir JEI (R sur un coin) | Apparaît dans JEI (les coins ont des recipes via crafting de base) | ☐ | ☐ |
| 9.2 | Pointer un piggy bank | Jade affiche "Tirelire" + texture item | ☐ | ☐ |
| 9.3 | Pointer un shop | Jade affiche le tier | ☐ | ☐ |

## Bloc 10 — Network / Sync

| # | Test | Attendu | Fabric | NeoForge |
|---|---|---|---|---|
| 10.1 | `/numismatic give @s 5000` puis ouvrir purse | Solde 5000 visible immédiatement | ☐ | ☐ |
| 10.2 | Extract via popup → check `/numismatic balance` | Balance correctement diminuée | ☐ | ☐ |
| 10.3 | Logout + relog | Balance préservée (SavedData) | ☐ | ☐ |

## Bloc 11 — Build & CI

| # | Test | Attendu |
|---|---|---|
| 11.1 | `./gradlew build` | BUILD SUCCESSFUL en local |
| 11.2 | GitHub Actions Build & Test | Vert sur push feature branch |
| 11.3 | `./gradlew :common:test :common:jacocoTestCoverageVerification` | Coverage ≥96% sur logique pure |
| 11.4 | JARs distribuables | `fabric/build/libs/*.jar` + `neoforge/build/libs/*.jar` non vides |

---

## Bugs connus / Out of scope 2.0-alpha.1

- GUI Shop (Phase 4) : backend uniquement, pas de Screen pour gérer les offres → reporté en 2.0-beta
- GUI Request Board (Phase 5) : backend uniquement → reporté en 2.0-beta
- Achievements : pas de trigger auto pour Star Coin → reporté
- Loot table chests injection : drops mob seulement, pas chests vanilla → reporté
- Config menu owo : pas de config UI → utiliser commands

## Procédure de release

1. Cocher tous les ☐ critiques (Bloc 1, 2, 11) sur Fabric ET NeoForge.
2. Mettre à jour `mod_version` dans `gradle.properties` (`2.0.0-alpha.1` → `2.0.0`).
3. Mettre à jour `CHANGELOG.md` (passer Unreleased → 2.0.0 + date).
4. Créer un tag `v2.0.0` et le push : déclenchera `release.yml` qui publie sur Modrinth + CurseForge.
