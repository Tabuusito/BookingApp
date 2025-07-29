package domain.model;

public enum Role {
    CLIENT("ROLE_CLIENT"),
    PROVIDER("ROLE_PROVIDER"),
    ADMIN("ROLE_ADMIN");

    private final String authority;

    Role(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }
}
