# Numismatic Reimagined — CLAUDE.md

> Conventions projet applicables à Claude Code et à tout contributeur.

## Identité du projet

- **Nom** : Numismatic Reimagined
- **Mod ID** : `numismatic_reimagined`
- **Package** : `dev.gaspard4i.numismatic`
- **Licence** : MIT (dérive de wisp-forest/numismatic-overhaul MIT)
- **Version cible** : Minecraft 1.21.1
- **Loaders** : Fabric + NeoForge (via Architectury API 13.x)
- **Java** : 21 (imposé par MC 1.21.1 / NeoForge). **Kotlin interdit**.
- **UI lib** : owo-lib (Fabric + NeoForge ≥0.12.20)

## Principe directeur

**Le mod est une copie améliorée de `wisp-forest/numismatic-overhaul`**. Avant d'implémenter une feature, check si le mod upstream l'a déjà. Si oui : port fidèle (adapter APIs 1.20→1.21). Si non : nouvelle impl bien documentée.

Toute feature qui diverge de l'upstream doit être clairement marquée dans le commit message comme "reimagined-specific".

---

## Règles de commit (NON-NÉGOCIABLES)

### Auteur

- **JAMAIS** de mention `Co-Authored-By: Claude` ou équivalent dans les commits
- **JAMAIS** de mention d'IA, d'assistant, de Claude dans les messages
- Auteur Git = utilisateur, point.

### Format

```
<type>(<scope>): <description courte> [NR-XXX]

<corps optionnel>

<footer optionnel>
```

### Types

| Type | Usage |
|---|---|
| `feat` | Nouvelle feature |
| `fix` | Correction de bug |
| `refactor` | Refactor sans changement de comportement |
| `perf` | Optim perf |
| `asset` | Assets (textures, modèles, sons) |
| `lang` | Traductions |
| `config` | Config du mod |
| `compat` | Compat avec un autre mod |
| `docs` | Docs |
| `build` | Gradle, CI, dépendances |
| `test` | Tests |
| `chore` | Maintenance |

### Scopes

| Scope | Zone |
|---|---|
| `currency` | Système de monnaie |
| `shop` | Shop blocks + UI |
| `purse` | Purse system + overlay |
| `piggy` | Piggy bank |
| `request` | Reverse shop / bounty board |
| `villager` | Trades villageois |
| `loot` | Loot tables + mob drops |
| `advancement` | Advancements |
| `item` | Items génériques |
| `network` | Packets |
| `gui` | UI générique |
| `component` | Data components |
| `config` | Config |
| `fabric` | Fabric-spécifique |
| `neoforge` | NeoForge-spécifique |
| `common` | Code commun |
| `ci` | GitHub Actions |
| `bootstrap` | Structure projet initiale |

### Exemples

```
feat(shop): hopper input toggle with side container [NR-123]

ShopBlockEntity implements WorldlyContainer now. Transfer flag
persisted in NBT, toggle packet from owner UI.

Tests: ShopTransferLogicTest (8 cases)
```

```
fix(purse): popup position relative to inventory button [NR-137]
```

---

## Branches

| Branche | Usage |
|---|---|
| `main` | Releases stables seulement (tags v*) |
| `develop` | Développement principal |
| `feature/NR-XXX-description` | Nouvelles features |
| `fix/NR-XXX-description` | Bugs |
| `release/vX.Y.Z` | Préparation release |

- **Squash merge** vers `develop`.
- **Merge commit** vers `main` lors d'une release.
- Pas de force-push sauf sur branche perso en cours.

---

## Tests (OBLIGATOIRES)

### Framework

- **JUnit 5.10**
- **Mockito 5.11** pour les mocks
- **JaCoCo 0.8.12** pour la couverture

### Seuils

- **Code testable** (logique pure, pas de dépendance MC runtime) : **≥96%** line coverage (enforced via `jacocoTestCoverageVerification`)
- **Logique métier critique** (calculs de prix, conversion, validation) : **100%**

