package org.yann.integerasiorderkuota.orderkuota.scheduling;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.yann.integerasiorderkuota.orderkuota.client.settlement.SettlementDTO;
import org.yann.integerasiorderkuota.orderkuota.client.settlement.SettlementService;
import org.yann.integerasiorderkuota.orderkuota.dto.CallbackDTO;
import org.yann.integerasiorderkuota.orderkuota.entity.*;
import org.yann.integerasiorderkuota.orderkuota.service.InvoiceService;
import org.yann.integerasiorderkuota.orderkuota.service.SendCallbackService;
import org.yann.integerasiorderkuota.orderkuota.service.StatementService;
import org.yann.integerasiorderkuota.orderkuota.service.UserService;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SaveStatementTransaction {


    private final UserService userService;
    private final SettlementService settlementService;
    private final StatementService statementService;
    private final InvoiceService invoiceService;
    private final RestTemplateBuilder restTemplateBuilder;
    private final SendCallbackService sendCallbackService;


    @Scheduled(fixedRate = 10_000)
    public void saveStatementTransaction() {
        List<User> allUser = userService.getAllUser();

        allUser.parallelStream().forEach(user -> {
            SettlementDTO dto = settlementService.getSettlementAllHistory(user.getId());

            List<SettlementDTO.QrisTransaction> results = dto.getQrisHistory().getResults();

            List<Long> ids = results.stream()
                    .map(SettlementDTO.QrisTransaction::getId)
                    .toList();
            Set<Long> existingStatementIds = statementService.getAllStatementByIds(Set.copyOf(ids));
            List<Statement> newStatement = results.stream()
                    .filter(result -> !existingStatementIds.contains(result.getId()))
                    .filter(result -> result.getStatus().equals("IN"))
                    .map(result -> new Statement(result, user.getUsername()))
                    .toList();
            statementService.saveAllStatements(Set.copyOf(newStatement));
        });
    }

    @Scheduled(fixedRate = 1_000)
    public void getPaidInvoice() {
        Map<Long, Invoice> invoiceByAmount = invoiceService.getPendingInvoice()
                .stream()
                .filter(invoice -> invoice.getExpiresAt() > System.currentTimeMillis())
                .collect(Collectors.toMap(Invoice::getAmount, Function.identity()));

        Map<String, String> invoiceMap = new HashMap<>();
        List<Long> statementList = new ArrayList<>();

        statementService.getPendingStatement()
                .stream()
                .filter(statement -> {
                    Invoice invoice = invoiceByAmount.get(statement.getKredit());
                    return invoice != null && statement.getTransferTime().isAfter(invoice.getCreatedAt());
                })
                .filter(statement -> invoiceByAmount.containsKey(statement.getKredit()))
                .forEach(statement -> {
                    Invoice invoice = invoiceByAmount.get(statement.getKredit());
                    invoiceMap.put(invoice.getUsername(), invoice.getId());
                    statementList.add(statement.getId());
                });
        statementService.updateBulkStatement(statementList);
        invoiceService.updateInvoicesToPaid(invoiceMap.values());

        sendCallbackUserInvoiceIsPaid(invoiceMap);
    }

    public void sendCallbackUserInvoiceIsPaid(Map<String, String> invoice) {
        invoice.forEach((username, invoice1) -> {
            User user = userService.getUserDetailsByUsername(username);
            if (user != null) {
                System.out.println("Invoice for user " + username + " is paid: " + invoice1);
                Invoice invoice2 = invoiceService.getById(invoice1).orElse(null);
                if (invoice2 == null) {
                    return;
                }
                CallbackDTO dto = new CallbackDTO(invoice2);
                if (user.getCallbackUrl() == null) {
                    System.out.println("Callback URL is null for user: " + username);
                    return;
                }
                invoice2.setPaidAt(System.currentTimeMillis());
                CompletableFuture.runAsync(() -> restTemplateBuilder.build().postForEntity(user.getCallbackUrl(), dto, String.class));
            } else {
                System.out.println("User not found for username: " + username);
            }
        });
    }

    @Scheduled(fixedRate = 1_000)
    @Async("taskScheduler")
    public CompletableFuture<Void> deleteExpiredInvoice() {
        CompletableFuture.runAsync(() -> {
            List<Invoice> pendingInvoice = invoiceService.getPendingInvoice().stream()
                    .filter(invoice -> invoice.getExpiresAt() < System.currentTimeMillis())
                    .toList();
            pendingInvoice.forEach(invoice -> {
                invoiceService.updateInvoiceStatus(invoice.getId(), InvoiceStatus.EXPIRED, false);
                User user = userService.getUserDetailsByUsername(invoice.getUsername());
                if (user != null && user.getCallbackUrl() != null) {
                    invoice.setStatus(InvoiceStatus.EXPIRED);
                    CallbackDTO dto = new CallbackDTO(invoice);
                    sendCallbackService.sendCallback(user.getCallbackUrl(), dto);
                } else {
                    System.out.println("User or callback URL not found for invoice: " + invoice.getId());
                }
            });
        });
        return CompletableFuture.completedFuture(null);
    }


}
