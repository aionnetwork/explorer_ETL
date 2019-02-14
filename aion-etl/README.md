## Building:
##### To build, use the following command:
```
./gradlew build -x test
```
Build should be available at `./build/distributions`

##### To build and run the all tests, use the following command:
```
./gradlew build
```

## Testing:
##### Configuration:
1) Open *config.json* 
2) Ensure **aionPath** is correctly pointing to your kernel's directory
3) Ensure **scriptsPath** is correctly pointing to your transaction-generator scripts directory
4) Set **largeLimit** to your ideal size for the ReorgTest for "Deep Reorgs"
5) Set **smallLimit** to your ideal size for the ReorgTest for "Shallow Reorgs"

##### To run all tests, use the following command:
```
./gradlew clean test
```

##### To run specific a specific test, use the following command:

```
./gradlew test --tests "aion.dashboard.blockchain.TestClass.testToRun"

```
To execute a specific test. Example: 

```
./gradlew test --tests "aion.dashboard.blockchain.AionServiceTest.testBadByteArrays"
```