### Pattern refactor obligatoire

Si une classe touche MC mais contient de la logique métier, **extraire la logique pure** dans une classe séparée (`*Logic`, `*Ops`, `*Account`, `*Helper`) qui peut être testée sans MC runtime.

Exemples :
- `ShopTransferLogic` extrait de `ShopBlockEntity`
- `MoneyBagClickLogic` extrait de `MoneyBagItem`
- `PurseExtractLogic` extrait de `PurseScreen`
- `PiggyBankAccount` extrait de `PiggyBankBlockEntity`
- `RequestFulfillLogic` extrait de `RequestBoardBlockEntity`

### Classes exclues de JaCoCo (validées in-game via STATUS.md)

- `*Block.java`, `*BlockEntity.java`, `*Item.java` (touchent Level/Player/ItemStack runtime)
- `*Screen.java`, `*Menu.java` (UI rendering)
- `client/**` (Minecraft client runtime)
- `network/**` (ServerPlayer / NetworkManager)
- `command/**` (Brigadier + CommandSource)
- `mixin/**`
- `*Numismatic*.java` (registration glue)
- Fichiers listés dans `build.gradle.kts` section `jacocoExclusions`

### Workflow avant commit

1. Écrire les tests **avant ou en même temps** que le code
2. `./gradlew :common:test :common:jacocoTestReport`
3. Vérifier `common/build/reports/jacoco/test/html/index.html`
4. Si une classe testable est <100% : ajouter les tests manquants
5. Si le seuil global est ≥96% : commit autorisé
6. **Si tests cassent** : INTERDIT de commit, on corrige
7. **Si JaCoCo fail** : INTERDIT de commit, on ajoute des tests ou on exclut proprement avec justification

---

## Style de code

### Language

- **Code** : anglais (identifiers, noms de classes, variables, méthodes, JavaDoc technique)
- **Documentation utilisateur** : français (README, CHANGELOG descriptions)
- **Commits** : description française, type/scope anglais
- **Issues/PRs** : français

### Orthographe

Toujours écrire avec accents et caractères français corrects dans les textes français (é è ê ç à ô...). Jamais de ASCII fallback "e" pour "é".

### Commentaires

Par défaut **pas de commentaires**. Seulement quand le WHY est non-évident :
- Contrainte cachée
- Invariant subtil
- Contournement d'un bug spécifique
- Comportement qui surprendrait un lecteur

Ne pas commenter le WHAT (le code bien-nommé le dit déjà). Ne pas référencer la tâche ou l'IA.

### Emojis

**Jamais** dans le code, les fichiers, les commits, les docs, la lang JSON. Sauf si l'utilisateur demande explicitement.

### Logging

- SLF4J standard : `private static final Logger LOGGER = LoggerFactory.getLogger(Numismatic.MOD_ID);`
- `LOGGER.info` lifecycle events only (mod init, registry registration count)
- `LOGGER.warn` user-facing problems recoverable
- `LOGGER.error` bugs + stacktrace
- **Rate-limiter** tout log dans une boucle de tick (utiliser un compteur ou Guava RateLimiter)

### Data storage

- **DataComponents** (1.20.5+) pour toute data sur ItemStack. **Pas de NBT legacy** (`stack.getTag()` / `stack.getOrCreateTag()`).
- **SavedData** pour data globale server-side.
- **Architectury AttachmentType API** pour data attachée à Entity/Player (au lieu de Cardinal Components Fabric-only).

### Registries

- **DeferredRegister** via Architectury, jamais direct.
- Init dans un `register()` static appelé depuis `Numismatic.init()`.

### Events

- **Architectury Events** par défaut (80% des cas).
- **Platform events** uniquement dans fabric/ ou neoforge/ si pas de pont Architectury (rare).

### Mixins

