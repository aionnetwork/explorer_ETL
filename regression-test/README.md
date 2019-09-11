# Regression Test
### Setup
1) build the project using `./gradlew clean build`
2) execute `cd build/distributions` and extract the tarball
3) open the output folder and set the database server information and any tables that should be exempted from the test. 
4) specify the databases to be tested
5) the output will be found in the test_output folder 

### Interpreting Results
The test results are stored as a value along with the primary key of the table and the table name (database1-value, database1-pk, database2-value, database2-pk).
This allows a user to easily find the missing result by querying the table on the primary key.