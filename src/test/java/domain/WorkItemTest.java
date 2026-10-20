package domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("WorkItem Domain Class Tests")
class WorkItemTest {

    @Test
    @DisplayName("Creating a work item without spare parts: PENDING status, parts cost defaults to 0.00")
    void constructor_WithoutParts_InitializesCorrectly() {
        WorkItem work = new WorkItem("Filter Replacement", new BigDecimal("250.00"));

        assertEquals("Filter Replacement", work.getDescription());
        assertEquals(new BigDecimal("250.00"), work.getLaborCost());
        assertEquals(new BigDecimal("0.00"), work.getPartsCost());
        assertEquals(new BigDecimal("250.00"), work.getTotalCost());
        assertEquals(WorkStatus.PENDING, work.getStatus());
        assertFalse(work.isCompleted());
    }

    @Test
    @DisplayName("Calculates total work cost including labor and assigned spare parts")
    void getTotalCost_WithParts_CalculatesSumOfLaborAndParts() {
        List<SparePart> parts = List.of(
                new SparePart("P-1", "Engine Oil", new BigDecimal("1200.00")),
                new SparePart("P-2", "Oil Filter", new BigDecimal("300.00"))
        );
        WorkItem work = new WorkItem("Scheduled Maintenance", new BigDecimal("500.00"), MechanicSpecialization.GENERAL, parts);

        assertEquals(new BigDecimal("1500.00"), work.getPartsCost());
        assertEquals(new BigDecimal("2000.00"), work.getTotalCost());
    }

    @Test
    @DisplayName("markCompleted transitions work item to COMPLETED status")
    void markCompleted_PendingWork_ChangesStatusToCompleted() {
        WorkItem work = new WorkItem("Diagnostics", new BigDecimal("300.00"));

        work.markCompleted();

        assertEquals(WorkStatus.COMPLETED, work.getStatus());
        assertTrue(work.isCompleted());
    }

    @Test
    @DisplayName("Calling markCompleted on an already completed work item throws IllegalStateException")
    void markCompleted_AlreadyCompleted_ThrowsIllegalStateException() {
        WorkItem work = new WorkItem("Diagnostics", new BigDecimal("300.00"));
        work.markCompleted();

        assertThrows(IllegalStateException.class, work::markCompleted);
    }
}