package aion.dashboard.integritychecks;

import aion.dashboard.blockchain.interfaces.APIService;
import aion.dashboard.domainobject.Account;
import aion.dashboard.service.AccountService;
import aion.dashboard.util.Tuple2;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class AccountIntegrityCheck extends IntegrityCheck<Account, Tuple2<String, String>> {

    private final AccountService service;
    private final APIService apiService;

    AccountIntegrityCheck(AccountService service, APIService apiService) {
        super("acc-integrity-check", "Accounts");
        this.service = service;
        this.apiService = apiService;
    }

    @Override
    protected List<Tuple2<String, String>> integrityCheck(List<Account> candidates) throws Exception {
        INTEGRITY_LOGGER.info("Starting integrity check for: {}", candidates.size());
        ArrayList<Tuple2<String, String>> res = new ArrayList<>();
        List<Account> accountsToUpdate=new ArrayList<>();
        for (var acc : candidates){
            boolean runUpdate = false;
            var nonce = apiService.getNonce(acc.getAddress());
            var balance = apiService.getBalance(acc.getAddress());
            if (nonce.compareTo(new BigInteger(acc.getNonce(),16))!=0){
                res.add(new Tuple2<>(acc.toString(), "Difference found in account nonce. Expected: "+ nonce.toString(16)+", found: "+acc.getNonce() ));
                runUpdate = true;
            }

            //skip the check if the account is found to be inconsistent
            if (!runUpdate && balance.compareTo(acc.getBalance().toBigIntegerExact())!=0){
                res.add(new Tuple2<>(acc.toString(), "Difference found in account balance. Expected: "+ balance.toString()+", found: "+acc.getBalance().toPlainString() ));
                runUpdate = true;
            }

            if (runUpdate){
                acc.setBalance(new BigDecimal(balance));
                acc.setNonce(nonce.toString());
                accountsToUpdate.add(acc);

            }


        }

        if (!accountsToUpdate.isEmpty()) {
            if (service.save(accountsToUpdate)) {
                INTEGRITY_LOGGER.info("Updated accounts.");
                accountsToUpdate.forEach(account -> INTEGRITY_LOGGER.trace("Update: {}", account.getAddress()));
            }
            else {
                INTEGRITY_LOGGER.warn("Failed to update accounts.");
            }

        }
        return res;
    }

    /**
     * @return the random list of values to use for the integrity check
     */
    @Override
    protected List<Account> findCandidates() throws Exception {
        return service.getRandomAccounts(ThreadLocalRandom.current().nextInt(50,100)).orElseThrow(()->new NoSuchElementException("Could not find any accounts"));
    }

    @Override
    protected void printFailure(List<Tuple2<String, String>> failedCandidates) {
        if (INTEGRITY_LOGGER.isWarnEnabled()){
            for (var tuple2: failedCandidates){
                INTEGRITY_LOGGER.warn("Check for account failed. \n{}", tuple2);
            }
        }
    }

    @Override
    protected void printSuccess(List<Account> succeededCandidates) {
        if (INTEGRITY_LOGGER.isTraceEnabled()){
            for (var candidate: succeededCandidates){
                INTEGRITY_LOGGER.trace("Account correct: {}", candidate);
            }
        }
    }
}
