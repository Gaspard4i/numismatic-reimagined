# Changelog

Toutes les notes de version sont au format [Keep a Changelog](https://keepachangelog.com/fr/1.1.0/),
et ce projet suit [SemVer](https://semver.org/lang/fr/).

## [Unreleased] — 2.0.0-alpha.1

### Ajouté

- **Système de monnaie 4 dénominations** : Bronze (1), Silver (100), Gold (10 000), Netherite (1 000 000) — la dénomination netherite est exclusive à Reimagined.
- **Bourse joueur** : popup overlay sur l'inventaire vanilla avec extract incrémental par dénomination (shift = ×10).
- **Tirelires 3 tiers** : base (9 999), dorée (999 999), netherite (999 999 999). Cassez-la → contenu déposé dans la bourse du joueur.
- **Boutiques 5 tiers** : bronze (9/3), silver (18/6), gold (27/12), netherite (36/24), admin (27/56) — blocs placeables.
- **Panneau de requêtes** (reverse shop) : déposez des fonds + listez ce que vous voulez acheter, les joueurs livrent.
- **Star Coin** : item trophée légendaire (Rarity.EPIC).
- **Drops mobs** : tous les mobs ont une chance (30% par défaut, ±40% variance) de drop des coins basés sur leur max health.
- **Trades villageois** : le bibliothécaire échange émeraudes ↔ silver/gold coins (niveaux 1 et 3).
- **Commandes** : `/numismatic balance`, `/numismatic give <player> <amount>`, `/numismatic set <player> <amount>` (op).

### Compatibilité

- **Out-of-the-box** : JEI / REI / EMI (recipes vanilla auto-détectées), Jade.
- **Hard dep** : Architectury API ≥ 13.0.
- **Loaders** : Fabric 0.16+ et NeoForge 21.1+ pour Minecraft 1.21.1.

### Stack

- Minecraft 1.21.1, Java 21
- Architectury 13.0.8 + Architectury Loom 1.10.431
- Gradle 8.11.1 + Kotlin DSL
- DataComponents (1.20.5+) — pas de NBT legacy
- JUnit 5.10 + Mockito 5.11 + JaCoCo 0.8.12 (logique pure ≥ 96% coverage enforced)

## [Legacy] — 0.1.0-1.20.1-legacy

Snapshot Fabric+Forge 1.20.1 archivé sous tag `v0.1.0-1.20.1-legacy`. N'est plus maintenu.
