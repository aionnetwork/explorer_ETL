package aion.dashboard.integrityChecks;

import aion.dashboard.blockchain.APIService;
import aion.dashboard.domainobject.Account;
import aion.dashboard.service.AccountService;
import aion.dashboard.util.Tuple2;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
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
        for (var acc : candidates){
            var nonce = apiService.getNonce(acc.getAddress());
            var balance = apiService.getBalance(acc.getAddress());
            if (nonce.compareTo(new BigInteger(acc.getNonce(),16))!=0){
                res.add(new Tuple2<>(acc.toString(), "Difference found in account nonce. Expected: "+ nonce.toString(16)+", found: "+acc.getNonce() ));
            }

            if (balance.compareTo(acc.getBalance().toBigIntegerExact())!=0){
                res.add(new Tuple2<>(acc.toString(), "Difference found in account balance. Expected: "+ balance.toString()+", found: "+acc.getBalance().toPlainString() ));

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
