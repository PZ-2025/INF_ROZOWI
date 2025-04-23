-- Sprawdzenie czy kolumna notification_type już istnieje
SET @NotificationTypeColumnExists = 0;

SELECT COUNT(*) INTO @NotificationTypeColumnExists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'it_task_management'
  AND TABLE_NAME = 'notifications'
  AND COLUMN_NAME = 'notification_type';

-- Dodanie kolumny notification_type jeśli nie istnieje
SET @query = IF(@NotificationTypeColumnExists = 0,
  'ALTER TABLE notifications ADD COLUMN notification_type VARCHAR(50) DEFAULT "Powiadomienie systemowe" AFTER content',
  'SELECT "Column notification_type already exists"');

PREPARE stmt FROM @query;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;