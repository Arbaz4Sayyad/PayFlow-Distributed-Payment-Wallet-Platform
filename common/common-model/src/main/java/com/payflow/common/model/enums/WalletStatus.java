package com.payflow.common.model.enums;

public enum WalletStatus {
    ACTIVE,
    FROZEN,
    CLOSED;

    public boolean canTransact() {
        return this == ACTIVE;
    }
}
