# ParkU — versión Kotlin

Traducción 1:1 del prototipo `Flutter-main/Flutter-main/parku` a Android nativo con
**Kotlin + Jetpack Compose**. Mismos textos, colores, tamaños, espaciados, fuente e
iconos. El proyecto Flutter original queda intacto.

## Equivalencias

| Flutter (Dart) | Kotlin (Compose) |
|---|---|
| `lib/main.dart` | `MainActivity.kt` |
| `lib/theme/app_theme.dart` | `ui/theme/AppTheme.kt` |
| `lib/widgets/design_icon.dart` | `ui/components/DesignIcon.kt` |
| `lib/widgets/navigation_bar.dart` | `ui/components/NavBar.kt` |
| `lib/widgets/parking_card.dart` | `ui/components/ParkingCard.kt` |
| `lib/widgets/pickup_widgets.dart` | `ui/components/PickupWidgets.kt` |
| `lib/screens/main_navigation.dart` | `ui/screens/MainNavigationScreen.kt` |
| `lib/screens/home.dart` | `ui/screens/HomeScreen.kt` |
| `lib/screens/parking_list.dart` | `ui/screens/ParkingListScreen.kt` |
| `lib/screens/my_parking.dart` | `ui/screens/MyParkingScreen.kt` |
| `lib/screens/no_active_parking.dart` | `ui/screens/NoActiveParkingScreen.kt` |
| `lib/screens/pickup_time.dart` | `ui/screens/PickupTimeScreen.kt` |
| `lib/screens/change_pickup_time.dart` | `ui/screens/ChangePickupTimeScreen.kt` |
| `lib/screens/favorites.dart` | `ui/screens/FavoritesScreen.kt` |
| `lib/screens/no_favorites.dart` | `ui/screens/NoFavoritesScreen.kt` |
| `lib/screens/search.dart` (vacío) | `ui/screens/SearchScreen.kt` (vacío) |
| `lib/screens/search_results.dart` (vacío) | `ui/screens/SearchResultsScreen.kt` (vacío) |

### Traducciones de concepto

| Flutter | Compose |
|---|---|
| `StatelessWidget` | función `@Composable` |
| `StatefulWidget` + `setState` | `remember { mutableStateOf(...) }` |
| `Navigator.push` / `pop` | estado `Overlay` en `MainNavigationScreen` + `BackHandler` |
| `Column` + `Expanded` | `Column` + `Modifier.weight(1f)` |
| `Stack` + `Positioned` | `Box` + `Modifier.align().offset()` |
| `SingleChildScrollView` | `Modifier.verticalScroll()` |
| `ListView.separated` | `LazyColumn` + `Arrangement.spacedBy()` |
| `InkWell` | `Modifier.clickable()` |
| `SafeArea` | `Modifier.safeDrawingPadding()` |
| `BoxDecoration(borderRadius:)` | `Modifier.clip(RoundedCornerShape())` |
| `height: 1.35` (multiplicador) | `lineHeight` calculado (`fontSize × 1.35`) |
| `Map<String, String>` | `data class ParkingLot` / `Favorite` |

### Recursos

| Flutter | Android |
|---|---|
| `assets/fonts/Inter.ttf` | `res/font/inter.ttf` |
| `assets/images/map.png` | `res/drawable-nodpi/map.png` |
| `assets/icons/*.svg` (flutter_svg) | `res/drawable/ic_*.xml` (vector drawables) |

Los SVG se convirtieron a vector drawables conservando `pathData`, grosor de
trazo y los extremos/uniones redondeados. El tintado que en Flutter hace
`ColorFilter.mode(color, BlendMode.srcIn)` aquí lo hace el parámetro `tint` de
`Icon`.

## Requisitos

- **JDK 17** (ya instalado en este equipo)
- **Android SDK** con API 35
- Gradle se descarga solo con el wrapper

## Compilar y ejecutar

```bash
./gradlew assembleDebug        # genera app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # instala en el teléfono conectado por USB
```

O abrir la carpeta en Android Studio y darle a Run.
