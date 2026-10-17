package ui;

import java.util.EnumSet;
import java.util.Set;

public final class Access {

    public enum Module {
        DASHBOARD, EMPLOYEES, ATTENDANCE, PAYROLL, PAYSLIP,
        SETUP,
        SETUP_ADMIN,
        PROFILE
    }

    private Access() {}

    public static boolean can(String role, Module m) {
        return allowed(role).contains(m);
    }

    private static Set<Module> allowed(String role) {
        if (role == null) return EnumSet.noneOf(Module.class);
        switch (role) {
            case "ADMIN":
                return EnumSet.allOf(Module.class);
            case "HR":
                return EnumSet.of(Module.DASHBOARD, Module.EMPLOYEES,
                        Module.ATTENDANCE, Module.SETUP, Module.PROFILE);
            case "PAYROLL":
                return EnumSet.of(Module.DASHBOARD, Module.PAYROLL,
                        Module.PAYSLIP, Module.PROFILE);
            case "EMPLOYEE":
                return EnumSet.of(Module.PAYSLIP, Module.PROFILE);
            default:
                return EnumSet.noneOf(Module.class);
        }
    }
}