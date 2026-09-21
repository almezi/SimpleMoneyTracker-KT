# TESTABILITY.md

## Testing Strategy

### Unit Tests
- Location: `app/src/test/java/id/almezi/simplemoneytracker_kt/`
- Framework: JUnit 4
- Current coverage: Example test only (`ExampleUnitTest.kt`)

### Instrumented Tests
- Location: `app/src/androidTest/java/id/almezi/simplemoneytracker_kt/`
- Framework: AndroidJUnit4
- Current coverage: Example test only (`ExampleInstrumentedTest.kt`)

### Test Plan
- [ ] Add unit tests for business logic (transactions, calculations, etc.)
- [ ] Add instrumented tests for UI flows
- [ ] Add tests for data persistence layer
- [ ] Set up code coverage reporting

## Running Tests
- Unit tests: `./gradlew test`
- Instrumented tests: `./gradlew connectedAndroidTest`
