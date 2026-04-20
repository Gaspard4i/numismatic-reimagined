# Numismatic Reimagined

Mod économique Minecraft 1.21.1 (Fabric + NeoForge), réimagination du mod de wisp-forest/numismatic-overhaul.

## Features

| Feature | Description |
|---|---|
| **Monnaie 4 dénoms** | Bronze (1), Silver (100), Gold (10 000), Netherite (1 000 000) |
| **Bourse** | Overlay popup sur l'inventaire avec extract granulaire par dénomination |
| **Tirelires** | 3 tiers (base / dorée / netherite) — déposez vos pièces, récoltez à la casse |
| **Boutiques** | 5 tiers de blocs vendeurs (bronze/silver/gold/netherite/admin) |
| **Panneau de requêtes** | Reverse shop : payez les autres joueurs pour qu'ils vous livrent des items |
| **Star Coin** | Trophée légendaire à débloquer |
| **Mob drops** | Tous les mobs droppent des coins (30% chance, scale sur leur max health) |
| **Trades villageois** | Bibliothécaire échange émeraudes contre silver/gold coins |
| **Commands** | `/numismatic balance`, `/numismatic give`, `/numismatic set` |

## Installation

1. Installez Fabric Loader 0.16+ ou NeoForge 21.1.95+ pour Minecraft 1.21.1.
2. Installez **Architectury API ≥ 13.0** (hard dependency).
3. Sur Fabric : installez aussi **Fabric API**.
4. Téléchargez le JAR correspondant à votre loader depuis [Releases](https://github.com/Gaspard4i/numismatic-reimagined/releases) et placez-le dans `mods/`.

## Compatibilité

| Mod | Niveau | Notes |
|---|---|---|
| **Architectury API** | Hard dep | ≥ 13.0 |
| **Fabric API** | Hard dep (Fabric only) | |
| **JEI / REI / EMI** | Out-of-the-box | Recipes vanilla auto-détectées |
| **Jade / The One Probe** | Out-of-the-box | Affiche le tier des piggy banks et shops |

## Build local

Requiert JDK 21 dans `JAVA_HOME`.

```bash
./gradlew build
```

JARs générés dans `fabric/build/libs/` et `neoforge/build/libs/`.

Pour lancer en dev :

```bash
./gradlew :fabric:runClient
./gradlew :neoforge:runClient
```

JEI et Jade sont inclus en `modRuntimeOnly` pour faciliter les tests.

## Tests

```bash
./gradlew :common:test :common:jacocoTestReport
```

Coverage minimum 96% sur la logique pure (enforcé via `:common:check`).

## License

MIT — voir [LICENSE](LICENSE). Inspiré de [wisp-forest/numismatic-overhaul](https://github.com/wisp-forest/numismatic-overhaul) (MIT).
