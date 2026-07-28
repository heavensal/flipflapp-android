# Material-Native Product Design

FlipFlapp should feel designed for Android, not like the Rails site translated pixel-for-pixel. Visual brand is shared: **olive pitch + gold**, dark only.

## Design principles

- **Hierarchy:** make the event, its time, location, availability, and primary action immediately clear.
- **Consistency:** use Material 3 navigation, controls, icons, typography, and FlipFlapp design-system components.
- **Focus:** each screen has one obvious primary purpose.
- **Context:** preserve scroll/navigation state and avoid disorienting full-screen replacements.
- **Accessibility:** build for diverse vision, mobility, cognition, and input needs from the first implementation.

References:

- [Material Design 3](https://m3.material.io/)
- [Android accessibility](https://developer.android.com/guide/topics/ui/accessibility)
- [Compose accessibility](https://developer.android.com/jetpack/compose/accessibility)

## Theme mode

Dark only. Do not add a light color scheme or follow system light/dark. Status and navigation bars use the olive background with light icons.

## Brand tokens

Aligned with Rails (`olive pitch + gold`):

| Token | Hex / role |
|-------|------------|
| Background / surface | `#2F4A0C` olive pitch |
| Elevated surface | `#3D5E12` / `#354F12` |
| Form / deep surface | `#24380A` |
| Primary (CTA) | `#E5B512` gold |
| On primary | `#24380A` |
| Title accent | `#F0D15A` |
| On-surface text | `#E8F0D2` |
| Muted text | pitch white @ ~48–72% |
| Error | `#C43C2C` |
| Success | `#6BBF3A` |
| Team A / B / Bench | `#1E3A8A` / `#7F1D1D` / `#4B5563` |
| Radius | 14dp brand (`medium`), 16dp cards |

Compose source of truth:

- `core/designsystem/theme/` — `Color`, `Type`, `Shape`, `Spacing`, `FlipflappTheme`
- Extra colors via `FlipflappThemeTokens.extras` (teams, muted, title, success)

## Component inventory

Use these before inventing one-off UI:

| Component | Role |
|-----------|------|
| `FfPrimaryButton` | Gold filled CTA |
| `FfSecondaryButton` | Outlined secondary action |
| `FfTextButton` | Low-emphasis / dialog actions |
| `FfDestructiveButton` | Delete / leave |
| `FfTextField` | Branded outlined field |
| `FfAddressField` | Google Places Autocomplete (`googleMaps.apiKey`); fills hidden lat/lng |
| `FfDateTimeField` | Material date + time pickers → ISO-8601 for the API |
| `FfMetaRow` | Icon + text metadata row (time, place, price, …) |
| `FfTopAppBar` | Title bar with optional back |
| `FfCard` / `FfSection` | Glass-like content surfaces |
| `FfStatusChip` | Privacy, fill, team, read state |
| `FfEventRow` | Events list row |
| `FfEmptyState` / `FfLoading` | Empty, error, loading |
| `FfBadgedIcon` | Unread badge on nav |
| `LoadStateView` | Maps `LoadState` to loading / empty / content |

Coordinates are transport data only. Event forms never expose latitude/longitude fields; users pick a formatted address via `FfAddressField`.


## Information architecture

Use a stable bottom navigation bar for top-level destinations, not actions. Keep tabs visible while navigating within a section and preserve each section's navigation state when practical. Use a top-bar action or FAB for event creation.

Suggested tabs: Events, Friends, Notifications, Profile. Use short localized labels and Material icons. Show an unread **badge** on Notifications when count > 0.

## Screen patterns

### Events list

- Top app bar and pull-to-refresh.
- Event rows (`FfEventRow`) prioritize title, localized date/time, location, availability, and privacy.
- A row navigates to details; creation is a separate FAB action.
- Empty state explains why and offers retry / creation as appropriate.

### Event details

- Clear title, organizer, time, location, price, privacy, and capacity inside `FfCard`.
- Teams are grouped by immutable slot with `FfStatusChip` tones; display current labels.
- Primary participation action reflects server state: join, switch, bench, or leave.
- Owner edit/delete controls are separated from participant actions.
- Destructive deletion uses a confirmation dialog with a specific consequence.

### Forms

- Use `FfTextField`, sections, switches, keyboard options, and inline validation.
- Preserve entered values after recoverable server failures.
- Disable duplicate submission while submitting, but keep cancellation/navigation behavior intentional.
- Place field errors close to fields and provide a concise summary when multiple errors exist.

### Friends

- Preserve the four server buckets: accepted, sent, received, declined.
- Received requests expose accept and decline with unambiguous labels.
- Search communicates that email is not searchable.
- Declined state respects the receiver-only domain rule.

### Notifications

- Use readable semantic rows and chips, not color alone, to distinguish unread state.
- Mark-as-read feedback is immediate but reconciled with server failure.
- Unknown payloads degrade gracefully.

## Visual system

- Use Material theme color roles (`primary`, `onSurface`, `surface`, `error`, …) plus `FlipflappThemeTokens.extras` for brand-only roles.
- Use scalable text styles from `FlipflappTypography`.
- Use Material Icons before custom icons.
- Use `FlipflappThemeTokens.spacing` instead of ad-hoc dp when possible.
- Brand color may be the app primary, but never the only carrier of meaning.

## Accessibility acceptance criteria

- Default interactive targets are at least 48×48 dp.
- Content descriptions communicate purpose, value, and state without duplicating visible prose.
- Focus order matches visual and task order.
- Large fonts / font scale work without clipping critical content.
- Color contrast remains sufficient and status is not encoded by color alone.
- Gestures have visible control alternatives.
- Progress and important state changes are announced when needed, without noisy repeated announcements.
- French localization expansion does not truncate controls or rely on English word length.

## Feedback

- Use inline progress for scoped actions and content-preserving refresh.
- Use dialogs for decisions or blocking failures, not routine success.
- Errors explain what happened and the next available action: retry, edit, sign in, or dismiss.

## Design review

Review representative screens in:

- phone and large-phone widths;
- portrait and landscape when applicable;
- default and large font scales;
- TalkBack;
- loading, empty, populated, long-content, offline, `401`, `403`, `404`, and `422` states.
