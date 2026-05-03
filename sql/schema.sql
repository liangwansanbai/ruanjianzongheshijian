CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL,
    real_name VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    CONSTRAINT ck_users_role CHECK (role IN ('admin', 'user'))
);

CREATE TABLE rooms (
    room_id INT AUTO_INCREMENT PRIMARY KEY,
    room_no VARCHAR(20) UNIQUE NOT NULL,
    room_type VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT '空闲' NOT NULL,
    remark VARCHAR(200),
    CONSTRAINT ck_rooms_price CHECK (price > 0),
    CONSTRAINT ck_rooms_status CHECK (status IN ('空闲', '已预订', '维修中'))
);

CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    room_id INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_price DECIMAL(10,2),
    status VARCHAR(20) DEFAULT '待确认' NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_orders_total_price CHECK (total_price >= 0),
    CONSTRAINT ck_orders_status CHECK (status IN ('待确认', '已确认', '已完成', '已取消')),
    CONSTRAINT ck_orders_date CHECK (end_date > start_date),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_orders_room FOREIGN KEY (room_id) REFERENCES rooms(room_id)
);

CREATE INDEX idx_rooms_type ON rooms(room_type);
CREATE INDEX idx_rooms_status ON rooms(status);
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_room ON orders(room_id);
CREATE INDEX idx_orders_status ON orders(status);

CREATE OR REPLACE VIEW v_order_detail AS
SELECT
    o.order_id,
    o.user_id,
    u.username,
    u.real_name,
    o.room_id,
    r.room_no,
    r.room_type,
    r.price,
    o.start_date,
    o.end_date,
    o.total_price,
    o.status,
    o.create_time
FROM orders o
JOIN users u ON o.user_id = u.user_id
JOIN rooms r ON o.room_id = r.room_id;

DROP TRIGGER IF EXISTS trg_after_order_insert;
DELIMITER $$
CREATE TRIGGER trg_after_order_insert
AFTER INSERT ON orders
FOR EACH ROW
BEGIN
    UPDATE rooms
    SET status = CASE
                     WHEN status = '维修中' THEN '维修中'
                     ELSE '已预订'
                 END
    WHERE room_id = NEW.room_id;
END$$
DELIMITER ;

DROP TRIGGER IF EXISTS trg_after_order_status_update;
DELIMITER $$
CREATE TRIGGER trg_after_order_status_update
AFTER UPDATE ON orders
FOR EACH ROW
BEGIN
    IF NEW.status IN ('已完成', '已取消') THEN
        UPDATE rooms
        SET status = CASE
                         WHEN status = '维修中' THEN '维修中'
                         ELSE '空闲'
                     END
        WHERE room_id = NEW.room_id;
    ELSEIF NEW.status IN ('待确认', '已确认') THEN
        UPDATE rooms
        SET status = CASE
                         WHEN status = '维修中' THEN '维修中'
                         ELSE '已预订'
                     END
        WHERE room_id = NEW.room_id;
    END IF;
END$$
DELIMITER ;
