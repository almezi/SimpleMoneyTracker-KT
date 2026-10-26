# Changelog

All notable changes to SimpleMoneyTracker-KT are documented here.

## [Unreleased] — PR #13: Fix undetected button in automation

### Added
- `Modifier.automationTestTags()` expect/actual extension that re-arms `testTagsAsResourceId` on Android for compositions rooted outside `MainActivity` (bottom sheets, dialogs); no-op on iOS.
- `release/SimpleMoneyTrackerKT-v2.2.apk` release artifact.

### Fixed
- Test tags were missing from the accessibility hierarchy in separate window compositions, so automation could not resolve buttons in `SakuBottomSheet`, `SakuDatePicker`, and the category rename `AlertDialog`. Re-armed the flag at each root.
- Category rename dialog error text now renders under the input field.
- Refined the rename dialog field with explicit text/cursor styling and spacing for error messages.

### Changed
- Simplified `Saku.xcscheme` by removing unused test, profile, analyze, and archive actions; kept build and debug launch settings. Added scheme management entries in `xcschememanagement.plist`.
