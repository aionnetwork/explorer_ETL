package aion.dashboard.exception;

/**
 * Thrown in the case of an exception in the DBService class.
 * May result from exceptions when connecting or accessing the Database.
 */
public class DbServiceException extends Exception {

    public DbServiceException(){
        super();
    }

    public DbServiceException(String message){
        super(message);
    }


}