- **Uniquement dans fabric/ ou neoforge/**, jamais dans common/.
- **Dernier recours** : préférer Access Wideners (Fabric) / Access Transformers (NeoForge) quand possible.
- Un mixin = un commentaire JavaDoc expliquant pourquoi il est nécessaire et quand il pourra être retiré.

### Networking

- **Architectury NetworkManager** pour les payloads simples.
- **owo-lib OwoNetChannel** quand on a besoin de sérialisation Endec complexe (ShopOffer list).

---

## Workflow par feature (le standard)

Toute feature suit ces 7 étapes :

1. **Check upstream** (`wisp-forest/numismatic-overhaul`) : existe ? copier/porter. Sinon : from scratch.
2. **Extraire la logique pure** dans `common/` sous une classe sans dépendance MC.
3. **Écrire les tests JUnit** en parallèle (100% coverage sur la logique pure).
4. **Implémenter la glue MC** (Block/Item/Menu/Screen) qui utilise la logique pure.
5. **Intégrer l'UI** via owo-lib (XML template) ou vanilla Screen simple.
6. **Ajouter datapack + assets** (recipes, tags, lang en/fr, textures, models).
7. **Build + tests + commit + push** avec `[NR-XXX]`.

## Anti-patterns (ne PAS faire)

- Ajouter du code dans un Screen 800+ lignes. Split si >300.
- Patch UV successifs dans un fichier. Redessiner la texture ou laisser owo-lib gérer.
- Cacher des champs "Y=17 vs Y=18" sans commentaire expliquant pourquoi.
- Rarity tier-based sur money bag (rester COMMON).
- Dupliquer Fabric→NeoForge à la main quand Architectury fait le pont.
- Utiliser NBT legacy (`stack.getTag()`) — DataComponents uniquement.
- Mixer logique métier + code MC dans la même classe → casse la testabilité.

## Conventions fichiers spécifiques

### lang/*.json

- Keys en kebab-case avec namespace : `item.numismatic_reimagined.foo_bar`
- **Pas de clés mortes** (nettoyer à la suppression d'une feature)
- Toutes les clés doivent exister en en_us ET fr_fr

### textures

- 16×16 pour items, 256×256 pour GUI owo-ui (standard upstream)
- PNG RGBA optimisés (pngcrush si gros)
- Nommer avec préfixe bloc/item : `block/shop_top.png`, `item/bronze_coin.png`

### assets/owo_ui/*.xml

- Fichier par écran (purse.xml, shop.xml, piggy_bank.xml, request_board.xml)
- Nommer les components : `id="gold-count"`, `id="extract-button"` etc.
- Pas de positions hardcodées si owo-ui peut les dériver (flow-layout, margins)

---

## Distribution

### Versioning

**SemVer strict** : MAJOR.MINOR.PATCH
- MAJOR : breaking change config/API
- MINOR : feature
- PATCH : fix

### Changelog

`CHANGELOG.md` format **Keep a Changelog** : Added / Changed / Deprecated / Removed / Fixed / Security.

### Publishing

- **ModPublisher (firstdarkdev)** : une tâche → Modrinth + CurseForge
- Trigger : tag `v*` pushé → workflow `release.yml` auto
- Metadata dans `build.gradle.kts` : dependencies (Architectury hard, owo-lib hard, JEI/REI/EMI/Curios/Trinkets/Create soft)

### Compatibilité

- **Out-of-the-box** : JEI/REI/EMI (auto-détection recipes vanilla)
- **Soft dep** : Curios / Trinkets (slot purse accessoire)
- **Soft dep** : Create (items passent via belts)
- **Soft dep** : CraftTweaker (register @ZenRegister si mod présent)
- **Hard dep** : Architectury API, owo-lib

Toute nouvelle compat doit être testée sur un modpack minimal reproductible.

---

## Statut

- Voir `STATUS.md` à la racine pour l'état d'avancement des phases du plan en cours (`plans/structured-weaving-token.md`).
- Le legacy 1.20.1 est taggé `v0.1.0-1.20.1-legacy` et n'est plus maintenu.
