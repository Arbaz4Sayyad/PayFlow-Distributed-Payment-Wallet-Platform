# =========================================================================
# PayFlow Recruiter Demo Environment Reset & Seed Script
# Deterministically configures real PostgreSQL databases, Redis, and microservices
# =========================================================================

$ErrorActionPreference = "Stop"

Write-Host "Resetting PayFlow Demo Environment..." -ForegroundColor Cyan

# 1. Reset user_db
$userSql = @'
DELETE FROM refresh_tokens WHERE user_id IN (
    '11111111-1111-1111-1111-111111111111',
    '22222222-2222-2222-2222-222222222222',
    '33333333-3333-3333-3333-333333333333',
    '44444444-4444-4444-4444-444444444444'
) OR user_id IN (SELECT id FROM users WHERE email IN ('demo@payflow.demo', 'sarah@payflow.demo', 'rahul@payflow.demo', 'alex@payflow.demo'));

DELETE FROM users WHERE email IN ('demo@payflow.demo', 'sarah@payflow.demo', 'rahul@payflow.demo', 'alex@payflow.demo')
   OR id IN (
    '11111111-1111-1111-1111-111111111111',
    '22222222-2222-2222-2222-222222222222',
    '33333333-3333-3333-3333-333333333333',
    '44444444-4444-4444-4444-444444444444'
   );

-- System User
INSERT INTO users (id, email, phone, password_hash, status, role, kyc_level, created_at, updated_at)
VALUES 
    ('00000000-0000-0000-0000-000000000001', 'system.clearing@payflow.demo', '+15550000000', '$2a$12$hlmYcwqrBlQIQwR0X8rb8unnqwuJQ4ZJmxktBhga8TrYifrGL1ID6', 'ACTIVE', 'ROLE_USER', 'TIER_3', NOW() - INTERVAL '30 days', NOW())
ON CONFLICT (id) DO NOTHING;

-- 4 Fictional Demo Accounts
INSERT INTO users (id, email, phone, password_hash, status, role, kyc_level, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'demo@payflow.demo', '+15551234567', '$2a$12$hlmYcwqrBlQIQwR0X8rb8unnqwuJQ4ZJmxktBhga8TrYifrGL1ID6', 'ACTIVE', 'ROLE_USER', 'TIER_3', NOW() - INTERVAL '14 days', NOW()),
    ('22222222-2222-2222-2222-222222222222', 'sarah@payflow.demo', '+15551234568', '$2a$12$hlmYcwqrBlQIQwR0X8rb8unnqwuJQ4ZJmxktBhga8TrYifrGL1ID6', 'ACTIVE', 'ROLE_USER', 'TIER_2', NOW() - INTERVAL '14 days', NOW()),
    ('33333333-3333-3333-3333-333333333333', 'rahul@payflow.demo', '+15551234569', '$2a$12$hlmYcwqrBlQIQwR0X8rb8unnqwuJQ4ZJmxktBhga8TrYifrGL1ID6', 'ACTIVE', 'ROLE_USER', 'TIER_2', NOW() - INTERVAL '14 days', NOW()),
    ('44444444-4444-4444-4444-444444444444', 'alex@payflow.demo', '+15551234570', '$2a$12$hlmYcwqrBlQIQwR0X8rb8unnqwuJQ4ZJmxktBhga8TrYifrGL1ID6', 'ACTIVE', 'ROLE_USER', 'TIER_2', NOW() - INTERVAL '14 days', NOW());
'@

$userSql | docker exec -i payflow-postgres psql -U postgres -d user_db
Write-Host "  [OK] user_db demo users seeded." -ForegroundColor Green

# 2. Reset wallet_db
$walletSql = @"
DELETE FROM wallet_audit_log WHERE wallet_id IN (
    '11111111-2222-3333-4444-555555555555',
    '22222222-3333-4444-5555-666666666666',
    '33333333-4444-5555-6666-777777777777',
    '44444444-5555-6666-7777-888888888888',
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000002'
);

DELETE FROM wallets WHERE id IN (
    '11111111-2222-3333-4444-555555555555',
    '22222222-3333-4444-5555-666666666666',
    '33333333-4444-5555-6666-777777777777',
    '44444444-5555-6666-7777-888888888888',
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000002'
) OR user_id IN (
    '11111111-1111-1111-1111-111111111111',
    '22222222-2222-2222-2222-222222222222',
    '33333333-3333-3333-3333-333333333333',
    '44444444-4444-4444-4444-444444444444'
);

