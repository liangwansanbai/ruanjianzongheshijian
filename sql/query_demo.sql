SELECT * FROM users;

SELECT * FROM rooms ORDER BY room_id;

SELECT * FROM rooms WHERE status = '空闲' ORDER BY room_id;

SELECT * FROM rooms WHERE room_type = '单人间' ORDER BY room_id;

SELECT * FROM rooms WHERE price BETWEEN 100 AND 300 ORDER BY room_id;

SELECT *
FROM rooms
WHERE room_no LIKE '%10%'
   OR NVL(remark, '') LIKE '%普通%'
ORDER BY room_id;

SELECT *
FROM v_order_detail
WHERE username = 'user01'
ORDER BY order_id DESC;

SELECT *
FROM v_order_detail
WHERE status = '待确认'
ORDER BY order_id DESC;

SELECT status, COUNT(*) AS room_count
FROM rooms
GROUP BY status
ORDER BY status;

SELECT status, COUNT(*) AS order_count
FROM orders
GROUP BY status
ORDER BY status;

SELECT NVL(SUM(total_price), 0) AS total_income
FROM orders
WHERE status = '已完成';
