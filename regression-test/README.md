# Regression Test
### Setup
1) Open the project in Intellij and go to "Edit Configurations"

2) Add an environment variable for each database's ip, name, username, and password in the format of `V[version_number]_ip`:
    ```
    V3_IP
    V3_NAME
    V3_USER
    V3_PASSWORD
    
    V4_IP
    V4_NAME
    ...
    ```
    *NB: Failure to provide these values will default to localhost, aionv(#), admin1, and password for each field respectively*
    
3) Add an environment variable for the minimum range called `RANGE_MIN` and the maximum range called `RANGE_MAX`. 

*NB: Failure to provide these values will default them to 1,384,500 and 1,385,000 respectively.*

4) (Optional) You can go to the `FromV4toV5` file located in `src > main > java > com > aion > dashboard > etl > tests`
and within its main method, select specficially which tests you wish to run.

5) Run the project with the environment configurations set.

### NOTICE
TO DO: Tests involving the v3 database against the v4 and v5 databases.