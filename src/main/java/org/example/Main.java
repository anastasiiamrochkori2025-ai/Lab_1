package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 1. Pure Dependency Injection (ручне збирання залежностей без фреймворків)
        RepairOrderRepository repository = new InMemoryRepairOrderRepository();
        BigDecimal minimumTariff = new BigDecimal("300.00");
        PricingPolicy pricingPolicy = new StandardPricingPolicy(minimumTariff);
        RepairOrderService service = new RepairOrderService(repository, pricingPolicy);

        // 2. Створення доменних сутностей: Клієнт (зі знижкою 10%) та Автомобіль
        Client client = new Client(
                "C-101",
                "Олександр Мельник",
                "+380501234567",
                new BigDecimal("10.00")
        );
        Car car = new Car(
                "1HGCR2F83HA123456",
                "Honda",
                "Accord",
                2020,
                client
        );

        // 3. Створення замовлення та додавання позицій робіт і деталей
        RepairOrder order = new RepairOrder("RO-2026-001", car, client);

        order.addWork(new WorkItem("Комп'ютерна діагностика", new BigDecimal("0.00"), new BigDecimal("500.00")));
        order.addWork(new WorkItem("Заміна передніх гальмівних колодок", new BigDecimal("0.00"), new BigDecimal("600.00")));
        order.addWork(new WorkItem("Заміна моторного масла та фільтра", new BigDecimal("0.00"), new BigDecimal("400.00")));

        order.addSparePart(new SparePart("BP-204", "Гальмівні колодки передні", "Brembo", new BigDecimal("1850.00")));
        order.addSparePart(new SparePart("OIL-5W30", "Моторне масло 5W-30 (4л)", "Motul", new BigDecimal("1450.00")));
        order.addSparePart(new SparePart("OF-112", "Масляний фільтр", "Mann", new BigDecimal("350.00")));

        // Реєстрація нового замовлення в сервісі
        service.saveOrder(order);

        // 4. Виклик сервісу для повного розрахунку та закриття замовлення
        OrderCheckoutResult checkoutResult = service.checkoutOrder("RO-2026-001");
        RepairOrder completedOrder = checkoutResult.getOrder();
        BigDecimal finalPrice = checkoutResult.getFinalPrice();

        // 5. Розрахунок аналітики для структурованого чека
        BigDecimal totalWorksCost = BigDecimal.ZERO;
        for (WorkItem work : completedOrder.getWorks()) {
            totalWorksCost = totalWorksCost.add(work.getPartsCost()).add(work.getLaborCost());
        }
        totalWorksCost = totalWorksCost.setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalPartsCost = BigDecimal.ZERO;
        for (SparePart part : completedOrder.getSpareParts()) {
            totalPartsCost = totalPartsCost.add(part.getPrice());
        }
        totalPartsCost = totalPartsCost.setScale(2, RoundingMode.HALF_UP);

        BigDecimal basePrice = completedOrder.calculateBasePrice();
        BigDecimal discountAmount = basePrice.subtract(finalPrice).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        // 6. Виведення структурованого чека в консоль
        printReceipt(completedOrder, totalWorksCost, totalPartsCost, basePrice, discountAmount, finalPrice, minimumTariff);
    }

    private static void printReceipt(
            RepairOrder order,
            BigDecimal totalWorksCost,
            BigDecimal totalPartsCost,
            BigDecimal basePrice,
            BigDecimal discountAmount,
            BigDecimal finalPrice,
            BigDecimal minimumTariff
    ) {
        System.out.println("================================================================================");
        System.out.println("                            СТО \"АВТОСЕРВІС ЕКСПРЕС\"                            ");
        System.out.println("                                 ФІСКАЛЬНИЙ ЧЕК                                 ");
        System.out.println("================================================================================");
        System.out.printf("Замовлення №:   %s%n", order.getId());
        System.out.printf("Статус:         %s%n", order.getStatus());
        System.out.printf("Клієнт:         %s (тел. %s)%n", order.getClient().getName(), order.getClient().getPhone());
        System.out.printf("Знижка клієнта: %s%%%n", order.getClient().getDiscountPercentage());
        System.out.printf("Автомобіль:     %s %s (%d р.), VIN: %s%n",
                order.getCar().getMake(),
                order.getCar().getModel(),
                order.getCar().getYear(),
                order.getCar().getVin()
        );
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("ВИКОНАНІ РОБОТИ:");
        List<WorkItem> works = order.getWorks();
        for (int i = 0; i < works.size(); i++) {
            WorkItem work = works.get(i);
            BigDecimal cost = work.getPartsCost().add(work.getLaborCost()).setScale(2, RoundingMode.HALF_UP);
            System.out.printf("  %2d. %-58s %12s грн%n", (i + 1), work.getDescription(), cost);
        }
        System.out.printf("  %-62s %12s грн%n", "РАЗОМ ЗА РОБОТИ:", totalWorksCost);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("ВИКОРИСТАНІ ДЕТАЛІ ТА МАТЕРІАЛИ:");
        List<SparePart> parts = order.getSpareParts();
        for (int i = 0; i < parts.size(); i++) {
            SparePart part = parts.get(i);
            String partInfo = part.getName() + " (" + part.getManufacturer() + ", " + part.getCode() + ")";
            System.out.printf("  %2d. %-58s %12s грн%n", (i + 1), partInfo, part.getPrice());
        }
        System.out.printf("  %-62s %12s грн%n", "РАЗОМ ЗА ДЕТАЛІ:", totalPartsCost);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("Базова вартість робіт:                                        %12s грн%n", totalWorksCost);
        System.out.printf("Базова вартість деталей:                                      %12s грн%n", totalPartsCost);
        System.out.printf("Загальна базова вартість замовлення:                          %12s грн%n", basePrice);
        System.out.printf("Застосована персональна знижка:                              -%12s грн%n", discountAmount);
        System.out.printf("Поріг мінімальної вартості замовлення (тариф):                %12s грн%n", minimumTariff);
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("ПІДСУМКОВА СУМА ДО ОПЛАТИ:                                    %12s грн%n", finalPrice);
        System.out.println("================================================================================");
    }
}
