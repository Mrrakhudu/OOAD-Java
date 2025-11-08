package bank.finalsys;

public class BankTeller extends User {
    private String employeeId;
    private String fullName;

    public BankTeller(String username, String password, String employeeId, String fullName) {
        super(username, password, UserRole.TELLER);
        this.employeeId = employeeId;
        this.fullName = fullName;
    }

    public BankTeller(String username, String password, String employeeId, String fullName, UserRole role) {
        super(username, password, role);
        this.employeeId = employeeId;
        this.fullName = fullName;
    }

    public String getEmployeeId() { return employeeId; }
    public String getFullName() { return fullName; }

    @Override
    public String getDisplayName() {
        return fullName + " (Teller)";
    }

    @Override
    public String toFileString() {
        return getUsername() + "|" + getPassword() + "|" + getRole().toString() + "|" + employeeId + "|" + fullName;
    }

    public static BankTeller fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 5) {
            return new BankTeller(parts[0], parts[1], parts[3], parts[4], UserRole.valueOf(parts[2]));
        }
        return null;
    }
}