-- System Wallets
INSERT INTO wallets (id, user_id, currency, balance_minor, status, version, created_at, updated_at)
VALUES
    ('00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 'INR', 9999999999, 'ACTIVE', 0, NOW() - INTERVAL '30 days', NOW()),
    ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', 'INR', 9999999999, 'ACTIVE', 0, NOW() - INTERVAL '30 days', NOW())
ON CONFLICT (id) DO UPDATE SET balance_minor = 9999999999;

-- 4 Demo Wallets with Exact Balances:
-- Arbaz Sayyad: ₹24,750.00 (2,475,000 minor)
-- Sarah Miller: ₹12,500.00 (1,250,000 minor)
-- Rahul Sharma: ₹18,300.00 (1,830,000 minor)
-- Alex Johnson: ₹ 7,850.00 (  785,000 minor)
INSERT INTO wallets (id, user_id, currency, balance_minor, status, version, created_at, updated_at)
VALUES
    ('11111111-2222-3333-4444-555555555555', '11111111-1111-1111-1111-111111111111', 'INR', 2475000, 'ACTIVE', 0, NOW() - INTERVAL '14 days', NOW()),
    ('22222222-3333-4444-5555-666666666666', '22222222-2222-2222-2222-222222222222', 'INR', 1250000, 'ACTIVE', 0, NOW() - INTERVAL '14 days', NOW()),
    ('33333333-4444-5555-6666-777777777777', '33333333-3333-3333-3333-333333333333', 'INR', 1830000, 'ACTIVE', 0, NOW() - INTERVAL '14 days', NOW()),
    ('44444444-5555-6666-7777-888888888888', '44444444-4444-4444-4444-444444444444', 'INR',  785000, 'ACTIVE', 0, NOW() - INTERVAL '14 days', NOW());
"@

$walletSql | docker exec -i payflow-postgres psql -U postgres -d wallet_db
Write-Host "  [OK] wallet_db demo wallets seeded with exact balances." -ForegroundColor Green

# 3. Reset payment_db
$paymentSql = @"
DELETE FROM payments WHERE sender_wallet_id IN (
    '11111111-2222-3333-4444-555555555555',
    '22222222-3333-4444-5555-666666666666',
    '33333333-4444-5555-6666-777777777777',
    '44444444-5555-6666-7777-888888888888'
) OR recipient_wallet_id IN (
    '11111111-2222-3333-4444-555555555555',
    '22222222-3333-4444-5555-666666666666',
    '33333333-4444-5555-6666-777777777777',
    '44444444-5555-6666-7777-888888888888'
);

-- Seed Realistic Historical Payments for Arbaz Sayyad
INSERT INTO payments (id, sender_wallet_id, recipient_wallet_id, amount_minor, currency, status, payment_type, idempotency_key, created_at, updated_at)
VALUES
    ('a0000001-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', '11111111-2222-3333-4444-555555555555', 1000000, 'INR', 'SUCCESS', 'TOP_UP', 'SEED-OPENING-BALANCE', NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days'),
    ('a0000001-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', '11111111-2222-3333-4444-555555555555', 2500000, 'INR', 'SUCCESS', 'P2P_TRANSFER', 'SEED-SALARY-CREDIT', NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
    ('a0000001-0000-0000-0000-000000000003', '11111111-2222-3333-4444-555555555555', '00000000-0000-0000-0000-000000000002',  249900, 'INR', 'SUCCESS', 'MERCHANT_PAYMENT', 'SEED-AMAZON-PURCHASE', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
    ('a0000001-0000-0000-0000-000000000004', '11111111-2222-3333-4444-555555555555', '22222222-3333-4444-5555-666666666666',  150000, 'INR', 'SUCCESS', 'P2P_TRANSFER', 'SEED-TRANSFER-SARAH', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
    ('a0000001-0000-0000-0000-000000000005', '11111111-2222-3333-4444-555555555555', '00000000-0000-0000-0000-000000000002',   65000, 'INR', 'SUCCESS', 'MERCHANT_PAYMENT', 'SEED-FOOD-DELIVERY', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),
    ('a0000001-0000-0000-0000-000000000006', '11111111-2222-3333-4444-555555555555', '00000000-0000-0000-0000-000000000002',  234000, 'INR', 'SUCCESS', 'MERCHANT_PAYMENT', 'SEED-ELECTRICITY-BILL', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
    ('a0000001-0000-0000-0000-000000000007', '33333333-4444-5555-6666-777777777777', '11111111-2222-3333-4444-555555555555',  300000, 'INR', 'SUCCESS', 'P2P_TRANSFER', 'SEED-TRANSFER-RAHUL', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
    ('a0000001-0000-0000-0000-000000000008', '00000000-0000-0000-0000-000000000001', '11111111-2222-3333-4444-555555555555',  500000, 'INR', 'SUCCESS', 'TOP_UP', 'SEED-WALLET-TOPUP', NOW() - INTERVAL '1 days', NOW() - INTERVAL '1 days'),
    ('a0000001-0000-0000-0000-000000000009', '11111111-2222-3333-4444-555555555555', '00000000-0000-0000-0000-000000000002', 1126100, 'INR', 'SUCCESS', 'MERCHANT_PAYMENT', 'SEED-MUTUAL-FUND', NOW() - INTERVAL '12 hours', NOW() - INTERVAL '12 hours');
"@

$paymentSql | docker exec -i payflow-postgres psql -U postgres -d payment_db
Write-Host "  [OK] payment_db demo payments seeded." -ForegroundColor Green

# 4. Reset ledger_db (Balanced Double-Entry Journal Postings)
$ledgerSql = @"
DELETE FROM journal_entry_lines WHERE wallet_id IN (
    '11111111-2222-3333-4444-555555555555',
    '22222222-3333-4444-5555-666666666666',
    '33333333-4444-5555-6666-777777777777',
    '44444444-5555-6666-7777-888888888888'
);

DELETE FROM journal_entries WHERE transaction_id IN (
    'a0000001-0000-0000-0000-000000000001',
    'a0000001-0000-0000-0000-000000000002',
    'a0000001-0000-0000-0000-000000000003',
    'a0000001-0000-0000-0000-000000000004',
    'a0000001-0000-0000-0000-000000000005',
    'a0000001-0000-0000-0000-000000000006',
    'a0000001-0000-0000-0000-000000000007',
    'a0000001-0000-0000-0000-000000000008',
    'a0000001-0000-0000-0000-000000000009'
);

-- 1. Opening Balance (+10,000 INR)
INSERT INTO journal_entries (id, transaction_id, description, currency, created_at)
VALUES ('b0000001-0000-0000-0000-000000000001', 'a0000001-0000-0000-0000-000000000001', 'Opening Demo Balance', 'INR', NOW() - INTERVAL '14 days');
INSERT INTO journal_entry_lines (id, journal_entry_id, wallet_id, entry_type, amount_minor, created_at)
VALUES
    ('c0000001-0000-0000-0000-000000000001', 'b0000001-0000-0000-0000-000000000001', '11111111-2222-3333-4444-555555555555', 'CREDIT', 1000000, NOW() - INTERVAL '14 days'),
    ('c0000001-0000-0000-0000-000000000002', 'b0000001-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 'DEBIT',  1000000, NOW() - INTERVAL '14 days');

-- 2. Salary Credit (+25,000 INR)
INSERT INTO journal_entries (id, transaction_id, description, currency, created_at)
VALUES ('b0000001-0000-0000-0000-000000000002', 'a0000001-0000-0000-0000-000000000002', 'Salary Credit - TechCorp Solutions', 'INR', NOW() - INTERVAL '10 days');
INSERT INTO journal_entry_lines (id, journal_entry_id, wallet_id, entry_type, amount_minor, created_at)
VALUES
    ('c0000001-0000-0000-0000-000000000003', 'b0000001-0000-0000-0000-000000000002', '11111111-2222-3333-4444-555555555555', 'CREDIT', 2500000, NOW() - INTERVAL '10 days'),
    ('c0000001-0000-0000-0000-000000000004', 'b0000001-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', 'DEBIT',  2500000, NOW() - INTERVAL '10 days');

-- 3. Amazon Purchase (-2,499 INR)
INSERT INTO journal_entries (id, transaction_id, description, currency, created_at)
VALUES ('b0000001-0000-0000-0000-000000000003', 'a0000001-0000-0000-0000-000000000003', 'Amazon Purchase - Prime Electronics', 'INR', NOW() - INTERVAL '7 days');
INSERT INTO journal_entry_lines (id, journal_entry_id, wallet_id, entry_type, amount_minor, created_at)
VALUES
    ('c0000001-0000-0000-0000-000000000005', 'b0000001-0000-0000-0000-000000000003', '11111111-2222-3333-4444-555555555555', 'DEBIT',   249900, NOW() - INTERVAL '7 days'),
    ('c0000001-0000-0000-0000-000000000006', 'b0000001-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000002', 'CREDIT',  249900, NOW() - INTERVAL '7 days');

-- 4. Transfer to Sarah Miller (-1,500 INR)
INSERT INTO journal_entries (id, transaction_id, description, currency, created_at)
VALUES ('b0000001-0000-0000-0000-000000000004', 'a0000001-0000-0000-0000-000000000004', 'Transfer to Sarah Miller', 'INR', NOW() - INTERVAL '5 days');
INSERT INTO journal_entry_lines (id, journal_entry_id, wallet_id, entry_type, amount_minor, created_at)
VALUES
    ('c0000001-0000-0000-0000-000000000007', 'b0000001-0000-0000-0000-000000000004', '11111111-2222-3333-4444-555555555555', 'DEBIT',   150000, NOW() - INTERVAL '5 days'),
    ('c0000001-0000-0000-0000-000000000008', 'b0000001-0000-0000-0000-000000000004', '22222222-3333-4444-5555-666666666666', 'CREDIT',  150000, NOW() - INTERVAL '5 days');

-- 5. Food Delivery (-650 INR)
INSERT INTO journal_entries (id, transaction_id, description, currency, created_at)
VALUES ('b0000001-0000-0000-0000-000000000005', 'a0000001-0000-0000-0000-000000000005', 'Food Delivery - Bistro Kitchen', 'INR', NOW() - INTERVAL '4 days');
INSERT INTO journal_entry_lines (id, journal_entry_id, wallet_id, entry_type, amount_minor, created_at)
VALUES
    ('c0000001-0000-0000-0000-000000000009', 'b0000001-0000-0000-0000-000000000005', '11111111-2222-3333-4444-555555555555', 'DEBIT',    65000, NOW() - INTERVAL '4 days'),
    ('c0000001-0000-0000-0000-000000000010', 'b0000001-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000002', 'CREDIT',   65000, NOW() - INTERVAL '4 days');

-- 6. Electricity Bill (-2,340 INR)
INSERT INTO journal_entries (id, transaction_id, description, currency, created_at)
VALUES ('b0000001-0000-0000-0000-000000000006', 'a0000001-0000-0000-0000-000000000006', 'Electricity Bill Payment', 'INR', NOW() - INTERVAL '3 days');
INSERT INTO journal_entry_lines (id, journal_entry_id, wallet_id, entry_type, amount_minor, created_at)
VALUES
    ('c0000001-0000-0000-0000-000000000011', 'b0000001-0000-0000-0000-000000000006', '11111111-2222-3333-4444-555555555555', 'DEBIT',   234000, NOW() - INTERVAL '3 days'),
    ('c0000001-0000-0000-0000-000000000012', 'b0000001-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000002', 'CREDIT',  234000, NOW() - INTERVAL '3 days');

-- 7. Transfer from Rahul Sharma (+3,000 INR)
INSERT INTO journal_entries (id, transaction_id, description, currency, created_at)
VALUES ('b0000001-0000-0000-0000-000000000007', 'a0000001-0000-0000-0000-000000000007', 'Transfer from Rahul Sharma', 'INR', NOW() - INTERVAL '2 days');
INSERT INTO journal_entry_lines (id, journal_entry_id, wallet_id, entry_type, amount_minor, created_at)
VALUES
    ('c0000001-0000-0000-0000-000000000013', 'b0000001-0000-0000-0000-000000000007', '11111111-2222-3333-4444-555555555555', 'CREDIT',  300000, NOW() - INTERVAL '2 days'),
    ('c0000001-0000-0000-0000-000000000014', 'b0000001-0000-0000-0000-000000000007', '33333333-4444-5555-6666-777777777777', 'DEBIT',   300000, NOW() - INTERVAL '2 days');

-- 8. Wallet Top-up (+5,000 INR)
INSERT INTO journal_entries (id, transaction_id, description, currency, created_at)
VALUES ('b0000001-0000-0000-0000-000000000008', 'a0000001-0000-0000-0000-000000000008', 'Wallet Top-up via UPI NetBanking', 'INR', NOW() - INTERVAL '1 days');
INSERT INTO journal_entry_lines (id, journal_entry_id, wallet_id, entry_type, amount_minor, created_at)
VALUES
    ('c0000001-0000-0000-0000-000000000015', 'b0000001-0000-0000-0000-000000000008', '11111111-2222-3333-4444-555555555555', 'CREDIT',  500000, NOW() - INTERVAL '1 days'),
    ('c0000001-0000-0000-0000-000000000016', 'b0000001-0000-0000-0000-000000000008', '00000000-0000-0000-0000-000000000001', 'DEBIT',   500000, NOW() - INTERVAL '1 days');

-- 9. Investment / Mutual Fund Expense (-11,261 INR)
INSERT INTO journal_entries (id, transaction_id, description, currency, created_at)
VALUES ('b0000001-0000-0000-0000-000000000009', 'a0000001-0000-0000-0000-000000000009', 'Investment Settlement - Index Fund', 'INR', NOW() - INTERVAL '12 hours');
INSERT INTO journal_entry_lines (id, journal_entry_id, wallet_id, entry_type, amount_minor, created_at)
VALUES
    ('c0000001-0000-0000-0000-000000000017', 'b0000001-0000-0000-0000-000000000009', '11111111-2222-3333-4444-555555555555', 'DEBIT',  1126100, NOW() - INTERVAL '12 hours'),
    ('c0000001-0000-0000-0000-000000000018', 'b0000001-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', 'CREDIT', 1126100, NOW() - INTERVAL '12 hours');
"@

$ledgerSql | docker exec -i payflow-postgres psql -U postgres -d ledger_db
Write-Host "  [OK] ledger_db balanced double-entry statements seeded." -ForegroundColor Green

# 5. Reset notification_db
$notifSql = @"
DELETE FROM notification_logs WHERE user_id IN (
    '11111111-1111-1111-1111-111111111111',
    '22222222-2222-2222-2222-222222222222',
    '33333333-3333-3333-3333-333333333333',
    '44444444-4444-4444-4444-444444444444'
);

INSERT INTO notification_logs (id, event_id, user_id, channel, recipient, subject, body, status, created_at, sent_at)
VALUES
    ('d0000001-0000-0000-0000-000000000001', 'e0000001-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'EMAIL', 'demo@payflow.demo', 'Top-up Successful', 'Your wallet has been topped up with ₹5,000.00.', 'SENT', NOW() - INTERVAL '1 days', NOW() - INTERVAL '1 days'),
    ('d0000001-0000-0000-0000-000000000002', 'e0000001-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111', 'SMS', '+15551234567', 'Money Received', 'Received ₹3,000.00 from Rahul Sharma.', 'SENT', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),
    ('d0000001-0000-0000-0000-000000000003', 'e0000001-0000-0000-0000-000000000003', '11111111-1111-1111-1111-111111111111', 'EMAIL', 'demo@payflow.demo', 'Transfer Completed', '₹1,500.00 sent to Sarah Miller successfully.', 'SENT', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
    ('d0000001-0000-0000-0000-000000000004', 'e0000001-0000-0000-0000-000000000004', '11111111-1111-1111-1111-111111111111', 'EMAIL', 'demo@payflow.demo', 'Salary Credited', '₹25,000.00 corporate payroll salary credited to your wallet.', 'SENT', NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days');
"@

$notifSql | docker exec -i payflow-postgres psql -U postgres -d notification_db
Write-Host "  [OK] notification_db demo alerts seeded." -ForegroundColor Green

# 6. Flush Redis Cache for demo keys
docker exec payflow-redis redis-cli KEYS "wallet:balance:*" | ForEach-Object {
    if ($_ -ne "") {
        docker exec payflow-redis redis-cli DEL $_ | Out-Null
    }
}
docker exec payflow-redis redis-cli KEYS "idempotency:*" | ForEach-Object {
    if ($_ -ne "") {
        docker exec payflow-redis redis-cli DEL $_ | Out-Null
    }
}
Write-Host "  [OK] Redis cache evicted for demo keys." -ForegroundColor Green

Write-Host "`nDEMO ENVIRONMENT RESET COMPLETED SUCCESSFULLY!" -ForegroundColor Green
Write-Host "  Arbaz Sayyad (demo@payflow.demo) Balance: ₹24,750.00" -ForegroundColor Yellow
Write-Host "  Sarah Miller (sarah@payflow.demo) Balance: ₹12,500.00" -ForegroundColor Yellow
Write-Host "  Rahul Sharma (rahul@payflow.demo) Balance: ₹18,300.00" -ForegroundColor Yellow
Write-Host "  Alex Johnson (alex@payflow.demo) Balance: ₹7,850.00`n" -ForegroundColor Yellow
