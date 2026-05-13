-- V5: Add plan and trial fields for premium features
ALTER TABLE users ADD COLUMN plan_type VARCHAR(20) DEFAULT 'FREE' AFTER timezone;
ALTER TABLE users ADD COLUMN is_trial_used BOOLEAN DEFAULT FALSE AFTER plan_type;
