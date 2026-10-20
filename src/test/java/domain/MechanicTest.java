package domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Mechanic Entity Tests")
class MechanicTest {

    @Test
    @DisplayName("A newly created mechanic should be available by default (isAvailable = true)")
    void constructor_ValidData_MechanicIsAvailable() {
        Mechanic mechanic = new Mechanic("M-1", "Ivan Koval", MechanicSpecialization.GENERAL);

        assertEquals("M-1", mechanic.getId());
        assertEquals("Ivan Koval", mechanic.getName());
        assertEquals(MechanicSpecialization.GENERAL, mechanic.getSpecialization());
        assertTrue(mechanic.isAvailable());
    }

    @Test
    @DisplayName("Availability transitions: makeBusy sets available to false, makeFree sets available to true")
    void makeBusyAndMakeFree_StateTransitions_WorkCorrectly() {
        Mechanic mechanic = new Mechanic("M-1", "Ivan", MechanicSpecialization.GENERAL);

        mechanic.makeBusy();
        assertFalse(mechanic.isAvailable());

        mechanic.makeFree();
        assertTrue(mechanic.isAvailable());
    }

    @Test
    @DisplayName("Attempting to make an already busy mechanic busy should throw IllegalStateException")
    void makeBusy_AlreadyBusy_ThrowsIllegalStateException() {
        Mechanic mechanic = new Mechanic("M-1", "Ivan", MechanicSpecialization.GENERAL);
        mechanic.makeBusy();

        assertThrows(IllegalStateException.class, mechanic::makeBusy);
    }

    @Test
    @DisplayName("Attempting to free an already available mechanic should throw IllegalStateException")
    void makeFree_AlreadyFree_ThrowsIllegalStateException() {
        Mechanic mechanic = new Mechanic("M-1", "Ivan", MechanicSpecialization.GENERAL);

        assertThrows(IllegalStateException.class, mechanic::makeFree);
    }

    @Test
    @DisplayName("A GENERAL specialization mechanic can perform any type of work")
    void canPerform_GeneralMechanic_CanPerformAnySpecialization() {
        Mechanic general = new Mechanic("M-1", "Ivan", MechanicSpecialization.GENERAL);
        WorkItem electricalWork = new WorkItem("Electrical Diagnostics", new BigDecimal("500.00"), MechanicSpecialization.ELECTRICAL);

        assertTrue(general.canPerform(electricalWork));
    }

    @Test
    @DisplayName("A specialized mechanic cannot perform work outside their specialization")
    void canPerform_DifferentSpecialization_ReturnsFalse() {
        Mechanic electrician = new Mechanic("M-2", "Olena", MechanicSpecialization.ELECTRICAL);
        WorkItem engineWork = new WorkItem("Engine Overhaul", new BigDecimal("1000.00"), MechanicSpecialization.ENGINE);

        assertFalse(electrician.canPerform(engineWork));
    }
}