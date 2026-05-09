-- V8: Promote existing user to ADMIN instead of recreating
-- This ensures that if the user logged in via Google, they keep their account settings
UPDATE users 
SET role = 'ADMIN' 
WHERE email = 'prathamkacher.connect@gmail.com';

-- Ensure the user exists if not already there (fallback)
INSERT INTO users (full_name, email, role, is_active, provider, currency, timezone, plan_type, is_trial_used)
SELECT 'Pratham', 'prathamkacher.connect@gmail.com', 'ADMIN', 1, 'LOCAL', 'INR', 'Asia/Kolkata', 'FREE', 0
FROM (SELECT 1) AS dummy
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'prathamkacher.connect@gmail.com');
