package app;

import domain.RepairOrder;
import domain.SparePart;
import domain.WorkItem;
import pricing.PriceBreakdown;
import service.OrderCheckoutResult;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class ReceiptPrinter {
    private static final String DOUBLE_LINE = "=".repeat(79);
    private static final String LINE = "-".repeat(79);

    private final PrintStream out;

    public ReceiptPrinter(PrintStream out) {
        this.out = Objects.requireNonNull(out, "Output stream cannot be null.");
    }

    public void print(OrderCheckoutResult result) {
        RepairOrder order = result.getOrder();
        PriceBreakdown price = result.getPrice();

        out.println(DOUBLE_LINE);
        out.println("                   AUTO SERVICE EXPRESS - RECEIPT");
        out.println(DOUBLE_LINE);
        out.printf("Order ID:         %s%n", order.getId());
        out.printf("Status:           %s%n", order.getStatus());
        out.printf("Client:           %s (phone: %s)%n", order.getClient().getName(), order.getClient().getPhone());
        out.printf("Client Discount:  %s%%%n", order.getClient().getDiscountPercentage());
        out.printf("Vehicle:          %s %s (%d), VIN: %s%n",
                order.getCar().getMake(), order.getCar().getModel(),
                order.getCar().getYear(), order.getCar().getVin());
        if (order.getMechanic() != null) {
            out.printf("Mechanic:         %s%n", order.getMechanic().getName());
        }
        out.println(LINE);

        out.println("SERVICES & PARTS:");
        List<WorkItem> works = order.getWorks();
        for (int i = 0; i < works.size(); i++) {
            WorkItem work = works.get(i);
            row((i + 1) + ". " + work.getDescription(), work.getLaborCost());
            for (SparePart part : work.getParts()) {
                row("     - " + part.getName() + " (" + part.getManufacturer() + ", " + part.getCode() + ")",
                        part.getPrice());
            }
        }
        out.println(LINE);

        row("Total for labor:", order.getLaborTotal());
        row("Total for parts:", order.getPartsTotal());
        row("Base order cost:", price.getBasePrice());
        if (price.getDiscountAmount().signum() > 0) {
            row("Personal discount:", "-" + price.getDiscountAmount());
        }
        if (price.isMinimumApplied()) {
            row("Minimum threshold adjustment:", price.getMinimumAdjustment());
        }
        out.println(LINE);
        row("TOTAL AMOUNT DUE:", price.getFinalPrice());
        out.println(DOUBLE_LINE);
        out.println();
    }

    private void row(String label, BigDecimal amount) {
        row(label, amount.toPlainString());
    }

    private void row(String label, String amount) {
        out.printf("%-62s %12s USD%n", label, amount);
    }
}