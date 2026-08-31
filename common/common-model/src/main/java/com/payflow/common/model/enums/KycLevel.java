package com.payflow.common.model.enums;

public enum KycLevel {
    TIER_1, // Basic email/phone (Low daily transaction limit)
    TIER_2, // Government ID verified (Standard limit)
    TIER_3  // Full business/KYC verified (High/Enterprise limit)
}
