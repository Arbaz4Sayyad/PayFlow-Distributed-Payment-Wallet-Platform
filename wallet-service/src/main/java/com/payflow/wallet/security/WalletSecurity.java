package com.payflow.wallet.security;

import com.payflow.common.security.UserPrincipal;
import com.payflow.wallet.domain.entity.Wallet;
import com.payflow.wallet.domain.repository.WalletRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("walletSecurity")
public class WalletSecurity {

    private final WalletRepository walletRepository;

    public WalletSecurity(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public boolean isOwner(Authentication authentication, UUID walletId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return false;
        }

        // Allow ADMIN role bypass
        if (principal.roles() != null && principal.roles().contains("ROLE_ADMIN")) {
            return true;
        }

        return walletRepository.findById(walletId)
                .map(Wallet::getUserId)
                .map(userId -> userId.equals(principal.userId()))
                .orElse(false);
    }
}
