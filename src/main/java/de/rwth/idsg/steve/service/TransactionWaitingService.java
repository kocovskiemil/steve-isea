package de.rwth.idsg.steve.service;

import de.rwth.idsg.steve.ocpp.CommunicationTask;
import de.rwth.idsg.steve.repository.TaskStore;
import de.rwth.idsg.steve.service.notification.OcppTransactionStarted;
import de.rwth.idsg.steve.web.dto.ocpp.RemoteStartTransactionParams;
import de.rwth.idsg.steve.service.ChargePointService16_Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionWaitingService {

    private final TaskStore taskStore;
    private final ChargePointService16_Client chargePointService;
    
    private final ConcurrentHashMap<Integer, PendingTransaction> pendingTransactions = new ConcurrentHashMap<>();
    
    private static class PendingTransaction {
        final CompletableFuture<Integer> future;
        final String chargeBoxId;
        final String idTag;
        final Integer connectorId;
        
        PendingTransaction(CompletableFuture<Integer> future, String chargeBoxId, String idTag, Integer connectorId) {
            this.future = future;
            this.chargeBoxId = chargeBoxId;
            this.idTag = idTag;
            this.connectorId = connectorId;
        }
    }
    
    public Integer waitForTransactionId(int taskId, RemoteStartTransactionParams params, long timeoutSeconds) 
            throws TimeoutException, Exception {
        
        String chargeBoxId = params.getChargePointSelectList().get(0).getChargeBoxId();
        log.debug("Waiting for transaction ID for task {} on charge box {}", taskId, chargeBoxId);
        
        CompletableFuture<Integer> future = new CompletableFuture<>();
        PendingTransaction pendingTx = new PendingTransaction(
            future, 
            chargeBoxId, 
            params.getIdTag(), 
            params.getConnectorId()
        );
        
        log.debug("Storing pending transaction for task {} - ChargeBoxId: '{}', IdTag: '{}', ConnectorId: {}", 
                 taskId, chargeBoxId, params.getIdTag(), params.getConnectorId());
        
        pendingTransactions.put(taskId, pendingTx);
        
        // Check if the remote start was successful
        CommunicationTask<?, ?> task = taskStore.get(taskId);
        if (task == null) {
            pendingTransactions.remove(taskId);
            throw new IllegalArgumentException("Task not found: " + taskId);
        }
        
        int maxChecks = 30; // 3 seconds max
        int checkCount = 0;
        while (!task.isFinished() && checkCount < maxChecks) {
            try {
                Thread.sleep(100);
                checkCount++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                pendingTransactions.remove(taskId);
                throw new RuntimeException("Interrupted while waiting for task completion", e);
            }
        }
        
        if (!task.isFinished()) {
            pendingTransactions.remove(taskId);
            throw new TimeoutException("Remote start task did not complete within timeout");
        }
        
        var result = task.getResultMap().get(chargeBoxId);
        
        if (result == null) {
            pendingTransactions.remove(taskId);
            throw new Exception("No result found for charge box: " + chargeBoxId);
        }
        
        if (result.getErrorMessage() != null) {
            pendingTransactions.remove(taskId);
            throw new Exception("Remote start failed: " + result.getErrorMessage());
        }
        
        String response = result.getResponse();
        if (response == null || !response.toLowerCase().contains("accepted")) {
            pendingTransactions.remove(taskId);
            throw new Exception("Remote start was not accepted. Response: " + response);
        }
        
        log.debug("Remote start was accepted, waiting for actual transaction to start...");
        
        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("Timeout waiting for transaction to start for task {}", taskId);
            throw new TimeoutException("Transaction did not start within " + timeoutSeconds + " seconds");
        } finally {
            pendingTransactions.remove(taskId);
        }
    }
    
    /**
     * Starts a remote transaction and waits for the actual transaction ID.
     */
    public Integer startAndWaitForTransactionId(RemoteStartTransactionParams params, long timeoutSeconds) 
            throws TimeoutException, Exception {
        
        String chargeBoxId = params.getChargePointSelectList().get(0).getChargeBoxId();
        log.debug("Starting and waiting for transaction ID on charge box {} with timeout {}s", chargeBoxId, timeoutSeconds);
        
        CompletableFuture<Integer> future = new CompletableFuture<>();
        PendingTransaction pendingTx = new PendingTransaction(
            future, 
            chargeBoxId, 
            params.getIdTag(), 
            params.getConnectorId()
        );
        
        int tempTaskId = -1;
        log.debug("Pre-registering pending transaction with temp ID {} - ChargeBoxId: '{}', IdTag: '{}', ConnectorId: {}", 
                 tempTaskId, chargeBoxId, params.getIdTag(), params.getConnectorId());
        
        pendingTransactions.put(tempTaskId, pendingTx);
        
        try {
            int realTaskId = chargePointService.remoteStartTransaction(params);
            log.debug("Remote start call returned task ID {}, replacing temp ID {}", realTaskId, tempTaskId);
            
            pendingTransactions.remove(tempTaskId);
            pendingTransactions.put(realTaskId, pendingTx);

            CommunicationTask<?, ?> task = taskStore.get(realTaskId);
            if (task == null) {
                pendingTransactions.remove(realTaskId);
                throw new IllegalArgumentException("Task not found: " + realTaskId);
            }
            
            int maxChecks = 30; // 3 seconds max
            int checkCount = 0;
            while (!task.isFinished() && checkCount < maxChecks) {
                try {
                    Thread.sleep(100);
                    checkCount++;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    pendingTransactions.remove(realTaskId);
                    throw new RuntimeException("Interrupted while waiting for task completion", e);
                }
            }
            
            if (!task.isFinished()) {
                pendingTransactions.remove(realTaskId);
                throw new TimeoutException("Remote start task did not complete within timeout");
            }
            
            var result = task.getResultMap().get(chargeBoxId);
            
            if (result == null) {
                pendingTransactions.remove(realTaskId);
                throw new Exception("No result found for charge box: " + chargeBoxId);
            }
            
            if (result.getErrorMessage() != null) {
                pendingTransactions.remove(realTaskId);
                throw new Exception("Remote start failed: " + result.getErrorMessage());
            }
            
            String response = result.getResponse();
            if (response == null || !response.toLowerCase().contains("accepted")) {
                pendingTransactions.remove(realTaskId);
                throw new Exception("Remote start was not accepted. Response: " + response);
            }
            
            log.debug("Remote start was accepted, waiting for actual transaction to start...");
            
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
            
        } catch (TimeoutException e) {
            log.warn("Timeout waiting for transaction to start");
            throw new TimeoutException("Transaction did not start within " + timeoutSeconds + " seconds");
        } finally {
            pendingTransactions.entrySet().removeIf(entry -> entry.getValue() == pendingTx);
        }
    }
    
    @EventListener
    public void onTransactionStarted(OcppTransactionStarted event) {
        log.debug("Transaction started: {} on charge box {}", event.getTransactionId(), event.getParams().getChargeBoxId());
        log.debug("Event details - ChargeBoxId: '{}', IdTag: '{}', ConnectorId: {}", 
                 event.getParams().getChargeBoxId(), 
                 event.getParams().getIdTag(), 
                 event.getParams().getConnectorId());
        
        log.debug("Current pending transactions: {}", pendingTransactions.size());
        pendingTransactions.forEach((taskId, pending) -> {
            log.debug("Pending task {} - ChargeBoxId: '{}', IdTag: '{}', ConnectorId: {}", 
                     taskId, pending.chargeBoxId, pending.idTag, pending.connectorId);
        });
        
        pendingTransactions.entrySet().removeIf(entry -> {
            PendingTransaction pending = entry.getValue();
            
            boolean chargeBoxMatches = pending.chargeBoxId.equals(event.getParams().getChargeBoxId());
            boolean idTagMatches = pending.idTag.equals(event.getParams().getIdTag());
            boolean connectorMatches = (pending.connectorId == null || pending.connectorId.equals(event.getParams().getConnectorId()));
            
            log.debug("Matching task {} - ChargeBox: {} ({}), IdTag: {} ({}), Connector: {} ({})", 
                     entry.getKey(),
                     chargeBoxMatches, pending.chargeBoxId + " vs " + event.getParams().getChargeBoxId(),
                     idTagMatches, pending.idTag + " vs " + event.getParams().getIdTag(),
                     connectorMatches, pending.connectorId + " vs " + event.getParams().getConnectorId());
                        
            boolean matches = chargeBoxMatches && idTagMatches && connectorMatches;
            
            if (matches) {
                log.debug("Completing waiting future for task {} with transaction ID {}", entry.getKey(), event.getTransactionId());
                pending.future.complete(event.getTransactionId());
                return true;
            }
            
            return false;
        });
        
        log.debug("Remaining pending transactions after processing: {}", pendingTransactions.size());
    }
}
