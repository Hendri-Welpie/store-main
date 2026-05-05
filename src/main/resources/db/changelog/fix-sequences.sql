
SELECT setval(
               pg_get_serial_sequence('customer', 'id'),
               COALESCE((SELECT MAX(id) FROM customer), 0)
       );
SELECT setval(
               pg_get_serial_sequence('order', 'id'),
               COALESCE((SELECT MAX(id) FROM "order"), 0)
       );
